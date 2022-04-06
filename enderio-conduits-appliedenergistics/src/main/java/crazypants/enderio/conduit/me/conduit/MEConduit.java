package crazypants.enderio.conduit.me.conduit;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.ITabPanel;
import com.enderio.core.common.vecmath.Vector4f;

import appeng.api.AEApi;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import crazypants.enderio.base.conduit.ConduitUtil;
import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitNetwork;
import crazypants.enderio.base.conduit.IConduitTexture;
import crazypants.enderio.base.conduit.IGuiExternalConnection;
import crazypants.enderio.base.conduit.RaytraceResult;
import crazypants.enderio.base.conduit.geom.CollidableComponent;
import crazypants.enderio.base.render.registry.TextureRegistry;
import crazypants.enderio.base.tool.ToolUtil;
import crazypants.enderio.conduit.me.gui.MESettings;
import crazypants.enderio.conduits.conduit.AbstractConduit;
import crazypants.enderio.conduits.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduits.conduit.TileConduitBundle;
import crazypants.enderio.conduits.render.ConduitTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static crazypants.enderio.conduit.me.init.ConduitAppliedEnergisticsObject.item_me_conduit;

public class MEConduit extends AbstractConduit implements IMEConduit {

  protected MEConduitNetwork network;
  protected MEConduitGrid grid;

  public static IConduitTexture coreTextureN = new ConduitTexture(TextureRegistry.registerTexture("blocks/me_conduit_core"), ConduitTexture.core());
  public static IConduitTexture coreTextureD = new ConduitTexture(TextureRegistry.registerTexture("blocks/me_conduit_core_dense"), ConduitTexture.core());
  public static IConduitTexture longTextureN = new ConduitTexture(TextureRegistry.registerTexture("blocks/me_conduit"), ConduitTexture.arm(0));
  public static IConduitTexture longTextureD = new ConduitTexture(TextureRegistry.registerTexture("blocks/me_conduit"), ConduitTexture.arm(1));

  private boolean isDense;
  private int playerID = -1;

  private IGridNode nodeR;

  public MEConduit() {
    this(0);
  }

  public MEConduit(int itemDamage) {
    isDense = itemDamage == 1;
  }

  @SideOnly(Side.CLIENT)
  public static void initIcons() {
  }

  public static int getDamageForState(boolean isDense) {
    return isDense ? 1 : 0;
  }

  @Override
  @Nonnull
  public Class<? extends IConduit> getBaseConduitType() {
    return IMEConduit.class;
  }

  @Override
  @Nonnull
  public ItemStack createItem() {
    return new ItemStack(item_me_conduit.getItemNN(), 1, getDamageForState(isDense));
  }

  @Override
  public AbstractConduitNetwork<?, ?> getNetwork() {
    return network;
  }

  @Override
  public boolean setNetwork(@Nonnull IConduitNetwork<?, ?> network) {
    this.network = (MEConduitNetwork) network;
    return super.setNetwork(network);
  }

  @Override
  public void writeToNBT(@Nonnull NBTTagCompound nbtRoot) {
    super.writeToNBT(nbtRoot);
    nbtRoot.setBoolean("isDense", isDense);
    nbtRoot.setInteger("playerID", playerID);
  }

  @Override
  public void readFromNBT(@Nonnull NBTTagCompound nbtRoot) {
    super.readFromNBT(nbtRoot);
    isDense = nbtRoot.getBoolean("isDense");
    if (nbtRoot.hasKey("playerID")) {
      playerID = nbtRoot.getInteger("playerID");
    } else {
      playerID = -1;
    }
  }

  public void setPlayerID(int playerID) {
    this.playerID = playerID;
  }

  @Override
  public int getChannelsInUse() {
    int channelsInUse = 0;
    IGridNode node = getNode();
    if (node != null) {
      for (IGridConnection gc : node.getConnections()) {
        channelsInUse = Math.max(channelsInUse, gc.getUsedChannels());
      }
    }
    return channelsInUse;
  }

