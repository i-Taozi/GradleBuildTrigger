package crazypants.enderio.invpanel.network;

import crazypants.enderio.invpanel.invpanel.InventoryPanelContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMoveItems implements IMessage {

  private int fromSlot;
  private int toSlotStart;
  private int toSlotEnd;
  private int amount;

  public PacketMoveItems(int fromSlot, int toSlotStart, int toSlotEnd, int amount) {
    this.fromSlot = fromSlot;
    this.toSlotStart = toSlotStart;
    this.toSlotEnd = toSlotEnd;
    this.amount = amount;
  }

  public PacketMoveItems() {
  }

  @Override
  public void fromBytes(ByteBuf bb) {
    fromSlot = bb.readShort();
    toSlotStart = bb.readShort();
    toSlotEnd = bb.readShort();
    amount = bb.readShort();
  }

  @Override
  public void toBytes(ByteBuf bb) {
    bb.writeShort(fromSlot);
    bb.writeShort(toSlotStart);
    bb.writeShort(toSlotEnd);
    bb.writeShort(amount);
  }
  
  public static class Handler implements IMessageHandler<PacketMoveItems, IMessage> {
  
    @Override
    public IMessage onMessage(PacketMoveItems message, MessageContext ctx) {
      EntityPlayerMP player = ctx.getServerHandler().player;
      if(player.openContainer instanceof InventoryPanelContainer) {
        InventoryPanelContainer ipc = (InventoryPanelContainer) player.openContainer;
        ipc.executeMoveItems(message.fromSlot, message.toSlotStart, message.toSlotEnd, message.amount);
      }
      return null;
    }
  }
}
