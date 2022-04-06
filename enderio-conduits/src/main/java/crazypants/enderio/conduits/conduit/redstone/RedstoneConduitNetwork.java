package crazypants.enderio.conduits.conduit.redstone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;
import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.base.conduit.ConduitUtil.UnloadedBlockException;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.redstone.signals.BundledSignal;
import crazypants.enderio.base.conduit.redstone.signals.CombinedSignal;
import crazypants.enderio.base.conduit.redstone.signals.Signal;
import crazypants.enderio.base.conduit.registry.ConduitRegistry;
import crazypants.enderio.base.diagnostics.Prof;
import crazypants.enderio.base.filter.redstone.IInputSignalFilter;
import crazypants.enderio.conduits.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduits.config.ConduitConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class RedstoneConduitNetwork extends AbstractConduitNetwork<IRedstoneConduit, IRedstoneConduit> {

  private final @Nonnull BundledSignal bundledSignal = new BundledSignal();

  int updatingNetwork = 0;

  private int baseId = 0;

  public RedstoneConduitNetwork() {
    super(IRedstoneConduit.class, IRedstoneConduit.class);
  }

  @Override
  public void init(@Nonnull IConduitBundle tile, Collection<IRedstoneConduit> connections, @Nonnull World world) throws UnloadedBlockException {
    super.init(tile, connections, world);
    updatingNetwork++;
    notifyNeigborsOfSignalUpdate();
    updatingNetwork--;
  }

  @Override
  public void destroyNetwork() {
    updatingNetwork++;
    for (IRedstoneConduit con : getConduits()) {
      con.setActive(false);
    }
    // Notify neighbours that all signals have been lost
    bundledSignal.clear();
    notifyNeigborsOfSignalUpdate();
    updatingNetwork--;
    super.destroyNetwork();
  }

  @Override
  public void addConduit(@Nonnull IRedstoneConduit con) {
    super.addConduit(con);
    con.setSignalIdBase(baseId);
    baseId += 6;
    updateInputsFromConduit(con, true); // all call paths to here come from updateNetwork() which already notifies all neighbors
  }

  public void updateInputsFromConduit(@Nonnull IRedstoneConduit con, boolean delayUpdate) {
    // Make my neighbors update as if we have no signals
    updatingNetwork++;
    notifyConduitNeighbours(con);
    updatingNetwork--;

    // Then ask them what inputs they have now
    for (EnumFacing side : EnumFacing.values()) {
      if (side != null && con.getConnectionMode(side).acceptsOutput()) {
        updateInputsForSource(con, side);
      }
    }

    if (!delayUpdate) {
      // then tell the whole network about the change
      notifyNeigborsOfSignalUpdate();
    }

    if (ConduitConfig.showState.get()) {
      updateActiveState();
    }
  }

  private void updateActiveState() {
    boolean isActive = false;
    for (CombinedSignal s : bundledSignal.getSignals()) {
      if (s.getStrength() > 0) {
        isActive = true;
        break;
      }
    }
    for (IRedstoneConduit con : getConduits()) {
      con.setActive(isActive);
    }
  }

  private void updateInputsForSource(@Nonnull IRedstoneConduit con, @Nonnull EnumFacing dir) {
    updatingNetwork++;
    Signal signal = con.getNetworkInput(dir);
    bundledSignal.addSignal(con.getInputSignalColor(dir), signal);

    // if (Loader.isModLoaded("computercraft")) {
    // Map<DyeColor, Signal> ccSignals = con.getComputerCraftSignals(dir);
    //
    // if (!ccSignals.isEmpty()) {
    // for (DyeColor color : ccSignals.keySet()) {
    // Signal ccSig = ccSignals.get(color);
    // bundledSignal.addSignal(color, ccSig);
    // }
    // }
    // }

    updatingNetwork--;
  }

  public @Nonnull BundledSignal getBundledSignal() {
    return bundledSignal;
  }

  // Need to disable the network when determining the strength of external
  // signals
  // to avoid feed back looops
  void setNetworkEnabled(boolean enabled) {
    updatingNetwork += enabled ? -1 : 1;
  }

  public boolean isNetworkEnabled() {
    return updatingNetwork == 0;
  }

  @Override
  public String toString() {
    return "RedstoneConduitNetwork [signals=" + signalsString() + ", conduits=" + conduitsString() + "]";
  }

  private String conduitsString() {
    StringBuilder sb = new StringBuilder();
    for (IRedstoneConduit con : getConduits()) {
      TileEntity te = con.getBundle().getEntity();
      sb.append("<").append(te.getPos().getX()).append(",").append(te.getPos().getY()).append(",").append(te.getPos().getZ()).append(">");
    }
    return sb.toString();
  }

  String signalsString() {
    StringBuilder sb = new StringBuilder();
    for (CombinedSignal s : bundledSignal.getSignals()) {
      sb.append("<");
      sb.append(s);
      sb.append(">");

    }
    return sb.toString();
  }

  public void notifyNeigborsOfSignalUpdate() {
    for (IRedstoneConduit con : new ArrayList<IRedstoneConduit>(getConduits())) {
      notifyConduitNeighbours(con);
    }
  }

  private void notifyConduitNeighbours(@Nonnull IRedstoneConduit con) {
    TileEntity te = con.getBundle().getEntity();

    World world = te.getWorld();

    BlockPos bc1 = te.getPos();

    if (!world.isBlockLoaded(bc1)) {
      return;
    }

    // Done manually to avoid orphaning chunks
    EnumSet<EnumFacing> cons = EnumSet.copyOf(con.getExternalConnections());
    if (!neighborNotifyEvent(world, bc1, null, cons)) {
      for (EnumFacing dir : con.getExternalConnections()) {
        BlockPos bc2 = bc1.offset(NullHelper.notnull(dir, "Conduit external connections contains null"));
        if (world.isBlockLoaded(bc2)) {
          world.neighborChanged(bc2, ConduitRegistry.getConduitModObjectNN().getBlockNN(), bc1);
          IBlockState bs = world.getBlockState(bc2);
          if (bs.isBlockNormalCube() && !neighborNotifyEvent(world, bc2, bs, EnumSet.allOf(EnumFacing.class))) {
            for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
              EnumFacing dir2 = itr.next();
              BlockPos bc3 = bc2.offset(dir2);
              if (!bc3.equals(bc1) && world.isBlockLoaded(bc3)) {
                world.neighborChanged(bc3, ConduitRegistry.getConduitModObjectNN().getBlockNN(), bc1);
              }
            }
          }
        }
      }
    }
  }

  private boolean neighborNotifyEvent(World world, @Nonnull BlockPos pos, @Nullable IBlockState state, EnumSet<EnumFacing> dirs) {
    return ForgeEventFactory.onNeighborNotify(world, pos, state == null ? world.getBlockState(pos) : state, dirs, false).isCanceled();
  }

  @Override
  public void tickEnd(@Nullable Profiler profiler) {
    Prof.start(profiler, "checkTickingFilters");
    String oldSignals = null;
    for (IRedstoneConduit con : getConduits()) {
      for (EnumFacing dir : EnumFacing.VALUES) {
        if (dir != null && ((IInputSignalFilter) con.getSignalFilter(dir, false)).shouldUpdate()) {
          if (oldSignals == null) {
            oldSignals = signalsString();
          }
          updateInputsFromConduit(con, true);
          break; // updateInputsFromConduit does all sides
        }
      }
    }
    if (oldSignals != null && !oldSignals.equals(signalsString())) {
      Prof.next(profiler, "notifyNeighborsForTickingFilters");
      notifyNeigborsOfSignalUpdate();
    }
    Prof.stop(profiler);
  }

}
