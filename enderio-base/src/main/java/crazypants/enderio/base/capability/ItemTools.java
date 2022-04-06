package crazypants.enderio.base.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.base.diagnostics.Prof;
import crazypants.enderio.base.machine.base.te.AbstractMachineEntity;
import crazypants.enderio.base.machine.modes.IoMode;
import crazypants.enderio.util.Prep;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public final class ItemTools {

  @CapabilityInject(IItemHandler.class)
  public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;

  private ItemTools() {
  }

  public static boolean doPush(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
    final CallbackPush callback = new CallbackPush(world, pos);
    NNList.FACING.apply(callback);
    return callback.movedSomething;
  }

  private static final class CallbackPush implements NNList.ShortCallback<EnumFacing> {
    private final @Nonnull IBlockAccess world;
    private final @Nonnull BlockPos pos;
    boolean movedSomething = false;

    private CallbackPush(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
      this.world = world;
      this.pos = pos;
    }

    @Override
    public boolean apply(@Nonnull EnumFacing facing) {
      MoveResult moveResult = move(NO_LIMIT, world, pos, facing, pos.offset(facing), facing.getOpposite());
      if (moveResult == MoveResult.SOURCE_EMPTY) {
        return true;
      }
      movedSomething |= moveResult == MoveResult.MOVED;
      return false;
    }
  }

  public static boolean doPull(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    final CallbackPull callback = new CallbackPull(world, pos);
    NNList.FACING.apply(callback);
    return callback.movedSomething;
  }

  private static final class CallbackPull implements NNList.ShortCallback<EnumFacing> {
    private final @Nonnull IBlockAccess world;
    private final @Nonnull BlockPos pos;
    boolean movedSomething = false;

    private CallbackPull(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
      this.world = world;
      this.pos = pos;
    }

    @Override
    public boolean apply(@Nonnull EnumFacing facing) {
      MoveResult moveResult = move(NO_LIMIT, world, pos.offset(facing), facing.getOpposite(), pos, facing);
      if (moveResult == MoveResult.TARGET_FULL) {
        return true;
      }
      movedSomething |= moveResult == MoveResult.MOVED;
      return false;
    }
  }

  public enum MoveResult {
    NO_ACTION,
    LIMITED,
    MOVED,
    TARGET_FULL,
    SOURCE_EMPTY;
  }

  public static MoveResult move(@Nonnull Limit limit, @Nonnull IBlockAccess world, @Nonnull BlockPos sourcePos, @Nonnull EnumFacing sourceFacing,
      @Nonnull BlockPos targetPos, @Nonnull EnumFacing targetFacing) {
    return move(limit, world, null, sourcePos, sourceFacing, null, targetPos, targetFacing);
  }

  public static MoveResult move(@Nonnull Limit limit, @Nonnull IBlockAccess world, @Nonnull TileEntity sourceTE, @Nonnull EnumFacing sourceFacing,
      @Nonnull BlockPos targetPos, @Nonnull EnumFacing targetFacing) {
    return move(limit, world, sourceTE, sourceTE.getPos(), sourceFacing, null, targetPos, targetFacing);
  }

  public static MoveResult move(@Nonnull Limit limit, @Nonnull IBlockAccess world, @Nonnull BlockPos sourcePos, @Nonnull EnumFacing sourceFacing,
      @Nonnull TileEntity targetTE, @Nonnull EnumFacing targetFacing) {
    return move(limit, world, null, sourcePos, sourceFacing, targetTE, targetTE.getPos(), targetFacing);
  }

  public static MoveResult move(@Nonnull Limit limit, @Nonnull IBlockAccess world, @Nonnull TileEntity sourceTE, @Nonnull EnumFacing sourceFacing,
      @Nonnull TileEntity targetTE, @Nonnull EnumFacing targetFacing) {
    return move(limit, world, sourceTE, sourceTE.getPos(), sourceFacing, targetTE, targetTE.getPos(), targetFacing);
  }

  private static MoveResult move(@Nonnull Limit limit, @Nonnull IBlockAccess world, @Nullable TileEntity sourceTE, @Nonnull BlockPos sourcePos,
      @Nonnull EnumFacing sourceFacing, @Nullable TileEntity targetTE, @Nonnull BlockPos targetPos, @Nonnull EnumFacing targetFacing) {
    if (!limit.canWork()) {
      return MoveResult.LIMITED;
    }
    Profiler profiler = world instanceof World ? ((World) world).profiler : null;
    boolean movedSomething = false;
    TileEntity source = sourceTE != null ? sourceTE : world.getTileEntity(sourcePos);
    if (source != null && source.hasWorld() && !source.getWorld().isRemote && canPullFrom(source, sourceFacing)) {
      Prof.start(profiler, "from_", source);
      TileEntity target = targetTE != null ? targetTE : world.getTileEntity(targetPos);
      if (target != null && target.hasWorld() && canPutInto(target, targetFacing)) {
        Prof.start(profiler, "to_", target);
        IItemHandler sourceHandler = getExternalInventory(source, sourceFacing);
        if (sourceHandler != null && hasItems(sourceHandler)) {
          IItemHandler targetHandler = getExternalInventory(target, targetFacing);
          if (targetHandler != null && hasFreeSpace(targetHandler)) {
            for (int i = 0; i < sourceHandler.getSlots(); i++) {
              ItemStack removable = sourceHandler.extractItem(i, limit.getItems(), true);
              if (Prep.isValid(removable)) {
                ItemStack unacceptable = insertItemStacked(targetHandler, removable, true); // <---
                int movable = removable.getCount() - unacceptable.getCount();
                if (movable > 0) {
                  ItemStack removed = sourceHandler.extractItem(i, movable, false);
                  if (Prep.isValid(removed)) {
                    ItemStack targetRejected = insertItemStacked(targetHandler, removed, false);
                    if (Prep.isValid(targetRejected)) {
                      ItemStack sourceRejected = insertItemStacked(sourceHandler, targetRejected, false);
                      if (Prep.isValid(sourceRejected)) {
                        EntityItem drop = new EntityItem(source.getWorld(), sourcePos.getX() + 0.5, sourcePos.getY() + 0.5, sourcePos.getZ() + 0.5,
                            sourceRejected);
                        source.getWorld().spawnEntity(drop);
                        Prof.stop(profiler, 2);
                        return MoveResult.MOVED;
                      }
                    }
                  }
                  movedSomething = true;
                  limit.useItems(movable);
                  if (!limit.canWork()) {
                    Prof.stop(profiler, 2);
                    return MoveResult.MOVED;
                  }
                }
              }
            }
          } else {
            Prof.stop(profiler, 2);
            return MoveResult.TARGET_FULL;
          }
        } else {
          Prof.stop(profiler, 2);
          return MoveResult.SOURCE_EMPTY;
        }
        Prof.stop(profiler);
      } else {
        Prof.stop(profiler);
        return MoveResult.TARGET_FULL;
      }
      Prof.stop(profiler);
    } else {
      return MoveResult.SOURCE_EMPTY;
    }
    return movedSomething ? MoveResult.MOVED : MoveResult.NO_ACTION;
  }

  /**
   * 
   * @param inventory
   * @param item
   * @return the number inserted
   */
  public static int doInsertItem(@Nullable IItemHandler inventory, @Nonnull ItemStack item) {
    if (inventory == null || Prep.isInvalid(item)) {
      return 0;
    }
    int startSize = item.getCount();
    ItemStack res = insertItemStacked(inventory, item.copy(), false);
    int val = startSize - res.getCount();
    return val;
  }

  public static boolean hasFreeSpace(@Nonnull IItemHandler handler) {
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (Prep.isInvalid(stack) || (stack.isStackable() && stack.getCount() < stack.getMaxStackSize() && stack.getCount() < handler.getSlotLimit(i))) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasItems(@Nonnull IItemHandler handler) {
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (Prep.isValid(stack)) {
        return true;
      }
    }
    return false;
  }

  public static int countItems(@Nonnull IItemHandler handler, @Nonnull ItemStack template) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (areStacksEqualIgnoringDamage(template, stack)) {
        count += stack.getCount();
      }
    }
    return count;
  }

  /**
   * Determines how many items can be inserted into an inventory given that the number of items is limited by "limit".
   * 
   * @param handler
   *          The target inventory
   * @param template
   *          The item to insert
   * @param limit
   *          The limit, meaning the maximum number of items that are allowed in the inventory.
   * @return The number of items that can be inserted without violating the limit.
   */
  public static int getInsertLimit(@Nonnull IItemHandler handler, @Nonnull ItemStack template, int limit) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (areStacksEqualIgnoringDamage(template, stack)) {
        count += stack.getCount();
        if (count >= limit) {
          return 0;
        }
      }
    }
    return limit - count;
  }

  // TODO: switch to com.enderio.core.common.util.ItemUtil.areStacksEqualIgnoringDamage(ItemStack, ItemStack)
  public static boolean areStacksEqualIgnoringDamage(@Nonnull ItemStack s1, @Nonnull ItemStack s2) {
    if (s1.isEmpty() || s2.isEmpty()) {
      return false;
    }
    if (!s1.isItemEqualIgnoreDurability(s2)) {
      return false;
    }
    return ItemStack.areItemStackTagsEqual(s1, s2);
  }

  public static boolean hasAtLeast(@Nonnull IItemHandler handler, @Nonnull ItemStack template, int limit) {
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (areStacksEqualIgnoringDamage(template, stack)) {
        count += stack.getCount();
        if (count >= limit) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean canPutInto(@Nullable TileEntity tileEntity, @Nonnull EnumFacing facing) {
    if (tileEntity instanceof AbstractMachineEntity) {
      IoMode ioMode = ((AbstractMachineEntity) tileEntity).getIoMode(facing);
      return ioMode != IoMode.DISABLED && ioMode != IoMode.PUSH;
    }
    return true;
  }

  public static boolean canPullFrom(@Nullable TileEntity tileEntity, @Nonnull EnumFacing facing) {
    if (tileEntity instanceof AbstractMachineEntity) {
      IoMode ioMode = ((AbstractMachineEntity) tileEntity).getIoMode(facing);
      return ioMode != IoMode.DISABLED && ioMode != IoMode.PULL;
    }
    return true;
  }

  public static final @Nonnull Limit NO_LIMIT = new Limit(Integer.MAX_VALUE, Integer.MAX_VALUE) {
    @Override
    public void useItems(int count) {
    }
  };

  public static class Limit {
    private int stacks, items;

    public Limit(int stacks, int items) {
      this.stacks = stacks;
      this.items = items;
    }

    public Limit(int items) {
      this.stacks = Integer.MAX_VALUE;
      this.items = items;
    }

    public int getStacks() {
      return stacks;
    }

    public int getItems() {
      return items;
    }

    public void useItems(int count) {
      stacks--;
      items -= count;
    }

    public boolean canWork() {
      return stacks > 0 && items > 0;
    }

    public @Nonnull Limit copy() {
      return new Limit(stacks, items);
    }

  }

  public static @Nullable IItemHandler getExternalInventory(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
    TileEntity te = world.getTileEntity(pos);
    if (te != null) {
      return getExternalInventory(te, face);
    }
    return null;
  }

  public static @Nullable IItemHandler getExternalInventory(@Nonnull TileEntity tile, @Nonnull EnumFacing face) {
    return tile.getCapability(NullHelper.notnullF(ITEM_HANDLER_CAPABILITY, "Capability<IItemHandler> is missing"), face);
  }

  @Nonnull
  public static ItemStack insertItemStacked(@Nonnull IItemHandler inventory, @Nonnull ItemStack stack, boolean simulate) {
    if (Prep.isValid(stack)) {

      // not stackable -> just insert into a new slot
      if (!stack.isStackable()) {
        return ItemHandlerHelper.insertItem(inventory, stack, simulate);
      }

      int sizeInventory = inventory.getSlots();
      int firstEmptyStack = -1;
      int origSize = stack.getCount();

      // go through the inventory and try to fill up already existing items
      for (int i = 0; i < sizeInventory; i++) {
        ItemStack slot = inventory.getStackInSlot(i);
        if (ItemHandlerHelper.canItemStacksStackRelaxed(slot, stack)) {
          stack = inventory.insertItem(i, stack, simulate);

          if ((simulate && stack.getCount() != origSize) || Prep.isInvalid(stack)) {
            // stack has been completely inserted, or we are are simulating and have a partial insert. As inventories may change their acceptance rules after a
            // partial insert, we stop here as the simulated insert doesn't do that.
            return stack;
          }
        } else if (firstEmptyStack < 0 && Prep.isInvalid(slot)) {
          firstEmptyStack = i;
        }
      }

      // insert remainder into empty slot
      if (Prep.isValid(stack) && firstEmptyStack >= 0) {
        stack = inventory.insertItem(firstEmptyStack, stack, simulate);
        if ((!simulate || stack.getCount() == origSize) && Prep.isValid(stack)) {
          // same "partial insert" issue as above
          for (int i = 0; i < sizeInventory; i++) {
            stack = inventory.insertItem(i, stack, simulate);
            if ((simulate && stack.getCount() != origSize) || Prep.isInvalid(stack)) {
              return stack;
            }
          }
        }
      }
    }

    return stack;
  }

  public static @Nonnull ItemStack oneOf(@Nonnull EntityPlayer player, @Nonnull ItemStack stack) {
    if (player.isCreative()) {
      ItemStack copy = stack.copy();
      copy.setCount(1);
      return copy;
    } else {
      player.inventory.markDirty();
      return stack.splitStack(1);
    }
  }

}
