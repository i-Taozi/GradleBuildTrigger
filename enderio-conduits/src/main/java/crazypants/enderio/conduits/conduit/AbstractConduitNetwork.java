package crazypants.enderio.conduits.conduit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.Log;
import crazypants.enderio.base.conduit.ConduitUtil;
import crazypants.enderio.base.conduit.ConduitUtil.UnloadedBlockException;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitNetwork;
import crazypants.enderio.base.conduit.IServerConduit;
import crazypants.enderio.base.diagnostics.ConduitNeighborUpdateTracker;
import crazypants.enderio.base.handler.ServerTickHandler;
import crazypants.enderio.util.Neighbours;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

//I=base type, I is the base class of the implementations accepted by the network 
public abstract class AbstractConduitNetwork<T extends IServerConduit, I extends T> implements IConduitNetwork<T, I> {

  private final @Nonnull NNList<I> conduits = new NNList<>();
  private long lastConduitListCheck = -1L; // server tick of the last time a full check on the conduit list was run. Used to limit the full check to once per
                                           // tick.

  protected final @Nonnull Class<I> implClass;
  protected final @Nonnull Class<T> baseConduitClass;

  protected AbstractConduitNetwork(@Nonnull Class<I> implClass, @Nonnull Class<T> baseConduitClass) {
    this.implClass = implClass;
    this.baseConduitClass = baseConduitClass;
  }

  @Override
  public void init(@Nonnull IConduitBundle tile, Collection<I> connections, @Nonnull World world) throws UnloadedBlockException {

    if (world.isRemote) {
      throw new UnsupportedOperationException();
    }

    // Destroy all existing networks around this block
    for (I con : connections) {
      IConduitNetwork<?, ?> network = con.getNetwork();
      if (network != null) {
        network.destroyNetwork();
      }
    }
    setNetwork(world, tile);
  }

  @Override
  public final @Nonnull Class<T> getBaseConduitType() {
    return baseConduitClass;
  }

  @Override
  public void setNetwork(@Nonnull World world, @Nonnull IConduitBundle tile) throws UnloadedBlockException {

    List<T> candidates = new LinkedList<>();
    candidates.add(tile.getConduit(getBaseConduitType()));

    while (!candidates.isEmpty()) {
      T conduit = candidates.remove(0);
      if (conduit == null || !implClass.isAssignableFrom(conduit.getClass())) {
        continue;
      }
      IConduitNetwork<?, ?> network = conduit.getNetwork();
      if (network == this) {
        continue;
      } else if (network != null) {
        network.destroyNetwork();
      }

      if (conduit.setNetwork(this)) {
        addConduit(implClass.cast(conduit));
        candidates.addAll(ConduitUtil.getConnectedConduits(world, conduit.getBundle().getEntity().getPos(), getBaseConduitType()));
      }

    }
  }

  private boolean isSameTick() {
    long temp = EnderIO.proxy.getServerTickCount();
    if (lastConduitListCheck != temp) {
      lastConduitListCheck = temp;
      return false;
    }
    return true;
  }

  @Override
  public void addConduit(@Nonnull I newConduit) {
    if (conduits.isEmpty()) {
      ServerTickHandler.addListener(this);
      ServerTickHandler.addListener(newConduit.getBundle().getBundleworld(), this);
    }
    boolean doFullCheck = !isSameTick();
    BlockPos newPos = null;
    boolean error = false;
    // Step 1: Is the new conduit attached to a TE that is valid?
    final IConduitBundle newBundle = newConduit.getBundle();
    final TileEntity newte = newBundle.getEntity();
    if (!newte.hasWorld()) {
      Log.info("Tried to add invalid (no world) conduit to network: ", newConduit);
      error = true;
    }
    if (newte.isInvalid()) {
      Log.info("Tried to add invalid (invalidated) conduit to network: ", newConduit);
      error = true;
    }
    newPos = newte.getPos();
    final World newworld = newte.getWorld();
    if (!newworld.isBlockLoaded(newPos)) {
      Log.info("Tried to add invalid (unloaded) conduit to network: ", newConduit);
      error = true;
    }
    if (newworld.getTileEntity(newte.getPos()) != newte) {
      Log.info("Tried to add invalid (world disagrees) conduit to network: ", newConduit);
      error = true;
    }
    if (error) {
      new Exception("trace for message above").printStackTrace();
      return;
    }
    // Step 2: Check for duplicates and other errors (short variant)
    if (!doFullCheck) {
      for (I oldConduit : conduits) {
        if (newConduit == oldConduit) {
          // real dupe, ignore it
          return;
        }
        if (oldConduit.getBundle().getEntity().getPos().equals(newPos)) {
          // Something fishy is happening, we need to do the full check
          doFullCheck = true;
          break;
        }
      }
      if (!doFullCheck) {
        conduits.add(newConduit);
        return;
      }
    }
    // Step 2: Check for duplicates and other errors (full variant)
    List<I> old = new ArrayList<I>(conduits);
    conduits.clear();
    boolean newConduitIsBad = false;
    for (I oldConduit : old) {
      // Step 2.1: Fast skip if we have a real dupe
      if (newConduit == oldConduit) {
        continue;
      }
      // Step 2.2: Check if the old conduit's TE is valid
      final IConduitBundle oldBundle = oldConduit.getBundle();
      final TileEntity oldTe = oldBundle.getEntity();
      if (oldTe.isInvalid() || !oldTe.hasWorld()) {
        oldConduit.clearNetwork();
        continue; // bad conduit, skip it
      }
      // Step 2.2b: Check if the target position is loaded
      final World oldWorld = oldBundle.getBundleworld();
      final BlockPos oldPos = oldTe.getPos();
      if (!oldWorld.isBlockLoaded(oldPos)) {
        Log.info("Removed unloaded but valid conduit from network: " + oldConduit);
        oldConduit.clearNetwork();
        continue; // bad conduit, skip it
      }
      // Step 2.3: Check if the old conduit's TE matches what its world has
      if (oldWorld.getTileEntity(oldPos) != oldTe) {
        oldConduit.clearNetwork();
        continue; // bad conduit, skip it
      }
      // Step 2.4: Check if the new conduit is for the same position as the old. This should not happen, as the new conduit should have been gotten from the
      // world and the old conduit already was checked against the world...
      if (newPos.equals(oldPos)) {
        Log.info("Tried to add invalid conduit to network! Old conduit: ", oldConduit, "/", oldBundle, " New conduit: ", newConduit, "/", oldBundle,
            " World says: ", oldWorld.getTileEntity(newPos));
        newConduitIsBad = true;
      }
      // Step 2.5: Old conduit is good and can stay
      conduits.add(oldConduit);
    }
    // Step 3: Add the new conduit
    if (!newConduitIsBad) {
      conduits.add(newConduit);
    }
  }

