package crazypants.enderio.invpanel.conduit.data;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.ITabPanel;
import com.enderio.core.common.BlockEnder;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.vecmath.Vector4f;

import crazypants.enderio.base.conduit.ConduitUtil;
import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitNetwork;
import crazypants.enderio.base.conduit.IConduitTexture;
import crazypants.enderio.base.conduit.IGuiExternalConnection;
import crazypants.enderio.base.conduit.RaytraceResult;
import crazypants.enderio.base.conduit.geom.CollidableComponent;
import crazypants.enderio.base.invpanel.capability.CapabilityDatabaseHandler;
import crazypants.enderio.base.invpanel.capability.InventoryDatabaseSource;
import crazypants.enderio.base.render.registry.TextureRegistry;
import crazypants.enderio.base.tool.ToolUtil;
import crazypants.enderio.conduits.conduit.AbstractConduit;
import crazypants.enderio.conduits.render.ConduitTexture;
import crazypants.enderio.invpanel.init.InvpanelObject;
import crazypants.enderio.invpanel.invpanel.TileInventoryPanel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DataConduit extends AbstractConduit implements IDataConduit {

  static final @Nonnull Map<String, IConduitTexture> ICONS = new HashMap<>();

  static {
    ICONS.put(ICON_KEY, new ConduitTexture(TextureRegistry.registerTexture(ICON_KEY), ConduitTexture.arm(0)));
    ICONS.put(ICON_CORE_KEY, new ConduitTexture(TextureRegistry.registerTexture(ICON_CORE_KEY), ConduitTexture.core()));
  }

  protected DataConduitNetwork network;
  private Map<EnumFacing, InventoryDatabaseSource> sources = new EnumMap<EnumFacing, InventoryDatabaseSource>(EnumFacing.class);

  public DataConduit() {
  }

  @SuppressWarnings("null")
  @Override
  public boolean canConnectToExternal(@Nonnull EnumFacing direction, boolean ignoreConnectionMode) {
    World world = getBundle().getBundleworld();
    BlockPos pos = getBundle().getLocation();
    TileEntity te = world.getTileEntity(pos.offset(direction));
    if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
      return true;
    }
    return false;
  }

  @Override
  @Nonnull
  public ITabPanel createGuiPanel(@Nonnull IGuiExternalConnection gui, @Nonnull IClientConduit con) {
    return new DataSettings(gui, con);
  }

  @Override
  public boolean updateGuiPanel(@Nonnull ITabPanel panel) {
    if (panel instanceof DataSettings) {
      return ((DataSettings) panel).updateConduit(this);
    }
    return false;
  }

  @Override
  public int getGuiPanelTabOrder() {
    return 8;
  }

  @Override
  @Nonnull
  public Class<? extends IConduit> getBaseConduitType() {
    return IDataConduit.class;
  }

  @Override
  @Nonnull
  public ItemStack createItem() {
    return new ItemStack(InvpanelObject.item_data_conduit.getItemNN());
  }

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
    if (facing != null && capability == CapabilityDatabaseHandler.DATABASE_HANDLER_CAPABILITY && externalConnections.contains(facing)
        && getConnectionMode(facing) != ConnectionMode.DISABLED) {
      return true;
    }
    return false;
  }

  @Override
  @Nullable
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
    if (capability == CapabilityDatabaseHandler.DATABASE_HANDLER_CAPABILITY) {
      return CapabilityDatabaseHandler.DATABASE_HANDLER_CAPABILITY.cast(getNetwork());
    }
    return null;
  }

  @Override
  @Nonnull
  public DataConduitNetwork createNetworkForType() {
    return new DataConduitNetwork();
  }

  @Override
  @Nullable
  public DataConduitNetwork getNetwork() throws NullPointerException {
    return network;
  }

  @Override
  public boolean setNetwork(@Nonnull IConduitNetwork<?, ?> network) {
    this.network = (DataConduitNetwork) network;
    return super.setNetwork(network);
  }

  @Override
  public void clearNetwork() {
    network = null;
  }

  @Override
  public boolean onBlockActivated(@Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull RaytraceResult res, @Nonnull List<RaytraceResult> all) {
    if (ToolUtil.isToolEquipped(player, hand)) {
      if (!getBundle().getEntity().getWorld().isRemote) {
        final CollidableComponent component = res.component;
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
      return true;
    }
    return false;
  }

  @Override
  public void invalidate() {
    if (network != null) {
      for (EnumFacing dir : EnumFacing.VALUES) {
        network.removeSource(sources.get(dir));
      }
    }
  }

  @Override
  public void onAddedToBundle() {
    super.onAddedToBundle();
    NNList.FACING.apply(this::checkConnections);
  }

  @Override
  public void externalConnectionAdded(@Nonnull EnumFacing dir) {
    super.externalConnectionAdded(dir);
    checkConnections(dir);
  }

  @Override
  public void externalConnectionRemoved(@Nonnull EnumFacing dir) {
    super.externalConnectionRemoved(dir);
    checkConnections(dir);
  }

  @Override
  public void checkConnections(@Nonnull EnumFacing dir) {
    if (getExternalConnections().contains(dir) && getConnectionMode(dir).isActive()) {
      IItemHandler inv = getExternalInventory(dir);
      if (inv != null) {
        addSource(dir, new InventoryDatabaseSource(getBundle().getLocation(), inv));
        return;
      }
    }
    removeSource(dir);
  }

  @SuppressWarnings("null")
  @Override
  public @Nullable IItemHandler getExternalInventory(@Nonnull EnumFacing dir) {
    if (getConnectionMode(dir).isActive()) {
      World world = getBundle().getBundleworld();
      BlockPos pos = getBundle().getLocation();

      TileEntity te = BlockEnder.getAnyTileEntitySafe(world, pos.offset(dir));
      // Ghost items bug. Missing if check caused INV panel to scan it's own inventory, so placing items in the return area causes it to show up instantly in
      // the inventory
      if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) && !(te instanceof TileInventoryPanel)) {
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
      }
    }
    return null;
  }

  @Override
  public void addSource(@Nonnull EnumFacing dir, @Nonnull InventoryDatabaseSource source) {
    if (network != null) {
      network.addSource(source);
    }
    sources.put(dir, source);
  }

  @Override
  public void removeSource(@Nonnull EnumFacing dir) {
    if (network != null) {
      network.removeSource(sources.get(dir));
    }
    sources.remove(dir);
  }

  @Override
  public void connectionsChanged() {
    super.connectionsChanged();
    this.invalidate();
    NNList.FACING.apply(this::checkConnections);
  }

  // TEXTURES

  @SuppressWarnings("null")
  @Override
  @Nonnull
  public IConduitTexture getTextureForState(@Nonnull CollidableComponent component) {
    return ICONS.get(component.isCore() ? ICON_CORE_KEY : ICON_KEY);
  }

  @Override
  public @Nullable IConduitTexture getTransmitionTextureForState(@Nonnull CollidableComponent component) {
    return null;
  }

  @Override
  public @Nullable Vector4f getTransmitionTextureColorForState(@Nonnull CollidableComponent component) {
    return null;
  }

}
