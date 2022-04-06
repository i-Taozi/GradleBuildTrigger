package crazypants.enderio.base.item.conduitprobe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.common.TileEntityBase;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.api.tool.IHideFacades;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IServerConduit;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.base.render.IHaveRenderers;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemConduitProbe extends Item implements IResourceTooltipProvider, IHideFacades, IHaveRenderers {

  public static ItemConduitProbe create(@Nonnull IModObject modObject, @Nullable Block block) {
    return new ItemConduitProbe(modObject);
  }

  protected ItemConduitProbe(@Nonnull IModObject modObject) {
    setCreativeTab(EnderIOTab.tabEnderIOItems);
    modObject.apply(this);
    setMaxStackSize(1);
    setHasSubtypes(true);
  }

  public static boolean copyPasteSettings(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, @Nonnull IConduitBundle bundle, @Nonnull EnumFacing dir) {
    if (player.world.isRemote) {
      return true;
    }

    if (player.isSneaking()) {
      return copySettings(player, stack, bundle, dir);
    } else {
      return pasteSettings(player, stack, bundle, dir);
    }
  }

  public static boolean pasteSettings(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, @Nonnull IConduitBundle bundle, @Nonnull EnumFacing dir) {
    NBTTagCompound nbt = stack.getTagCompound();
    if (nbt == null || nbt.hasNoTags()) {
      return false;
    }

    boolean performedAction = false;

    for (IServerConduit conduit : bundle.getServerConduits()) {
      if (conduit.readConduitSettingsFromNBT(dir, nbt)) {
        performedAction = true;
      }
    }

    if (performedAction) {
      player.sendStatusMessage(Lang.GUI_PROBE_PASTED.toChatServer(), true);
    }

    return performedAction;
  }

  public static boolean copySettings(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, @Nonnull IConduitBundle bundle, @Nonnull EnumFacing dir) {
    NBTTagCompound nbt = new NBTTagCompound();

    for (IServerConduit conduit : bundle.getServerConduits()) {
      if (conduit.getExternalConnections().contains(dir)) {
        conduit.writeConnectionSettingsToNBT(dir, nbt);
      }
    }

    if (!nbt.hasNoTags()) {
      stack.setTagCompound(nbt);
      player.sendStatusMessage(Lang.GUI_PROBE_COPIED.toChatServer(), true);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public @Nonnull EnumActionResult onItemUse(@Nonnull EntityPlayer playerIn, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand,
      @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
    if (playerIn.isCreative()) {
      TileEntity tileEntity = world.getTileEntity(pos);
      if (tileEntity instanceof TileEntityBase) {
        playerIn.sendMessage(new TextComponentString((world.isRemote ? "CLIENT: " : "SERVER: ") + tileEntity.getUpdateTag().toString()));
        if (!world.isRemote) {
          playerIn.sendMessage(new TextComponentString("SAVE: " + tileEntity.writeToNBT(new NBTTagCompound()).toString()));
        }
      }
    }
    ItemStack itemStack = playerIn.getHeldItem(hand);
    if (itemStack.getItemDamage() == 0) {
      if (PacketConduitProbe.canCreatePacket(world, pos)) {
        if (world.isRemote) {
          PacketHandler.INSTANCE.sendToServer(new PacketConduitProbe(pos, side));
        }
        return EnumActionResult.SUCCESS;
      }
    }
    return EnumActionResult.PASS;
  }

  @Override
  public boolean canDestroyBlockInCreative(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    return false;
  }

  @Override
  public @Nonnull String getUnlocalizedNameForTooltip(@Nonnull ItemStack stack) {
    return getUnlocalizedName();
  }

  @Override
  public boolean doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
    return true;
  }

  @Override
  public boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    return true;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers(@Nonnull IModObject modObject) {
    ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(modObject.getRegistryName(), "variant=probe"));
    ModelLoader.setCustomModelResourceLocation(this, 1, new ModelResourceLocation(modObject.getRegistryName(), "variant=copy"));
  }

}
