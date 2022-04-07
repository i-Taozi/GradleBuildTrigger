package sugar.free.sightparser.applayer.messages.remote_control;

import sugar.free.sightparser.applayer.descriptors.Service;

public class ChangeTBRMessage extends SetTBRMessage {

    private static final long serialVersionUID = 1L;

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return 0x53A4;
    }
}
