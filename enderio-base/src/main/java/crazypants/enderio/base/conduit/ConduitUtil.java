package crazypants.enderio.base.conduit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.BlockEnder;
import com.enderio.core.common.util.DyeColor;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.conduit.IConduitBundle.FacadeRenderState;
import crazypants.enderio.base.conduit.registry.ConduitRegistry;
import crazypants.enderio.base.machine.modes.RedstoneControlMode;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.base.paint.YetaUtil;
import crazypants.enderio.base.sound.IModSound;
import crazypants.enderio.base.sound.SoundHelper;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static crazypants.enderio.base.init.ModObject.itemConduitProbe;

public class ConduitUtil {

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void ensureValidNetwork(IServerConduit conduit) {
    try {
      TileEntity te = conduit.getBundle().getEntity();
      World world = te.getWorld();
      Collection<? extends IServerConduit> connections = getConnectedConduits(world, te.getPos(),
          (Class<? extends IServerConduit>) conduit.getBaseConduitType()); // TODO:
                                                                           // this
                                                                           // won't
                                                                           // work

      if (reuseNetwork(conduit, connections, world)) {
        // Log.warn("Re-Using network at " + conduit.getBundle().getLocation() + " for " + conduit);
        return;
      }

      // Log.warn("Re-Building network at " + conduit.getBundle().getLocation() + " for " + conduit);
      IConduitNetwork res = conduit.createNetworkForType();
      res.init(conduit.getBundle(), connections, world);
    } catch (UnloadedBlockException e) {
      IConduitNetwork<?, ?> networkToDestroy = e.getNetworkToDestroy();
      if (networkToDestroy != null) {
        for (IConduit con : networkToDestroy.getConduits()) {
          // This is just to reduce server load by avoiding that all those conduits try to form a network one by one. It failed for one of them, it will fail
          // for all of them.
          if (con instanceof IServerConduit) {
            ((IServerConduit) con).setNetworkBuildFailed();
          }
        }
        networkToDestroy.destroyNetwork();
        // Log.warn("Failed building network at " + conduit.getBundle().getLocation() + " for " + conduit);
      }
    }
    return;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static boolean reuseNetwork(IServerConduit con, Collection<? extends IServerConduit> connections, @Nonnull World world) {
    IConduitNetwork network = null;
    for (IServerConduit conduit : connections) {
      if (network == null) {
        network = conduit.getNetwork();
      } else if (network != conduit.getNetwork()) {
        return false;
      }
    }
    if (network == null) {
      return false;
    }
    if (con.setNetwork(network)) {
      network.addConduit(con);
      return true;
    }
    return false;
  }

  /**
   * Disconnects a conduit from the network in a direction
   * 
   * @param con
   *          Conduit to disconnect as selected by the player
   * @param connDir
   *          Direction that is being disconnected
   * @param <T>
   *          Type of Conduit
   */
  public static <T extends IServerConduit> void disconnectConduits(@Nonnull T con, @Nonnull EnumFacing connDir) {
    con.conduitConnectionRemoved(connDir);
    BlockPos pos = con.getBundle().getLocation().offset(connDir);
    IConduit neighbour = ConduitUtil.getConduit(con.getBundle().getEntity().getWorld(), pos, con.getBaseConduitType());
    if (neighbour instanceof IServerConduit) {
      ((IServerConduit) neighbour).conduitConnectionRemoved(connDir.getOpposite());
      final IConduitNetwork<?, ?> neighbourNetwork = ((IServerConduit) neighbour).getNetwork();
      if (neighbourNetwork != null) {
        neighbourNetwork.destroyNetwork();
      }
    }
    final IConduitNetwork<?, ?> network = con.getNetwork();
    if (network != null) { // this should have been destroyed when
      // destroying the neighbour's network but
      // lets just make sure
      network.destroyNetwork();
    }
    con.connectionsChanged();
    if (neighbour instanceof IServerConduit) {
      ((IServerConduit) neighbour).connectionsChanged();
    }
  }

  /**
   * Connects two conduits together
   * 
   * @param con
   *          Conduit to connect
   * @param faceHit
   *          Direction the conduit is connecting to
   * @param <T>
   *          Type of Conduit
   * @return True if the conduit can be connected, false otherwise
   */

  public static <T extends IServerConduit> boolean connectConduits(@Nonnull T con, @Nonnull EnumFacing faceHit) {
    BlockPos pos = con.getBundle().getLocation().offset(faceHit);
    IConduit neighbour = ConduitUtil.getConduit(con.getBundle().getEntity().getWorld(), pos, con.getBaseConduitType());
    if (neighbour instanceof IServerConduit && con.canConnectToConduit(faceHit, neighbour)
        && ((IServerConduit) neighbour).canConnectToConduit(faceHit.getOpposite(), con)) {
      con.conduitConnectionAdded(faceHit);
      ((IServerConduit) neighbour).conduitConnectionAdded(faceHit.getOpposite());
      final IConduitNetwork<?, ?> network = con.getNetwork();
      if (network != null) {
        network.destroyNetwork();
      }
      final IConduitNetwork<?, ?> neighbourNetwork = ((IServerConduit) neighbour).getNetwork();
      if (neighbourNetwork != null) {
        neighbourNetwork.destroyNetwork();
      }
      con.connectionsChanged();
      ((IServerConduit) neighbour).connectionsChanged();
      return true;
    }
    return false;
  }

  public static boolean forceSkylightRecalculation(@Nonnull World world, int xCoord, int yCoord, int zCoord) {
    return forceSkylightRecalculation(world, new BlockPos(xCoord, yCoord, zCoord));
  }

  public static boolean forceSkylightRecalculation(@Nonnull World world, @Nonnull BlockPos pos) {
    int height = world.getHeight(pos).getY();
    if (height <= pos.getY()) {
      for (int i = 1; i < 12; i++) {
        final BlockPos offset = pos.offset(EnumFacing.UP, i);
        if (world.isAirBlock(offset)) {
          // We need to force the re-lighting of the column due to a change
          // in the light reaching below the block from the sky. To avoid
          // modifying core classes to expose this functionality I am just
          // placing then breaking
          // a block above this one to force the check

          world.setBlockState(offset, Blocks.STONE.getDefaultState(), 3);
          world.setBlockToAir(offset);

          return true;
        }
      }
    }
    return false;
  }

  @SideOnly(Side.CLIENT)
  public static FacadeRenderState getRequiredFacadeRenderState(@Nonnull IConduitBundle bundle, @Nonnull EntityPlayer player) {
    if (!bundle.hasFacade()) {
      return FacadeRenderState.NONE;
    }
    if (YetaUtil.isFacadeHidden(bundle, player)) {
      return FacadeRenderState.WIRE_FRAME;
    }
    return FacadeRenderState.FULL;
  }

  public static boolean isConduitEquipped(@Nullable EntityPlayer player) {
    return isConduitEquipped(player, EnumHand.MAIN_HAND);
  }

  public static boolean isConduitEquipped(@Nullable EntityPlayer player, @Nonnull EnumHand hand) {
    player = player == null ? EnderIO.proxy.getClientPlayer() : player;
    if (player == null) {
      return false;
    }
    ItemStack equipped = player.getHeldItem(hand);
    return equipped.getItem() instanceof IConduitItem;
  }

  public static boolean isProbeEquipped(@Nullable EntityPlayer player, @Nonnull EnumHand hand) {
    player = player == null ? EnderIO.proxy.getClientPlayer() : player;
    if (player == null) {
      return false;
    }
    ItemStack equipped = player.getHeldItem(hand);
    return equipped.getItem() == itemConduitProbe.getItemNN();
  }

  @Deprecated
  public static <T extends IConduit> T getConduit(@Nonnull World world, int x, int y, int z, @Nonnull Class<T> type) {
    return getConduit(world, new BlockPos(x, y, z), type);
  }

  public static <T extends IConduit> T getConduit(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Class<T> type) {
    IConduitBundle con = BlockEnder.getAnyTileEntitySafe(world, pos, IConduitBundle.class);
    if (con != null) {
      return con.getConduit(type);
    }
    return null;
  }

  public static <T extends IConduit> T getConduit(@Nonnull World world, @Nonnull TileEntity te, @Nonnull EnumFacing dir, @Nonnull Class<T> type) {
    return ConduitUtil.getConduit(world, te.getPos().offset(dir), type);
  }

  public static <T extends IServerConduit> Collection<T> getConnectedConduits(@Nonnull World world, int x, int y, int z, @Nonnull Class<T> type)
      throws UnloadedBlockException {
    return getConnectedConduits(world, new BlockPos(x, y, z), type);
  }

  public static <T extends IServerConduit> Collection<T> getConnectedConduits(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Class<T> type)
      throws UnloadedBlockException {
    IConduitBundle root = BlockEnder.getAnyTileEntitySafe(world, pos, IConduitBundle.class);
    if (root == null) {
      return Collections.emptyList();
    }
    List<T> result = new ArrayList<T>();
    T con = root.getConduit(type);
    if (con != null) {
      for (EnumFacing dir : con.getConduitConnections()) {
        if (dir != null) {
          if (!world.isBlockLoaded(pos.offset(dir))) {
            throw new UnloadedBlockException(con.getNetwork());
          }
          T connected = getConduit(world, root.getEntity(), dir, type);
          if (connected != null) {
            result.add(connected);
          }
        }
      }
    }
    return result;
  }

  public static class UnloadedBlockException extends Exception {

    private static final long serialVersionUID = 2130974035860715939L;
    private IConduitNetwork<?, ?> networkToDestroy;

    public IConduitNetwork<?, ?> getNetworkToDestroy() {
      return networkToDestroy;
    }

    public UnloadedBlockException(IConduitNetwork<?, ?> networkToDestroy) {
      this.networkToDestroy = networkToDestroy;
    }

  }

  public static void writeToNBT(IServerConduit conduit, @Nonnull NBTTagCompound conduitRoot) {
    if (conduit == null) {
      conduitRoot.setString("UUID", UUID.nameUUIDFromBytes("null".getBytes()).toString());
    } else {
      conduitRoot.setString("UUID", ConduitRegistry.get(conduit).getUUID().toString());
      conduit.writeToNBT(conduitRoot);
    }
  }

  public static IServerConduit readConduitFromNBT(@Nonnull NBTTagCompound conduitRoot) {
    if (conduitRoot.hasKey("UUID")) {
      String UUIDString = conduitRoot.getString("UUID");
      IServerConduit result = ConduitRegistry.getServerInstance(UUID.fromString(UUIDString));
      if (result != null) {
        result.readFromNBT(conduitRoot);
      }
      return result;
    }
    return null;
  }

  @SideOnly(Side.CLIENT)
  public static IClientConduit readClientConduitFromNBT(@Nonnull NBTTagCompound conduitRoot) {
    if (conduitRoot.hasKey("UUID")) {
      String UUIDString = conduitRoot.getString("UUID");
      IClientConduit result = ConduitRegistry.getClientInstance(UUID.fromString(UUIDString));
      if (result != null) {
        result.readFromNBT(conduitRoot);
      }
      return result;
    }
    return null;
  }

  @Deprecated
  public static boolean isRedstoneControlModeMet(@Nonnull IServerConduit conduit, @Nonnull RedstoneControlMode mode, @Nonnull DyeColor col) {
    return mode != RedstoneControlMode.NEVER;
  }

  public static boolean isRedstoneControlModeMet(@Nonnull IServerConduit conduit, @Nonnull RedstoneControlMode mode, @Nonnull DyeColor col,
      @Nonnull EnumFacing dir) {

    if (mode == RedstoneControlMode.IGNORE) {
      return true;
    } else if (mode == RedstoneControlMode.NEVER) {
      return false;
    }

    int signalStrength = conduit.getBundle().getInternalRedstoneSignalForColor(col, dir);
    if (signalStrength < RedstoneControlMode.MIN_ON_LEVEL && DyeColor.RED == col) {
      signalStrength = Math.max(signalStrength, conduit.getExternalRedstoneLevel());
    }
    return RedstoneControlMode.isConditionMet(mode, signalStrength);
  }

  public static int isBlockIndirectlyGettingPoweredIfLoaded(@Nonnull World world, @Nonnull BlockPos pos) {
    int i = 0;

    NNIterator<EnumFacing> iterator = NNList.FACING.iterator();
    while (iterator.hasNext()) {
      EnumFacing enumfacing = iterator.next();
      final BlockPos offset = pos.offset(enumfacing);
      if (world.isBlockLoaded(offset)) {
        int j = world.getRedstonePower(offset, enumfacing);

        if (j >= 15) {
          return 15;
        }

        if (j > i) {
          i = j;
        }
      }
    }

    return i;
  }

  public static boolean isFluidValid(FluidStack fluidStack) {
    if (fluidStack != null) {
      String name = FluidRegistry.getFluidName(fluidStack);
      if (name != null && !name.trim().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public static void openConduitGui(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
    openConduitGui(world, pos.getX(), pos.getY(), pos.getZ(), player);
  }

  public static void openConduitGui(@Nonnull World world, int x, int y, int z, @Nonnull EntityPlayer player) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (!(te instanceof IConduitBundle)) {
      return;
    }
    IConduitBundle cb = (IConduitBundle) te;
    Set<EnumFacing> cons = new HashSet<EnumFacing>();
    boolean conduitConnections = false;
    boolean hasInsulated = false;
    for (IClientConduit con : cb.getClientConduits()) {
      cons.addAll(con.getExternalConnections());
      if (ConduitRegistry.getNetwork(con).canConnectToAnything()) {
        hasInsulated = true;
      }
      conduitConnections = conduitConnections || con.hasConduitConnections();
    }
    if (cons.isEmpty() && !hasInsulated && !conduitConnections) {
      return;
    }
    if (cons.size() == 1) {
      EnumFacing facing = cons.iterator().next();
      if (facing != null) {
        PacketHandler.INSTANCE.sendToServer(new PacketOpenConduitUI(te, facing));
        return;
      }
    }
    ConduitRegistry.getConduitModObjectNN().openClientGui(world, new BlockPos(x, y, z), player, null, 0);
  }

  public static void playBreakSound(@Nonnull SoundType snd, @Nonnull World world, @Nonnull BlockPos pos) {
    SoundHelper.playSound(world, pos, new Sound(snd.getBreakSound()), (snd.getVolume() + 1.0F) / 2.0F, snd.getPitch() * 0.8F);
  }

  public static void playHitSound(@Nonnull SoundType snd, @Nonnull World world, @Nonnull BlockPos pos) {
    SoundHelper.playSound(world, pos, new Sound(snd.getHitSound()), (snd.getVolume() + 1.0F) / 2.0F, snd.getPitch() * 0.8F);
  }

  public static void playStepSound(@Nonnull SoundType snd, @Nonnull World world, @Nonnull BlockPos pos) {
    SoundHelper.playSound(world, pos, new Sound(snd.getStepSound()), (snd.getVolume() + 1.0F) / 2.0F, snd.getPitch() * 0.8F);
  }

  public static void playPlaceSound(@Nonnull SoundType snd, @Nonnull World world, @Nonnull BlockPos pos) {
    SoundHelper.playSound(world, pos, new Sound(snd.getPlaceSound()), (snd.getVolume() + 1.0F) / 2.0F, snd.getPitch() * 0.8F);
  }

  private static class Sound implements IModSound {

    private final @Nonnull SoundEvent event;

    public Sound(@Nonnull SoundEvent event) {
      this.event = event;
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public @Nonnull SoundEvent getSoundEvent() {
      return event;
    }

    @Override
    public @Nonnull SoundCategory getSoundCategory() {
      return SoundCategory.BLOCKS;
    }

  }
}
