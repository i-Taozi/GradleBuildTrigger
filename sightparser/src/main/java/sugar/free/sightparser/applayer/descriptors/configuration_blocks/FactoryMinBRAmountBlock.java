package sugar.free.sightparser.applayer.descriptors.configuration_blocks;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.pipeline.ByteBuf;

public class FactoryMinBRAmountBlock extends ConfigurationBlock {

    private static final long serialVersionUID = 1L;

    public static final short ID = (short) 0xEBEB;

    @Getter
    @Setter
    private double minimumAmount;

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public void parse(ByteBuf byteBuf) {
        minimumAmount = Helpers.roundDouble(((double) byteBuf.readUInt16LE()) / 100D);
    }

    @Override
    public byte[] getData() {
        return null;
    }
}