  @Override
  @Method(modid = "appliedenergistics2")
  public boolean canConnectToExternal(@Nonnull EnumFacing dir, boolean ignoreDisabled) {
    World world = getBundle().getBundleworld();
    BlockPos pos = getBundle().getLocation().offset(dir);
    TileEntity te = world.getTileEntity(pos);

    if (te instanceof TileConduitBundle) {
      return false;
    }

    // because the AE2 API doesn't allow an easy query like "which side can connect to an ME cable" it needs this mess
    if (te instanceof IPartHost) {
      IPart part = ((IPartHost) te).getPart(dir.getOpposite());
      if (part == null) {
        part = ((IPartHost) te).getPart(AEPartLocation.INTERNAL);
        return part != null;
      }
      if (part.getExternalFacingNode() != null) {
        return true;
      }
      String name = part.getClass().getSimpleName();
      return "PartP2PTunnelME".equals(name) || "PartQuartzFiber".equals(name) || "PartToggleBus".equals(name) || "PartInvertedToggleBus".equals(name);
    } else if (te instanceof IGridHost) {
      IGridNode node = ((IGridHost) te).getGridNode(AEPartLocation.fromFacing(dir.getOpposite()));
      if (node == null) {
        node = ((IGridHost) te).getGridNode(AEPartLocation.INTERNAL);
      }
      if (node != null) {
        return node.getGridBlock().getConnectableSides().contains(dir.getOpposite());
      }
    }
    return false;
  }

  @Override
  public @Nonnull IConduitTexture getTextureForState(@Nonnull CollidableComponent component) {
    if (component.isCore()) {
      return (isDense ? coreTextureD : coreTextureN);
    } else {
      return (isDense ? longTextureD : longTextureN);
    }
  }

  @Override
  public @Nullable IConduitTexture getTransmitionTextureForState(@Nonnull CollidableComponent component) {
    return null;
  }

  @Override
  public @Nullable Vector4f getTransmitionTextureColorForState(@Nonnull CollidableComponent component) {
    return null;
  }

  @Override
  @Method(modid = "appliedenergistics2")
  public void updateEntity(@Nonnull World worldObj) {
    if (grid == null) {
      grid = new MEConduitGrid(this);
    }

    if (getNode() == null && !worldObj.isRemote) {
      IGridNode node = AEApi.instance().grid().createGridNode(grid);
      if (node != null) {
        node.setPlayerID(playerID);
        setGridNode(node);
        if (getNode() != null) {
          getNode().updateState();
        }
      }
    }

    super.updateEntity(worldObj);
  }

  @Override
  public @Nonnull ConnectionMode getNextConnectionMode(@Nonnull EnumFacing dir) {
    ConnectionMode mode = getConnectionMode(dir);
    mode = mode == ConnectionMode.IN_OUT ? ConnectionMode.DISABLED : ConnectionMode.IN_OUT;
    return mode;
  }

  @Override
  public @Nonnull ConnectionMode getPreviousConnectionMode(@Nonnull EnumFacing dir) {
    return getNextConnectionMode(dir);
  }

  @Override
  public boolean canConnectToConduit(@Nonnull EnumFacing direction, @Nonnull IConduit conduit) {
    if (!super.canConnectToConduit(direction, conduit)) {
      return false;
    }
    return conduit instanceof IMEConduit;
  }