  @Override
  public void destroyNetwork() {
    for (I con : conduits) {
      con.clearNetwork();
    }
    conduits.clear();
    ServerTickHandler.removeListener(this);
  }

  @Override
  @Nonnull
  public NNList<I> getConduits() {
    return conduits;
  }

  private static final EnumFacing[] WEDUNS = new EnumFacing[] { EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH,
      EnumFacing.SOUTH };

  @Override
  public void sendBlockUpdatesForEntireNetwork() {
    ConduitNeighborUpdateTracker tracker = null;
    Set<BlockPos> notified = new HashSet<BlockPos>();
    for (I con : conduits) {
      TileEntity te = con.getBundle().getEntity();
      if (con.hasExternalConnections()) {
        final BlockPos pos = te.getPos();
        final Block blockType = te.getBlockType();
        final World world = te.getWorld();
        if (world.isBlockLoaded(pos)) {
          IBlockState bs = world.getBlockState(pos);
          if (tracker == null) {
            tracker = new ConduitNeighborUpdateTracker("Conduit network " + this.getClass() + " was interrupted while notifying neighbors of changes");
          }
          tracker.start("World.notifyBlockUpdate() at " + pos);
          world.notifyBlockUpdate(pos, bs, bs, 3);
          tracker.stop();

          // the following is a fancy version of world.notifyNeighborsOfStateChange(pos, blockType);

          // don't notify other conduits and don't notify the same block twice
          EnumSet<EnumFacing> sidesToNotify = EnumSet.noneOf(EnumFacing.class);
          Neighbours offset = new Neighbours(pos);
          for (EnumFacing side : WEDUNS) {
            offset.setOffset(side);
            if (con.containsExternalConnection(offset.getOffset()) && !notified.contains(offset) && world.isBlockLoaded(offset)) {
              IBlockState blockState = world.getBlockState(offset);
              if (blockState.getBlock() != blockType && blockState.getBlock() != Blocks.AIR) {
                sidesToNotify.add(side);
                notified.add(offset.toImmutable());
              }
            }
          }

          if (!sidesToNotify.isEmpty()) {
            tracker.start("ForgeEventFactory.onNeighborNotify() at " + pos);
            // TODO Set the 4th parameter to only update the redstone state when the conduit network has a redstone conduit network in it
            boolean canceled = ForgeEventFactory.onNeighborNotify(world, pos, bs, sidesToNotify, false).isCanceled();
            tracker.stop();

            if (!canceled) {
              for (EnumFacing side : sidesToNotify) {
                if (side != null) {
                  offset.setOffset(side);
                  tracker.start("World.notifyNeighborsOfStateChange() from " + pos + " to " + offset + " (" + world.getBlockState(offset) + ")");
                  world.neighborChanged(offset, blockType, pos);
                  tracker.stop();
                }
              }
            }
          }
        }
      }
    }
    if (tracker != null) {
      tracker.discard();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (IConduit con : conduits) {
      sb.append(con.getBundle().getLocation());
      sb.append(", ");
    }
    return "AbstractConduitNetwork@" + Integer.toHexString(hashCode()) + " [conduits=" + sb.toString() + "]";
  }

  @Override
  @Deprecated // use WorldTickEvent: tickStart(profiler)
  public void tickStart(ServerTickEvent event, @Nullable Profiler profiler) {
  }

  @Override
  @Deprecated // use WorldTickEvent: tickEnd(profiler)
  public void tickEnd(ServerTickEvent event, @Nullable Profiler profiler) {
  }

}
