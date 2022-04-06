package crazypants.enderio.machines.machine.obelisk.weather;

import javax.annotation.Nonnull;

import com.enderio.core.common.network.MessageTileEntity;

import crazypants.enderio.base.EnderIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketWeatherTank extends MessageTileEntity<TileWeatherObelisk> {

  private NBTTagCompound tag;

  public PacketWeatherTank() {
  }

  public PacketWeatherTank(@Nonnull TileWeatherObelisk tile) {
    super(tile);
    tag = tile.getInputTank().writeToNBT(new NBTTagCompound());
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    ByteBufUtils.writeTag(buf, tag);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    tag = ByteBufUtils.readTag(buf);
  }

  public static class Handler implements IMessageHandler<PacketWeatherTank, IMessage> {

    @Override
    public IMessage onMessage(PacketWeatherTank message, MessageContext ctx) {
      EntityPlayer player = EnderIO.proxy.getClientPlayer();
      TileWeatherObelisk tile = message.getTileEntity(player.world);
      if (tile != null) {
        tile.getInputTank().readFromNBT(message.tag);
      }
      return null;
    }
  }
}