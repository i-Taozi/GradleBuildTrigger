package sugar.free.sightparser.applayer.messages.remote_control;

import lombok.Setter;
import sugar.free.sightparser.Helpers;
import sugar.free.sightparser.pipeline.ByteBuf;

public class MultiwaveBolusMessage extends BolusMessage {

    private static final long serialVersionUID = 1L;

    @Setter
    private double amount;
    @Setter
    private double delayedAmount;
    @Setter
    private int duration;

    @Override
    protected byte[] getData() throws Exception {
        ByteBuf data = new ByteBuf(22);
        data.putShort((short) 0x2503);
        data.putShort((short) 0xFC00);
        data.putShort((short) 0x1F00);
        data.putShort((short) 0x0000);
        data.putUInt16LE(Helpers.roundDoubleToInt(amount * 100D));
        data.putUInt16LE(Helpers.roundDoubleToInt(delayedAmount * 100D));
        data.putUInt16LE(duration);
        data.putShort((short) 0x0000);
        data.putUInt16LE(Helpers.roundDoubleToInt(amount * 100D));
        data.putUInt16LE(Helpers.roundDoubleToInt(delayedAmount * 100D));
        data.putUInt16LE(duration);
        return data.getBytes();
    }
}
