package crazypants.enderio.powertools.machine.capbank.packet;

import javax.annotation.Nonnull;

import crazypants.enderio.powertools.machine.capbank.network.ClientNetworkManager;
import crazypants.enderio.powertools.machine.capbank.network.ICapBankNetwork;
import crazypants.enderio.powertools.machine.capbank.network.NetworkState;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketNetworkStateResponse implements IMessage {

  private int id;
  private NetworkState state;

  public PacketNetworkStateResponse() {
  }

  public PacketNetworkStateResponse(@Nonnull ICapBankNetwork network) {
    this(network, false);
  }

  public PacketNetworkStateResponse(@Nonnull ICapBankNetwork network, boolean remove) {
    id = network.getId();
    if (!remove) {
      state = network.getState();
    } else {
      state = null;
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(id);
    buf.writeBoolean(state != null);
    if (state != null) {
      state.writeToBuf(buf);
    }
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    id = buf.readInt();
    boolean hasState = buf.readBoolean();
    if (hasState) {
      state = NetworkState.readFromBuf(buf);
    } else {
      state = null;
    }
  }

  public static class Handler implements IMessageHandler<PacketNetworkStateResponse, IMessage> {

    @SuppressWarnings("null")
    @Override
    public IMessage onMessage(PacketNetworkStateResponse message, MessageContext ctx) {
      if (message.state != null) {
        ClientNetworkManager.getInstance().updateState(message.id, message.state);
      } else {
        ClientNetworkManager.getInstance().destroyNetwork(message.id);
      }
      return null;
    }
  }
}
