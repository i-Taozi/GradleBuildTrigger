package crazypants.enderio.base.paint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crazypants.enderio.api.tool.IHideFacades;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.conduit.ConduitDisplayMode;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.paint.IPaintable.IPaintableTileEntity;
import crazypants.enderio.base.tool.ToolUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = EnderIO.MODID, value = Side.CLIENT)
public class YetaUtil {

  private static volatile @Nonnull YetaDisplayMode lastCheckResult = new YetaDisplayMode();
  private static boolean toggled = false;

  public static boolean shouldHeldItemHideFacades(@Nonnull EntityPlayer player) {
    @Nonnull
    ItemStack held = player.getHeldItemMainhand();
    boolean checkResult;
    if (held.getItem() instanceof IHideFacades) {
      checkResult = ((IHideFacades) held.getItem()).shouldHideFacades(held, player);
    } else {
      checkResult = ToolUtil.isToolEquipped(player, EnumHand.MAIN_HAND);
    }
    return checkResult;
  }

  @SubscribeEvent
  public static void onTick(@Nonnull ClientTickEvent evt) {
    if (evt.phase == Phase.END) {
      EntityPlayer player = EnderIO.proxy.getClientPlayer();
      if (player == null) {
        return;
      }
      boolean checkResult = shouldHeldItemHideFacades(player);
      toggled = lastCheckResult.isHideFacades() != checkResult;
      lastCheckResult.setHideFacades(checkResult);
      lastCheckResult.setDisplayMode(getDisplayMode(player));
    }
  }

  public static boolean shouldHeldItemHideFacadesClient() {
    return lastCheckResult.isHideFacades();
  }

  public static ConduitDisplayMode getDisplayModeClient() {
    return lastCheckResult.getDisplayMode();
  }

  public static @Nonnull YetaDisplayMode getYetaDisplayMode() {
    return new YetaDisplayMode(lastCheckResult);
  }

  public static void refresh(@Nonnull TileEntity te) {
    if (toggled && te instanceof IPaintable.IPaintableTileEntity && ((IPaintable.IPaintableTileEntity) te).getPaintSource() != null) {
      BlockPos pos = te.getPos();
      IBlockState bs = te.getWorld().getBlockState(pos);
      te.getWorld().notifyBlockUpdate(pos, bs, bs, 3);
    }
  }

  public static @Nonnull ConduitDisplayMode getDisplayMode(@Nullable EntityPlayer player) {
    player = player == null ? EnderIO.proxy.getClientPlayer() : player;
    if (player == null) {
      return ConduitDisplayMode.ALL;
    }
    return ConduitDisplayMode.getDisplayMode(player.getHeldItemMainhand());
  }

  public static boolean isFacadeHidden(@Nonnull IPaintableTileEntity bundle, EntityPlayer player) {
    if (bundle.getPaintSource() == null) {
      return false;
    }
    if (player == null || player.world.isRemote) {
      return shouldHeldItemHideFacadesClient();
    }
    return shouldHeldItemHideFacades(player);
  }

  public static boolean isSolidFacadeRendered(@Nonnull IConduitBundle bundle, EntityPlayer player) {
    return bundle.hasFacade() && !isFacadeHidden(bundle, player);
  }

  public static boolean renderInternalComponent(EntityPlayer player) {
    if (player == null || player.world.isRemote) {
      return lastCheckResult.getDisplayMode().isAll();
    }
    return getDisplayMode(player).isAll();
  }

  public static boolean renderConduit(EntityPlayer player, @Nullable Class<? extends IConduit> conduitType) {
    if (player == null || player.world.isRemote) {
      return lastCheckResult.renderConduit(conduitType);
    }
    return getDisplayMode(player).renderConduit(conduitType);
  }

  public static boolean renderConduit(EntityPlayer player, @Nonnull IConduit con) {
    return renderConduit(player, con.getBaseConduitType());
  }

  public static final class YetaDisplayMode {
    private boolean hideFacades = false;
    private @Nonnull ConduitDisplayMode displayMode = ConduitDisplayMode.ALL;

    private YetaDisplayMode() {
    }

    private YetaDisplayMode(@Nonnull YetaDisplayMode global) {
      hideFacades = global.hideFacades;
      displayMode = global.displayMode;
    }

    public boolean isHideFacades() {
      return hideFacades;
    }

    public @Nonnull ConduitDisplayMode getDisplayMode() {
      return displayMode;
    }

    void setHideFacades(boolean hideFacades) {
      this.hideFacades = hideFacades;
    }

    void setDisplayMode(@Nonnull ConduitDisplayMode displayMode) {
      this.displayMode = displayMode;
    }

    public boolean renderConduit(@Nullable Class<? extends IConduit> conduitType) {
      return displayMode.renderConduit(conduitType);
    }

    public boolean renderConduit(@Nonnull IConduit con) {
      return renderConduit(con.getBaseConduitType());
    }

  }

}
