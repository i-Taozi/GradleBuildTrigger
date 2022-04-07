package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class ReadDateTimeMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xE300;
    }

    @Override
    protected boolean inCRC() {
        return true;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        year = byteBuf.readUInt16LE();
        month = byteBuf.readByte();
        day = byteBuf.readByte();
        hour = byteBuf.readByte();
        minute = byteBuf.readByte();
        second = byteBuf.readByte();
    }
}
