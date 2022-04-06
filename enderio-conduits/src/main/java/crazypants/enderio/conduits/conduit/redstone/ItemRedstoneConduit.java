package crazypants.enderio.conduits.conduit.redstone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.conduit.ConduitDisplayMode;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IServerConduit;
import crazypants.enderio.base.conduit.geom.Offset;
import crazypants.enderio.base.conduit.registry.ConduitBuilder;
import crazypants.enderio.base.conduit.registry.ConduitRegistry;
import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.conduits.conduit.AbstractItemConduit;
import crazypants.enderio.conduits.conduit.ItemConduitSubtype;
import crazypants.enderio.conduits.render.ConduitBundleRenderManager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRedstoneConduit extends AbstractItemConduit {

  public static ItemRedstoneConduit create(@Nonnull IModObject modObject, @Nullable Block block) {
    return new ItemRedstoneConduit(modObject);
  }

  protected ItemRedstoneConduit(@Nonnull IModObject modObject) {
    super(modObject, new ItemConduitSubtype(modObject.getUnlocalisedName() + "_insulated", modObject.getRegistryName().toString() + "_insulated"));

    ConduitRegistry.register(ConduitBuilder.start().setUUID(new ResourceLocation(EnderIO.DOMAIN, "redstone")).setClass(getBaseConduitType())
        .setOffsets(Offset.UP, Offset.UP, Offset.NORTH, Offset.UP).setCanConnectToAnything().build()
        .setUUID(new ResourceLocation(EnderIO.DOMAIN, "redstone_conduit")).setClass(InsulatedRedstoneConduit.class).build().finish());
    ConduitDisplayMode.registerDisplayMode(new ConduitDisplayMode(getBaseConduitType(), IconEIO.WRENCH_OVERLAY_REDSTONE, IconEIO.WRENCH_OVERLAY_REDSTONE_OFF));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers(@Nonnull IModObject modObject) {
    super.registerRenderers(modObject);
    ConduitBundleRenderManager.instance.getConduitBundleRenderer().registerRenderer(new InsulatedRedstoneConduitRenderer());
  }

  @Override
  public @Nonnull Class<? extends IConduit> getBaseConduitType() {
    return IRedstoneConduit.class;
  }

  @Override
  public IServerConduit createConduit(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    return new InsulatedRedstoneConduit();
  }

  @Override
  public boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
    return true;
  }
}
