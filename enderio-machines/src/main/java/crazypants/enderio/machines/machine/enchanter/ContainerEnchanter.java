package crazypants.enderio.machines.machine.enchanter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostBackgroundItemSlot;
import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.ContainerEnder;

import crazypants.enderio.base.Log;
import crazypants.enderio.base.recipe.IMachineRecipe.ResultStack;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.enchanter.EnchanterRecipe;
import crazypants.enderio.util.Prep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEnchanter extends ContainerEnder<TileEnchanter> {

  // JEI wants this data without giving us a chance to instantiate a container
  public static int FIRST_RECIPE_SLOT = 0;
  public static int NUM_RECIPE_SLOT = 3;
  public static int FIRST_INVENTORY_SLOT = 3 + 1 + 0; // input + output + upgrade
  public static int NUM_INVENTORY_SLOT = 4 * 9;

  public ContainerEnchanter(EntityPlayer player, @Nonnull InventoryPlayer playerInv, @Nonnull TileEnchanter te) {
    super(playerInv, te);
  }

  @Override
  protected void addSlots(@Nonnull InventoryPlayer playerInv) {

    final TileEnchanter te = getInv();

    addSlotToContainer(new Slot(te, 0, 16, 35) {

      @Override
      public int getSlotStackLimit() {
        return 1;
      }

      @Override
      public boolean isItemValid(@Nonnull ItemStack itemStack) {
        return te.isItemValidForSlot(0, itemStack);
      }

      @Override
      public void onSlotChanged() {
        updateOutput();
      }

    });

    addSlotToContainer(new Slot(te, 1, 65, 35) {

      @Override
      public boolean isItemValid(@Nonnull ItemStack itemStack) {
        return te.isItemValidForSlot(1, itemStack);
      }

      @Override
      public void onSlotChanged() {
        updateOutput();
      }

    });

    addSlotToContainer(new Slot(te, 2, 85, 35) {

      @Override
      public boolean isItemValid(@Nonnull ItemStack itemStack) {
        return te.isItemValidForSlot(2, itemStack);
      }

      @Override
      public void onSlotChanged() {
        updateOutput();
      }

    });

    addSlotToContainer(new Slot(te, 3, 144, 35) {

      @Override
      public int getSlotStackLimit() {
        return 1;
      }

      @Override
      public boolean isItemValid(@Nullable ItemStack itemStack) {
        return false;
      }

      @Override
      public @Nonnull ItemStack onTake(@Nonnull EntityPlayer player, @Nonnull ItemStack stack) {
        EnchanterRecipe currentEnchantmentRecipe = getInv().getCurrentEnchantmentRecipe();
        if (currentEnchantmentRecipe != null) {
          final int xpCost = currentEnchantmentRecipe.getXPCost(getInv().getInvAsMachineRecipeInput());
          List<MachineRecipeInput> quantitiesConsumed = currentEnchantmentRecipe.getQuantitiesConsumed(getInv().getInvAsMachineRecipeInput());
          for (MachineRecipeInput machineRecipeInput : quantitiesConsumed) {
            te.decrStackSize(machineRecipeInput.slotNumber, machineRecipeInput.item.getCount());
          }
          if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-xpCost);
          }
        } else {
          Log.error("Enchanting yielded result without resources");
        }

        if (!te.getWorld().isRemote) {
          te.getWorld().playEvent(1030, te.getPos(), 0);
          te.getWorld().playEvent(2005, te.getPos().up(), 0);
        }
        return stack;
      }

      @Override
      public boolean canTakeStack(@Nonnull EntityPlayer player) {
        return playerHasEnoughLevels(player);
      }

    });
  }

  public void createGhostSlots(List<GhostSlot> slots) {
    slots.add(new GhostBackgroundItemSlot(Items.WRITABLE_BOOK, getSlotFromInventory(0)));
    slots.add(new GhostBackgroundItemSlot(new ItemStack(Items.DYE, 1, 4), getSlotFromInventory(2)));
  }

  public boolean playerHasEnoughLevels(EntityPlayer player) {
    if (player.capabilities.isCreativeMode) {
      return true;
    }
    return player.experienceLevel >= getInv().getCurrentEnchantmentCost();
  }

  private void updateOutput() {
    EnchanterRecipe currentEnchantmentRecipe = getInv().getCurrentEnchantmentRecipe();
    if (currentEnchantmentRecipe != null) {
      ResultStack[] completedResult = currentEnchantmentRecipe.getCompletedResult(0, 1F, getInv().getInvAsMachineRecipeInput());
      getInv().setOutput(completedResult[0].item);
    } else {
      getInv().setOutput(Prep.getEmpty());
    }
  }

  @Override
  public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer par1EntityPlayer, int par2) {
    ItemStack copyStack = Prep.getEmpty();
    Slot slot = inventorySlots.get(par2);

    if (slot != null && slot.getHasStack()) {
      ItemStack origStack = slot.getStack();
      copyStack = origStack.copy();

      if (par2 <= 3) {
        if (!mergeItemStack(origStack, 4, inventorySlots.size(), true)) {
          return Prep.getEmpty();
        }
      } else {
        if (!getInv().isItemValidForSlot(0, origStack) || !mergeItemStack(origStack, 0, 1, false)) {
          if (!getInv().isItemValidForSlot(1, origStack) || !mergeItemStack(origStack, 1, 2, false)) {
            if (!getInv().isItemValidForSlot(2, origStack) || !mergeItemStack(origStack, 2, 3, false)) {
              return Prep.getEmpty();
            }
          }
        }
      }
      if (Prep.isInvalid(origStack)) {
        slot.putStack(Prep.getEmpty());
      } else {
        slot.onSlotChanged();
      }
      return slot.onTake(par1EntityPlayer, origStack);
    }
    return copyStack;
  }
}
