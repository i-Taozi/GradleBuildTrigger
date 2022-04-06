package crazypants.enderio.invpanel.network;

import crazypants.enderio.invpanel.invpanel.InventoryPanelContainer;
import crazypants.enderio.invpanel.invpanel.TileInventoryPanel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetExtractionDisabled implements IMessage {

  private int windowId;
  private boolean extractionDisabled;

  public PacketSetExtractionDisabled() {
  }

  public PacketSetExtractionDisabled(int windowId, boolean extractionDisabled) {
    this.windowId = windowId;
    this.extractionDisabled = extractionDisabled;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    windowId = buf.readInt();
    extractionDisabled = buf.readBoolean();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(windowId);
    buf.writeBoolean(extractionDisabled);
  }
  
  public static class Handler implements IMessageHandler<PacketSetExtractionDisabled, IMessage> {
  
    @Override
    public IMessage onMessage(PacketSetExtractionDisabled message, MessageContext ctx) {
      EntityPlayerMP player = ctx.getServerHandler().player;
      if (player.openContainer.windowId == message.windowId && player.openContainer instanceof InventoryPanelContainer) {
        InventoryPanelContainer ipc = (InventoryPanelContainer) player.openContainer;
        TileInventoryPanel teInvPanel = ipc.getTe();
        teInvPanel.setExtractionDisabled(message.extractionDisabled);
      }
      return null;
    }
  }
}
