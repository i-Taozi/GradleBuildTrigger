package crazypants.enderio.machines.machine.generator.zombie;

import javax.annotation.Nonnull;

import com.enderio.core.common.network.MessageTileEntity;

import crazypants.enderio.base.EnderIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketNutrientTank extends MessageTileEntity<TileEntity> {

  private int amount;

  public PacketNutrientTank() {
  }

  public <T extends TileEntity & IHasNutrientTank> PacketNutrientTank(@Nonnull T tile) {
    super(tile);
    amount = tile.getNutrientTank().getFluidAmount();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    buf.writeInt(amount);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    amount = buf.readInt();
  }

  public static class Handler implements IMessageHandler<PacketNutrientTank, IMessage> {

    @Override
    public IMessage onMessage(PacketNutrientTank message, MessageContext ctx) {
      EntityPlayer player = EnderIO.proxy.getClientPlayer();
      TileEntity tile = message.getTileEntity(player.world);
      if (tile instanceof IHasNutrientTank) {
        ((IHasNutrientTank) tile).getNutrientTank().setFluidAmount(message.amount);
      }
      return null;
    }
  }
}