  @Override
  @Method(modid = "appliedenergistics2")
  public void connectionsChanged() {
    super.connectionsChanged();
    BlockPos pos = getBundle().getLocation();
    onNodeChanged(pos);
    IGridNode node = getNode();
    if (node != null) {
      node.updateState();
      World world = node.getWorld();
      if (!world.isRemote && world instanceof WorldServer)
        ((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(pos);
    }
  }

  @Override
  public boolean onBlockActivated(@Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull RaytraceResult res, @Nonnull List<RaytraceResult> all) {
    if (ToolUtil.isToolEquipped(player, hand)) {
      if (!getBundle().getEntity().getWorld().isRemote) {
        final CollidableComponent component = res.component;
        if (component != null) {
          EnumFacing faceHit = res.movingObjectPosition.sideHit;
          if (component.isCore()) {
            if (getConnectionMode(faceHit) == ConnectionMode.DISABLED) {
              setConnectionMode(faceHit, ConnectionMode.IN_OUT);
              return true;
            }
            return ConduitUtil.connectConduits(this, faceHit);
          } else {
            EnumFacing connDir = component.getDirection();
            if (externalConnections.contains(connDir)) {
              setConnectionMode(connDir, getNextConnectionMode(connDir));
            } else if (containsConduitConnection(connDir)) {
              ConduitUtil.disconnectConduits(this, connDir);
            }
          }
        }
      }
      return true;
    }
    return false;
  }

  @Method(modid = "appliedenergistics2")
  private void onNodeChanged(@Nonnull BlockPos pos) {
    for (EnumFacing dir : EnumFacing.VALUES) {
      TileEntity te = getBundle().getEntity();
      if (te instanceof IGridHost && !(te instanceof IConduitBundle)) {
        IGridNode node = ((IGridHost) te).getGridNode(AEPartLocation.INTERNAL);
        if (node == null) {
          node = ((IGridHost) te).getGridNode(AEPartLocation.fromFacing(dir.getOpposite()));
        }
        if (node != null) {
          node.updateState();
        }
      }
    }
  }

  @Override
  public void onAddedToBundle() {
    for (EnumFacing dir : EnumFacing.VALUES) {
      TileEntity te = getBundle().getEntity();
      if (te instanceof TileConduitBundle) {
        IMEConduit cond = ((TileConduitBundle) te).getConduit(IMEConduit.class);
        if (cond != null) {
          cond.setConnectionMode(dir.getOpposite(), ConnectionMode.IN_OUT);
          ConduitUtil.connectConduits(cond, dir.getOpposite());
        }
      }
    }
  }

  @Override
  @Method(modid = "appliedenergistics2")
  public void onAfterRemovedFromBundle() {
    super.onAfterRemovedFromBundle();
    if (getNode() != null) {
      getNode().destroy();
    }
    setGridNode(null);
  }

  @Override
  @Method(modid = "appliedenergistics2")
  public void onChunkUnload() {
    super.onChunkUnload();
    if (getNode() != null) {
      getNode().destroy();
    }
    setGridNode(null);
  }

  @Override
  public MEConduitGrid getGrid() {
    return grid;
  }

  @Method(modid = "appliedenergistics2")
  private IGridNode getNode() {
    return getGridNode();
  }

  @Override
  public EnumSet<EnumFacing> getConnections() {
    EnumSet<EnumFacing> cons = EnumSet.noneOf(EnumFacing.class);
    cons.addAll(getConduitConnections());
    for (EnumFacing dir : getExternalConnections()) {
      if (dir != null && getConnectionMode(dir) != ConnectionMode.DISABLED) {
        cons.add(dir);
      }
    }
    return cons;
  }

  @Override
  public boolean isDense() {
    return isDense;
  }

  @Override
  public @Nonnull MEConduitNetwork createNetworkForType() {
    return new MEConduitNetwork();
  }

  @SideOnly(Side.CLIENT)
  @Override
  @Nonnull
  public ITabPanel createGuiPanel(@Nonnull IGuiExternalConnection gui, @Nonnull IClientConduit con) {
    return new MESettings(gui, con);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public int getGuiPanelTabOrder() {
    return 6;
  }

  private void setGridNode(IGridNode node) {
    this.nodeR = node;
  }

  @Override
  public void clearNetwork() {
    this.network = null;
  }

  @Override
  public boolean updateGuiPanel(@Nonnull ITabPanel panel) {
    if (panel instanceof MESettings) {
      return ((MESettings) panel).updateConduit(this);
    }
    return false;
  }

  @Override
  public IGridNode getGridNode() {
    return this.nodeR;
  }

}
