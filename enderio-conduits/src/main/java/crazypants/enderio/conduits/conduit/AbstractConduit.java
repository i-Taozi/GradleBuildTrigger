package crazypants.enderio.conduits.conduit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;
import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.api.ILocalizable;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.conduit.ConduitUtil;
import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitNetwork;
import crazypants.enderio.base.conduit.IServerConduit;
import crazypants.enderio.base.conduit.RaytraceResult;
import crazypants.enderio.base.conduit.geom.CollidableCache;
import crazypants.enderio.base.conduit.geom.CollidableCache.CacheKey;
import crazypants.enderio.base.conduit.geom.CollidableComponent;
import crazypants.enderio.base.conduit.geom.ConduitGeometryUtil;
import crazypants.enderio.base.conduit.registry.ConduitRegistry;
import crazypants.enderio.base.diagnostics.Prof;
import crazypants.enderio.base.machine.interfaces.INotifier;
import crazypants.enderio.conduits.lang.Lang;
import crazypants.enderio.conduits.render.BlockStateWrapperConduitBundle;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractConduit implements IServerConduit, IClientConduit.WithDefaultRendering, IConduitComponent, INotifier {

  protected final @Nonnull Set<EnumFacing> conduitConnections = EnumSet.noneOf(EnumFacing.class);

  protected final @Nonnull Set<EnumFacing> externalConnections = EnumSet.noneOf(EnumFacing.class);

  public static final float TRANSMISSION_SCALE = 0.3f;

  // NB: This is a transient field controlled by the owning bundle. It is not
  // written to the NBT etc
  protected @Nullable IConduitBundle bundle;

  protected boolean active;

  protected List<CollidableComponent> collidables;

  protected final @Nonnull EnumMap<EnumFacing, ConnectionMode> conectionModes = new EnumMap<EnumFacing, ConnectionMode>(EnumFacing.class);

  protected boolean collidablesDirty = true;

  private boolean clientStateDirty = true;

  private boolean dodgyChangeSinceLastCallFlagForBundle = true;

  protected boolean connectionsDirty = true;

  protected boolean readFromNbt = false;

  private Integer lastExternalRedstoneLevel = null;

  /**
   * Client-only value. The server sends this depending on {@link #getNetwork()}. If false, the conduit will render in an error state. Initialized as
   * <code>true</code> because most conduits will have no issue to form a network.
   */
  private boolean hasNetwork = true;

  protected AbstractConduit() {
  }

  @Override
  public boolean writeConnectionSettingsToNBT(@Nonnull EnumFacing dir, @Nonnull NBTTagCompound nbt) {
    if (!getExternalConnections().contains(dir)) {
      return false;
    }
    NBTTagCompound dataRoot = getNbtRootForType(nbt, true);
    dataRoot.setShort("connectionMode", (short) getConnectionMode(dir).ordinal());
    writeTypeSettingsToNbt(dir, dataRoot);
    return true;
  }

  @Override
  public boolean readConduitSettingsFromNBT(@Nonnull EnumFacing dir, @Nonnull NBTTagCompound nbt) {
    if (!getExternalConnections().contains(dir) && !canConnectToExternal(dir, true)) {
      return false;
    }
    NBTTagCompound dataRoot = getNbtRootForType(nbt, false);
    if (dataRoot == null) {
      return false;
    }
    if (dataRoot.hasKey("connectionMode")) {
      ConnectionMode mode = NullHelper.first(ConnectionMode.values()[dataRoot.getShort("connectionMode")], getDefaultConnectionMode());
      setConnectionMode(dir, mode);
    }
    readTypeSettings(dir, dataRoot);
    getBundle().dirty();
    return true;
  }

  protected void readTypeSettings(@Nonnull EnumFacing dir, @Nonnull NBTTagCompound dataRoot) {
  }

  protected void writeTypeSettingsToNbt(@Nonnull EnumFacing dir, @Nonnull NBTTagCompound dataRoot) {
  }

  protected NBTTagCompound getNbtRootForType(@Nonnull NBTTagCompound nbt, boolean createIfNull) {
    Class<? extends IConduit> bt = getBaseConduitType();
    String dataRootName = NullHelper.notnullJ(bt.getSimpleName(), "Class#getSimpleName");
    NBTTagCompound dataRoot = null;
    if (nbt.hasKey(dataRootName)) {
      dataRoot = nbt.getCompoundTag(dataRootName);
    }
    if (dataRoot == null && createIfNull) {
      dataRoot = new NBTTagCompound();
      nbt.setTag(dataRootName, dataRoot);
    }
    return dataRoot;
  }

  @Override
  @Nonnull
  public ConnectionMode getConnectionMode(@Nonnull EnumFacing dir) {
    ConnectionMode res = conectionModes.get(dir);
    if (res == null) {
      return getDefaultConnectionMode();
    }
    return res;
  }

  @Nonnull
  protected ConnectionMode getDefaultConnectionMode() {
    return ConnectionMode.IN_OUT;
  }

  @Override
  public void setConnectionMode(@Nonnull EnumFacing dir, @Nonnull ConnectionMode mode) {
    ConnectionMode oldVal = conectionModes.get(dir);
    if (oldVal == mode) {
      return;
    }
    if (mode == getDefaultConnectionMode()) {
      conectionModes.remove(dir);
    } else {
      conectionModes.put(dir, mode);
    }

    connectionsChanged();
  }

  @Override
  public boolean supportsConnectionMode(@Nonnull ConnectionMode mode) {
    if (mode == getDefaultConnectionMode() && conectionModes.size() != 6) {
      return true;
    }
    for (ConnectionMode cm : conectionModes.values()) {
      if (cm == mode) {
        return true;
      }
    }
    return false;
  }

  @Override
  @Nonnull
  public ConnectionMode getNextConnectionMode(@Nonnull EnumFacing dir) {
    ConnectionMode curMode = getConnectionMode(dir);
    ConnectionMode next = ConnectionMode.getNext(curMode);
    if (next == ConnectionMode.NOT_SET) {
      next = ConnectionMode.IN_OUT;
    }
    return next;
  }

  @Override
  @Nonnull
  public ConnectionMode getPreviousConnectionMode(@Nonnull EnumFacing dir) {
    ConnectionMode curMode = getConnectionMode(dir);
    ConnectionMode prev = ConnectionMode.getPrevious(curMode);
    if (prev == ConnectionMode.NOT_SET) {
      prev = ConnectionMode.DISABLED;
    }
    return prev;
  }

  @Override
  public boolean haveCollidablesChangedSinceLastCall() {
    if (dodgyChangeSinceLastCallFlagForBundle) {
      dodgyChangeSinceLastCallFlagForBundle = false;
      return true;
    }
    return false;
  }

  @Override
  public void setBundle(@Nullable IConduitBundle tileConduitBundle) {
    bundle = tileConduitBundle;
  }

  @Override
  @Nonnull
  public IConduitBundle getBundle() {
    return NullHelper.notnull(bundle, "Logic error in conduit---no bundle set");
  }

  // Connections
  @Override
  @Nonnull
  public Set<EnumFacing> getConduitConnections() {
    return conduitConnections;
  }

  @Override
  public boolean containsConduitConnection(@Nonnull EnumFacing dir) {
    return conduitConnections.contains(dir);
  }

  @Override
  public void conduitConnectionAdded(@Nonnull EnumFacing fromDirection) {
    conduitConnections.add(fromDirection);
  }

  @Override
  public void conduitConnectionRemoved(@Nonnull EnumFacing fromDirection) {
    conduitConnections.remove(fromDirection);
  }

  @Override
  public boolean canConnectToConduit(@Nonnull EnumFacing direction, @Nonnull IConduit conduit) {
    return getConnectionMode(direction) != ConnectionMode.DISABLED && conduit.getConnectionMode(direction.getOpposite()) != ConnectionMode.DISABLED;
  }

  @Override
  public boolean canConnectToExternal(@Nonnull EnumFacing direction, boolean ignoreConnectionMode) {
    return false;
  }

  @Override
  @Nonnull
  public Set<EnumFacing> getExternalConnections() {
    return externalConnections;
  }

  @Override
  public boolean hasExternalConnections() {
    return !externalConnections.isEmpty();
  }

  @Override
  public boolean hasConduitConnections() {
    return !conduitConnections.isEmpty();
  }

  @Override
  public boolean containsExternalConnection(@Nonnull EnumFacing dir) {
    return externalConnections.contains(dir);
  }

  @Override
  public void externalConnectionAdded(@Nonnull EnumFacing fromDirection) {
    externalConnections.add(fromDirection);
  }

  @Override
  public void externalConnectionRemoved(@Nonnull EnumFacing fromDirection) {
    externalConnections.remove(fromDirection);
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void setActive(boolean active) {
    if (active != this.active) {
      clientStateDirty = true;
    }
    this.active = active;
  }

  @Override
  public void writeToNBT(@Nonnull NBTTagCompound conduitBody) {
    int[] dirs = new int[conduitConnections.size()];
    Iterator<EnumFacing> cons = conduitConnections.iterator();
    for (int i = 0; i < dirs.length; i++) {
      dirs[i] = cons.next().ordinal();
    }
    conduitBody.setIntArray("connections", dirs);

    dirs = new int[externalConnections.size()];
    cons = externalConnections.iterator();
    for (int i = 0; i < dirs.length; i++) {
      dirs[i] = cons.next().ordinal();
    }
    conduitBody.setIntArray("externalConnections", dirs);
    conduitBody.setBoolean("signalActive", active);

    if (conectionModes.size() > 0) {
      byte[] modes = new byte[6];
      int i = 0;
      for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
        modes[i] = (byte) getConnectionMode(itr.next()).ordinal();
        i++;
      }
      conduitBody.setByteArray("conModes", modes);
    }

    // Note: Don't tell the client that there's no network if we didn't actually try to form one yet
    conduitBody.setBoolean("hasNetwork", getNetwork() != null || nextNetworkTry == -1L);
  }

  @Override
  public void readFromNBT(@Nonnull NBTTagCompound conduitBody) {
    conduitConnections.clear();
    int[] dirs = conduitBody.getIntArray("connections");
    for (int i = 0; i < dirs.length; i++) {
      conduitConnections.add(EnumFacing.values()[dirs[i]]);
    }

    externalConnections.clear();
    dirs = conduitBody.getIntArray("externalConnections");
    for (int i = 0; i < dirs.length; i++) {
      externalConnections.add(EnumFacing.values()[dirs[i]]);
    }
    active = conduitBody.getBoolean("signalActive");

    conectionModes.clear();
    byte[] modes = conduitBody.getByteArray("conModes");
    if (modes.length == 6) {
      int i = 0;
      for (EnumFacing dir : EnumFacing.VALUES) {
        conectionModes.put(dir, ConnectionMode.values()[modes[i]]);
        i++;
      }
    }

    hasNetwork = conduitBody.getBoolean("hasNetwork");

    readFromNbt = true;
  }

  @Override
  public int getLightValue() {
    return 0;
  }

  @Override
  public boolean onBlockActivated(@Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull RaytraceResult res, @Nonnull List<RaytraceResult> all) {
    return false;
  }

  @Override
  public float getSelfIlluminationForState(@Nonnull CollidableComponent component) {
    return isActive() ? 1 : 0;
  }

  @Override
  public float getTransmitionGeometryScale() {
    return TRANSMISSION_SCALE;
  }

  @Override
  public void onChunkUnload() {
    IConduitNetwork<?, ?> network = getNetwork();
    if (network != null) {
      network.destroyNetwork();
    }
  }

  @Override
  public void updateEntity(@Nonnull World world) {
    if (world.isRemote) {
      return;
    }
    Prof.start(world, "updateNetwork");
    updateNetwork(world);
    Prof.next(world, "updateConnections");
    updateConnections();
    readFromNbt = false; // the two update*()s react to this on their first run
    if (clientStateDirty) {
      getBundle().dirty();
      clientStateDirty = false;
    }
    Prof.stop(world);
  }

  private void updateConnections() {
    if (!connectionsDirty && !readFromNbt) {
      return;
    }

    boolean externalConnectionsChanged = false;
    NNList<EnumFacing> copy = new NNList<EnumFacing>(externalConnections);
    // remove any no longer valid connections
    for (NNIterator<EnumFacing> itr = copy.fastIterator(); itr.hasNext();) {
      EnumFacing dir = itr.next();
      if (!canConnectToExternal(dir, false) || readFromNbt) {
        externalConnectionRemoved(dir);
        externalConnectionsChanged = true;
      }
    }

    // then check for new ones
    for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
      EnumFacing dir = itr.next();
      if (!conduitConnections.contains(dir) && !externalConnections.contains(dir)) {
        if (canConnectToExternal(dir, false)) {
          externalConnectionAdded(dir);
          externalConnectionsChanged = true;
        }
      }
    }
    if (externalConnectionsChanged) {
      connectionsChanged();
    }

    connectionsDirty = false;
  }

  @Override
  public void connectionsChanged() {
    collidablesDirty = true;
    clientStateDirty = true;
    dodgyChangeSinceLastCallFlagForBundle = true;
  }

  protected void setClientStateDirty() {
    clientStateDirty = true;
  }

  private long nextNetworkTry = -1L;

  protected void updateNetwork(World world) {
    long tickCount = EnderIO.proxy.getServerTickCount();
    if (tickCount < nextNetworkTry && getNetwork() == null) {
      return;
    }
    if (getNetwork() == null) {
      BlockPos pos = getBundle().getLocation();
      if (world.isBlockLoaded(pos)) {
        ConduitUtil.ensureValidNetwork(this);
        IConduitNetwork<?, ?> network = getNetwork();
        if (network != null) {
          nextNetworkTry = -1L;
          network.sendBlockUpdatesForEntireNetwork();
          if (readFromNbt) {
            connectionsChanged();
          }
        } else {
          setNetworkBuildFailed();
        }
      }
    } else if (nextNetworkTry > -1L) {
      nextNetworkTry = -1L;
      setClientStateDirty();
    }
  }

  @Override
  public void setNetworkBuildFailed() {
    if (nextNetworkTry == -1L) {
      setClientStateDirty();
    }
    nextNetworkTry = EnderIO.proxy.getServerTickCount() + 200 + (long) (Math.random() * 100);
  }

  @Override
  @Nonnull
  public Set<? extends ILocalizable> getNotification() {
    Set<ILocalizable> result = new HashSet<>();
    if (nextNetworkTry > -1L) {
      result.add(new ILocalizable() {
        @Override
        @Nonnull
        public String getUnlocalizedName() {
          return Lang.GUI_NETWORK_PARTIALLY_UNLOADED.getKey();
        }
      });
    }
    return result;
  }

  @Override
  @Nonnull
  public NNList<ITextComponent> getConduitProbeInformation(@Nonnull EntityPlayer player) {
    NNList<ITextComponent> result = new NNList<>();
    if (nextNetworkTry > -1L) {
      result.add(Lang.GUI_NETWORK_PARTIALLY_UNLOADED.toChatServer().setStyle(new Style().setColor(TextFormatting.RED)));
    }
    return result;
  }

  @Override
  public boolean setNetwork(@Nonnull IConduitNetwork<?, ?> network) {
    return true;
  }

  @Override
  public void onAddedToBundle() {

    TileEntity te = getBundle().getEntity();
    World world = te.getWorld();

    conduitConnections.clear();
    for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
      EnumFacing dir = itr.next();
      IConduit neighbour = ConduitUtil.getConduit(world, te, dir, getBaseConduitType());
      if (neighbour instanceof IServerConduit && ((IServerConduit) neighbour).canConnectToConduit(dir.getOpposite(), this)
          && canConnectToConduit(dir, neighbour)) {
        conduitConnections.add(dir);
        ((IServerConduit) neighbour).conduitConnectionAdded(dir.getOpposite());
        ((IServerConduit) neighbour).connectionsChanged();
      }
    }

    externalConnections.clear();
    for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
      EnumFacing dir = itr.next();
      if (!containsConduitConnection(dir) && canConnectToExternal(dir, false)) {
        externalConnectionAdded(dir);
      }
    }

    connectionsChanged();
  }

  @Override
  public void onAfterRemovedFromBundle() {
    TileEntity te = getBundle().getEntity();
    World world = te.getWorld();

    for (EnumFacing dir : conduitConnections) {
      if (dir != null) {
        IConduit neighbour = ConduitUtil.getConduit(world, te, dir, getBaseConduitType());
        if (neighbour instanceof IServerConduit) {
          ((IServerConduit) neighbour).conduitConnectionRemoved(dir.getOpposite());
          ((IServerConduit) neighbour).connectionsChanged();
        }
      }
    }
    conduitConnections.clear();

    if (!externalConnections.isEmpty()) {
      world.notifyNeighborsOfStateChange(te.getPos(), te.getBlockType(), true);
    }
    externalConnections.clear();

    IConduitNetwork<?, ?> network = getNetwork();
    if (network != null) {
      network.destroyNetwork();
    }
    connectionsChanged();
  }

  @Override
  public boolean onNeighborBlockChange(@Nonnull Block block) {

    // NB: No need to check externals if the neighbour that changed was a
    // conduit bundle as this
    // can't effect external connections.
    if (block == ConduitRegistry.getConduitModObjectNN().getBlock()) {
      return false;
    }

    lastExternalRedstoneLevel = null;

    // Check for changes to external connections, connections to conduits are
    // handled by the bundle
    Set<EnumFacing> newCons = EnumSet.noneOf(EnumFacing.class);
    for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
      EnumFacing dir = itr.next();
      if (!containsConduitConnection(dir) && canConnectToExternal(dir, false)) {
        newCons.add(dir);
      }
    }
    if (newCons.size() != externalConnections.size()) {
      connectionsDirty = true;
      return true;
    }
    for (EnumFacing dir : externalConnections) {
      if (!newCons.remove(dir)) {
        connectionsDirty = true;
        return true;
      }
    }
    if (!newCons.isEmpty()) {
      connectionsDirty = true;
      return true;
    }
    return false;
  }

  @Override
  public boolean onNeighborChange(@Nonnull BlockPos neighbourPos) {
    return false;
  }

  @Override
  @Nonnull
  public Collection<CollidableComponent> createCollidables(@Nonnull CacheKey key) {
    return NullHelper.notnullJ(Collections.singletonList(new CollidableComponent(getCollidableType(),
        ConduitGeometryUtil.getInstance().getBoundingBox(getBaseConduitType(), key.dir, key.offset), key.dir, null)), "Collections#singletonList");
  }

  @Override
  @Nonnull
  public Class<? extends IConduit> getCollidableType() {
    return getBaseConduitType();
  }

  @Override
  @Nonnull
  public List<CollidableComponent> getCollidableComponents() {

    if (collidables != null && !collidablesDirty) {
      return collidables;
    }

    List<CollidableComponent> result = new ArrayList<CollidableComponent>();
    for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
      EnumFacing dir = itr.next();
      Collection<CollidableComponent> col = getCollidables(dir);
      if (col != null) {
        result.addAll(col);
      }
    }
    collidables = result;

    collidablesDirty = false;

    return result;
  }

  private Collection<CollidableComponent> getCollidables(@Nonnull EnumFacing dir) {
    CollidableCache cc = CollidableCache.instance;
    Class<? extends IConduit> type = getCollidableType();
    if (isConnectedTo(dir) && getConnectionMode(dir) != ConnectionMode.DISABLED) {
      return cc.getCollidables(cc.createKey(type, getBundle().getOffset(getBaseConduitType(), dir), dir), this);
    }
    return null;
  }

  @Override
  public boolean shouldMirrorTexture() {
    return true;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void hashCodeForModelCaching(BlockStateWrapperConduitBundle.ConduitCacheKey hashCodes) {
    hashCodes.add(this.getClass());
    hashCodes.add(conduitConnections, externalConnections, conectionModes);
    hashCodes.add(hasNetwork);
  }

  @Override
  public void invalidate() {
    // TODO: 1.13: Make abstract unless something goes in here
  }

  @Override
  public int getExternalRedstoneLevel() {
    if (lastExternalRedstoneLevel == null) {
      if (bundle == null) {
        return 0;
      }
      TileEntity te = getBundle().getEntity();
      lastExternalRedstoneLevel = ConduitUtil.isBlockIndirectlyGettingPoweredIfLoaded(te.getWorld(), te.getPos());
    }
    return lastExternalRedstoneLevel;
  }

  @Override
  public boolean renderError() {
    return !hasNetwork;
  }

  @Override
  public String toString() {
    return "AbstractConduit [getClass()=" + getClass() + ", lastExternalRedstoneLevel=" + lastExternalRedstoneLevel + ", getConduitConnections()="
        + getConduitConnections() + ", getExternalConnections()=" + getExternalConnections() + ", getNetwork()=" + getNetwork() + "]";
  }

}
