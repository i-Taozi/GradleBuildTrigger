package crazypants.enderio.base.item.yetawrench;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;
import com.enderio.core.common.vecmath.Vector4d;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import crazypants.enderio.api.IModObject;
import crazypants.enderio.api.tool.IConduitControl;
import crazypants.enderio.api.tool.ITool;
import crazypants.enderio.base.BlockEio;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.conduit.ConduitDisplayMode;
import crazypants.enderio.base.config.config.PersonalConfig;
import crazypants.enderio.base.handler.KeyTracker;
import crazypants.enderio.base.machine.interfaces.IYetaAwareBlock;
import crazypants.enderio.base.paint.IPaintable.IBlockPaintableBlock;
import crazypants.enderio.base.paint.PaintUtil;
import crazypants.enderio.base.paint.YetaUtil;
import crazypants.enderio.base.render.IHaveRenderers;
import crazypants.enderio.base.render.registry.ItemModelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.InterfaceList({ @Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core"),
    @Interface(iface = "cofh.api.item.IToolHammer", modid = "cofhcore") })
public class ItemYetaWrench extends Item implements ITool, IConduitControl, IAdvancedTooltipProvider, IToolWrench, IToolHammer, IHaveRenderers {

  public static ItemYetaWrench create(@Nonnull IModObject modObject, @Nullable Block block) {
    return new ItemYetaWrench(modObject);
  }

  protected ItemYetaWrench(@Nonnull IModObject modObject) {
    setCreativeTab(EnderIOTab.tabEnderIOItems);
    modObject.apply(this);
    setMaxStackSize(1);
  }

  @Override
  public @Nonnull EnumActionResult onItemUseFirst(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side,
      float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {

    final IBlockState blockState = world.getBlockState(pos);
    IBlockState bs = blockState;
    Block block = bs.getBlock();
    boolean ret = false;
    RightClickBlock e = new RightClickBlock(player, hand, pos, side, new Vec3d(hitX, hitY, hitZ));
    if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.getUseBlock() == Result.DENY || e.getUseItem() == Result.DENY) {
      return EnumActionResult.PASS;
    }
    if (block instanceof BlockDoor) {
      EnumDoorHalf half = bs.getValue(BlockDoor.HALF);
      if (half == EnumDoorHalf.UPPER) {
        pos = pos.down();
      }
    }
    if (!player.isSneaking() && block.rotateBlock(world, pos, side)) {
      ret = true;
    } else if (block instanceof IBlockPaintableBlock && !player.isSneaking() && !YetaUtil.shouldHeldItemHideFacades(player)) {
      IBlockState paintSource = ((IBlockPaintableBlock) block).getPaintSource(blockState, world, pos);
      if (paintSource != null) {
        final IBlockState rotatedPaintSource = PaintUtil.rotate(paintSource);
        if (rotatedPaintSource != paintSource) {
          ((IBlockPaintableBlock) block).setPaintSource(blockState, world, pos, rotatedPaintSource);
        }
        ret = true;
      }
    }

    // Need to catch 'shift-clicks' here and pass them on manually or an item in the off hand can eat the right click
    // so 'onBlockActivated' is never called
    if (!ret && player.isSneaking() && block instanceof BlockEio<?>) {
      BlockEio<?> beio = (BlockEio<?>) block;
      if (beio.shouldWrench(world, pos, player, side)) {
        beio.onBlockActivated(world, pos, bs, player, hand, side, hitX, hitY, hitZ);
        ret = true;
      }
    }
    if (ret) {
      player.swingArm(hand);
    }

    // If its client side we have to return pass so this method is called on server, where we need to perform the op
    return ret && !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
  }

  @Override
  public @Nonnull ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
    ItemStack equipped = player.getHeldItem(hand);
    if (!PersonalConfig.yetaUseSneakRightClick.get() || !player.isSneaking()) {
      return new ActionResult<ItemStack>(EnumActionResult.PASS, equipped);
    }
    ConduitDisplayMode curMode = ConduitDisplayMode.getDisplayMode(equipped);
    ConduitDisplayMode newMode = curMode.next();
    ConduitDisplayMode.setDisplayMode(equipped, newMode);
    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, equipped);
  }

  @Override
  public boolean onBlockStartBreak(@Nonnull ItemStack itemstack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
    IBlockState bs = player.world.getBlockState(pos);
    Block block = bs.getBlock();
    if (player.isSneaking() && block instanceof IYetaAwareBlock && player.capabilities.isCreativeMode) {
      block.onBlockClicked(player.world, pos, player);
      return true;
    }
    return false;
  }

  @Override
  public boolean canDestroyBlockInCreative(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    return false;
  }

  @Override
  public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
    return !ItemStack.areItemsEqual(oldStack, newStack); // Ignore NBT
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isFull3D() {
    return true;
  }

  @Override
  public boolean doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
    return false;
  }

  @Override
  public boolean canUse(@Nonnull EnumHand hand, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
    return true;
  }

  @Override
  public void used(@Nonnull EnumHand hand, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
  }

  @Override
  public boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    ConduitDisplayMode curMode = ConduitDisplayMode.getDisplayMode(stack);
    return curMode != ConduitDisplayMode.NONE && curMode != ConduitDisplayMode.NEUTRAL;
  }

  @Override
  public boolean showOverlay(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    return true;
  }

  /* IAdvancedTooltipProvider */

  @Override
  public void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
  }

  @Override
  public void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
  }

  @Override
  public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    ArrayList<String> tmp = new ArrayList<String>();
    SpecialTooltipHandler.addDetailedTooltipFromResources(tmp, getUnlocalizedName());
    String keyName = KeyTracker.yetawrenchmode.getBinding().getDisplayName();
    for (String line : tmp) {
      list.add(String.format(line, keyName));
    }
  }

  @Override
  @Optional.Method(modid = "BuildCraftAPI|core")
  public boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack stack, RayTraceResult res) {
    return true;
  }

  @SuppressWarnings("null")
  @Override
  @Optional.Method(modid = "BuildCraftAPI|core")
  public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack stack, RayTraceResult res) {
    used(hand, player, res.getBlockPos());
  }

  @Override
  @Optional.Method(modid = "cofhcore")
  public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
    return true;
  }

  @Override
  @Optional.Method(modid = "cofhcore")
  public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
    return false;
  }

  @SuppressWarnings("null")
  @Override
  @Optional.Method(modid = "cofhcore")
  public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
    if (user instanceof EntityPlayer) {
      used(EnumHand.MAIN_HAND, (EntityPlayer) user, pos);
    }
  }

  @Override
  @Optional.Method(modid = "cofhcore")
  public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers(@Nonnull IModObject modObject) {
    ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(modObject.getRegistryName(), "#inventory"));
    if (PersonalConfig.animatedYeta.get()) {
      ItemModelRegistry.registerRotating(new ModelResourceLocation(modObject.getRegistryName(), "#inventory"), transformType -> {
        switch (transformType) {
        case THIRD_PERSON_RIGHT_HAND:
          return new Vector4d(1, 1, 0, 1);
        case THIRD_PERSON_LEFT_HAND:
          return new Vector4d(-1, 1, 0, 1);
        default:
          return null;
        }
      });

    }
  }
}
