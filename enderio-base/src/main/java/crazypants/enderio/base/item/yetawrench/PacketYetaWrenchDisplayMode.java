package crazypants.enderio.base.item.yetawrench;

import crazypants.enderio.base.conduit.ConduitDisplayMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketYetaWrenchDisplayMode implements IMessage {

  private int slot;
  private ConduitDisplayMode mode;

  public PacketYetaWrenchDisplayMode() {
  }

  public PacketYetaWrenchDisplayMode(int slot, ConduitDisplayMode mode) {
    this.slot = slot;
    this.mode = mode;
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeInt(slot);
    ByteBufUtils.writeUTF8String(buffer, mode.getName());
  }

  @Override
  public void fromBytes(ByteBuf buffer) {
    slot = buffer.readInt();
    mode = ConduitDisplayMode.fromName(ByteBufUtils.readUTF8String(buffer));
  }

  public static class Handler implements IMessageHandler<PacketYetaWrenchDisplayMode, IMessage> {
    @Override
    public IMessage onMessage(PacketYetaWrenchDisplayMode message, MessageContext ctx) {
      final ConduitDisplayMode mode_nullchecked = message.mode;
      if (mode_nullchecked != null && message.slot >= 0 && message.slot < InventoryPlayer.getHotbarSize()) {
        ConduitDisplayMode.setDisplayMode(ctx.getServerHandler().player.inventory.getStackInSlot(message.slot), mode_nullchecked);
      }
      return null;
    }

  }

}
