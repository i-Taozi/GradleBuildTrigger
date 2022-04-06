package crazypants.enderio.invpanel.network;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enderio.core.common.network.CompressedDataOutput;
import com.enderio.core.common.network.NetworkUtil;

import crazypants.enderio.base.invpanel.database.IInventoryDatabaseServer;
import crazypants.enderio.base.invpanel.database.IServerItemEntry;
import crazypants.enderio.invpanel.database.InventoryDatabase;
import crazypants.enderio.invpanel.invpanel.InventoryPanelContainer;
import crazypants.enderio.invpanel.invpanel.TileInventoryPanel;
import crazypants.enderio.invpanel.server.InventoryDatabaseServer;
import crazypants.enderio.invpanel.server.ItemEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestMissingItems implements IMessage {

  private int windowId;
  private byte[] compressed;

  public PacketRequestMissingItems() {
  }

  public PacketRequestMissingItems(int windowId, int generation, List<Integer> missingIDs) {
    this.windowId = windowId;
    try {
      CompressedDataOutput cdo = new CompressedDataOutput();
      try {
        cdo.writeVariable(generation);
        cdo.writeVariable(missingIDs.size());
        for (Integer id : missingIDs) {
          cdo.writeVariable(id - InventoryDatabase.COMPLEX_DBINDEX_START);
        }
        compressed = cdo.getCompressed();
      } finally {
        cdo.close();
      }
    } catch (IOException ex) {
      compressed = new byte[0];
    }
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    windowId = buf.readInt();
    compressed = NetworkUtil.readByteArray(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(windowId);
    NetworkUtil.writeByteArray(buf, compressed);
  }
  
  public static class Handler implements IMessageHandler<PacketRequestMissingItems, IMessage> {

    @Override
    public IMessage onMessage(PacketRequestMissingItems message, MessageContext ctx) {
      EntityPlayerMP player = ctx.getServerHandler().player;
      if (player.openContainer.windowId == message.windowId && player.openContainer instanceof InventoryPanelContainer) {
        InventoryPanelContainer ipc = (InventoryPanelContainer) player.openContainer;
        TileInventoryPanel teInvPanel = ipc.getTe();
        IInventoryDatabaseServer db = teInvPanel.getDatabaseServer();
        if (db != null) {
          try {
            List<? extends IServerItemEntry> items = db.decompressMissingItems(message.compressed);
            if (!items.isEmpty()) {
              return new PacketItemInfo(message.windowId, db, items);
            }
          } catch (IOException ex) {
            Logger.getLogger(PacketItemInfo.class.getName()).log(Level.SEVERE, "Exception while reading missing item IDs", ex);
          }
        }
      }
      return null;
  }
  }
}
