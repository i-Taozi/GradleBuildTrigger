package crazypants.enderio.machines.machine.vat;

import javax.annotation.Nonnull;

import com.enderio.core.common.network.MessageTileEntity;
import com.enderio.core.common.network.NetworkUtil;

import crazypants.enderio.base.EnderIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTanks extends MessageTileEntity<TileVat> {

  private NBTTagCompound nbtRoot;

  public PacketTanks() {
  }

  public PacketTanks(@Nonnull TileVat tile) {
    super(tile);
    nbtRoot = new NBTTagCompound();
    if (tile.inputTank.getFluidAmount() > 0) {
      NBTTagCompound tankRoot = new NBTTagCompound();
      tile.inputTank.writeToNBT(tankRoot);
      nbtRoot.setTag("inputTank", tankRoot);
    }
    if (tile.outputTank.getFluidAmount() > 0) {
      NBTTagCompound tankRoot = new NBTTagCompound();
      tile.outputTank.writeToNBT(tankRoot);
      nbtRoot.setTag("outputTank", tankRoot);
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    NetworkUtil.writeNBTTagCompound(nbtRoot, buf);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    nbtRoot = NetworkUtil.readNBTTagCompound(buf);
  }

  public static class Handler implements IMessageHandler<PacketTanks, IMessage> {

    @Override
    public IMessage onMessage(PacketTanks message, MessageContext ctx) {
      EntityPlayer player = EnderIO.proxy.getClientPlayer();
      TileVat tile = message.getTileEntity(player.world);
      if (tile != null) {
        if (message.nbtRoot.hasKey("inputTank")) {
          NBTTagCompound tankRoot = message.nbtRoot.getCompoundTag("inputTank");
          tile.inputTank.readFromNBT(tankRoot);
        } else {
          tile.inputTank.setFluid(null);
        }
        if (message.nbtRoot.hasKey("outputTank")) {
          NBTTagCompound tankRoot = message.nbtRoot.getCompoundTag("outputTank");
          tile.outputTank.readFromNBT(tankRoot);
        } else {
          tile.outputTank.setFluid(null);
        }
      }
      return null;
    }
  }
}
