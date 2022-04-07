package sugar.free.sightparser.applayer.messages;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.Message;
import sugar.free.sightparser.applayer.descriptors.MessagePriority;
import sugar.free.sightparser.applayer.messages.configuration.CloseWriteSessionMessage;
import sugar.free.sightparser.applayer.messages.configuration.OpenWriteSessionMessage;
import sugar.free.sightparser.applayer.messages.configuration.ReadConfigurationBlockMessage;
import sugar.free.sightparser.applayer.messages.configuration.WriteConfigurationBlockMessage;
import sugar.free.sightparser.applayer.messages.configuration.WriteDateTimeMessage;
import sugar.free.sightparser.applayer.messages.connection.ActivateServiceMessage;
import sugar.free.sightparser.applayer.messages.connection.BindMessage;
import sugar.free.sightparser.applayer.messages.connection.ConnectMessage;
import sugar.free.sightparser.applayer.messages.connection.DeactivateAllServicesMessage;
import sugar.free.sightparser.applayer.messages.connection.DisconnectMessage;
import sugar.free.sightparser.applayer.messages.connection.ServiceChallengeMessage;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.messages.history.CloseHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.messages.history.OpenHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.messages.history.ReadHistoryFramesMessage;
import sugar.free.sightparser.applayer.messages.remote_control.AvailableBolusesMessage;
import sugar.free.sightparser.applayer.messages.remote_control.BolusMessage;
import sugar.free.sightparser.applayer.messages.remote_control.CancelBolusMessage;
import sugar.free.sightparser.applayer.messages.remote_control.CancelTBRMessage;
import sugar.free.sightparser.applayer.messages.remote_control.ChangeTBRMessage;
import sugar.free.sightparser.applayer.messages.remote_control.DismissAlertMessage;
import sugar.free.sightparser.applayer.messages.remote_control.MuteAlertMessage;
import sugar.free.sightparser.applayer.messages.remote_control.SetPumpStatusMessage;
import sugar.free.sightparser.applayer.messages.remote_control.SetTBRMessage;
import sugar.free.sightparser.applayer.messages.status.ActiveAlertMessage;
import sugar.free.sightparser.applayer.messages.status.ActiveBolusesMessage;
import sugar.free.sightparser.applayer.messages.status.BatteryAmountMessage;
import sugar.free.sightparser.applayer.messages.status.CartridgeAmountMessage;
import sugar.free.sightparser.applayer.messages.status.CurrentBasalMessage;
import sugar.free.sightparser.applayer.messages.status.CurrentTBRMessage;
import sugar.free.sightparser.applayer.messages.status.DailyTotalMessage;
import sugar.free.sightparser.applayer.messages.status.FirmwareVersionMessage;
import sugar.free.sightparser.applayer.messages.status.PumpStatusMessage;
import sugar.free.sightparser.applayer.messages.status.ReadDateTimeMessage;
import sugar.free.sightparser.applayer.messages.status.WarrantyTimerMessage;
import sugar.free.sightparser.applayer.messages.status_param.ReadStatusParamBlockMessage;
import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.errors.AppError;
import sugar.free.sightparser.exceptions.InvalidAppCRCException;
import sugar.free.sightparser.exceptions.InvalidAppVersionException;
import sugar.free.sightparser.errors.UnknownAppErrorCodeError;
import sugar.free.sightparser.exceptions.UnknownAppMessageException;
import sugar.free.sightparser.exceptions.UnknownServiceException;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class AppLayerMessage extends Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final byte VERSION = 0x20;
    @SuppressLint("UseSparseArrays")
    private static Map<Byte, Map<Short, Class<? extends AppLayerMessage>>> MESSAGES = new HashMap<>();

    static {
        Map<Short, Class<? extends AppLayerMessage>> connectionMessages = new HashMap<>();
        connectionMessages.put((short) 0xCDF3, BindMessage.class);
        connectionMessages.put((short) 0x0BF0, ConnectMessage.class);
        connectionMessages.put((short) 0x14F0, DisconnectMessage.class);
        connectionMessages.put((short) 0xD2F3, ServiceChallengeMessage.class);
        connectionMessages.put((short) 0xF7F0, ActivateServiceMessage.class);
        connectionMessages.put((short) 0x31F3, DeactivateAllServicesMessage.class);
        MESSAGES.put(Service.CONNECTION.getServiceID(), connectionMessages);

        Map<Short, Class<? extends AppLayerMessage>> statusMessages = new HashMap<>();
        statusMessages.put((short) 0xFC00, PumpStatusMessage.class);
        statusMessages.put((short) 0xA905, CurrentBasalMessage.class);
        statusMessages.put((short) 0x3A03, CartridgeAmountMessage.class);
        statusMessages.put((short) 0x2503, BatteryAmountMessage.class);
        statusMessages.put((short) 0xB605, CurrentTBRMessage.class);
        statusMessages.put((short) 0x6F06, ActiveBolusesMessage.class);
        statusMessages.put((short) 0xD82E, FirmwareVersionMessage.class);
        statusMessages.put((short) 0x4A05, WarrantyTimerMessage.class);
        statusMessages.put((short) 0xE300, ReadDateTimeMessage.class);
        statusMessages.put((short) 0xD903, ActiveAlertMessage.class);
        statusMessages.put((short) 0xC603, DailyTotalMessage.class);
        MESSAGES.put(Service.STATUS.getServiceID(), statusMessages);

        Map<Short, Class<? extends AppLayerMessage>> remoteControlMessages = new HashMap<>();
        remoteControlMessages.put((short) 0x031B, BolusMessage.class);
        remoteControlMessages.put((short) 0xE01B, CancelBolusMessage.class);
        remoteControlMessages.put((short) 0x3918, CancelTBRMessage.class);
        remoteControlMessages.put((short) 0xC518, SetTBRMessage.class);
        remoteControlMessages.put((short) 0x53A4, ChangeTBRMessage.class);
        remoteControlMessages.put((short) 0xDA18, AvailableBolusesMessage.class);
        remoteControlMessages.put((short) 0x2618, SetPumpStatusMessage.class);
        remoteControlMessages.put((short) 0x8C06, MuteAlertMessage.class);
        remoteControlMessages.put((short) 0x9306, DismissAlertMessage.class);
        MESSAGES.put(Service.REMOTE_CONTROL.getServiceID(), remoteControlMessages);

        Map<Short, Class<? extends AppLayerMessage>> configurationMessages = new HashMap<>();
        configurationMessages.put((short) 0x561E, ReadConfigurationBlockMessage.class);
        configurationMessages.put((short) 0x491E, OpenWriteSessionMessage.class);
        configurationMessages.put((short) 0xB51E, CloseWriteSessionMessage.class);
        configurationMessages.put((short) 0xAA1E, WriteConfigurationBlockMessage.class);
        configurationMessages.put((short) 0xFF1B, WriteDateTimeMessage.class);
        MESSAGES.put(Service.CONFIGURATION.getServiceID(), configurationMessages);

        Map<Short, Class<? extends AppLayerMessage>> historyMessages = new HashMap<>();
        historyMessages.put((short) 0xE797, CloseHistoryReadingSessionMessage.class);
        historyMessages.put((short) 0x5428, OpenHistoryReadingSessionMessage.class);
        historyMessages.put((short) 0xA828, ReadHistoryFramesMessage.class);
        MESSAGES.put(Service.HISTORY.getServiceID(), historyMessages);

        Map<Short, Class<? extends AppLayerMessage>> statusParamMessages = new HashMap<>();
        statusParamMessages.put((short) 0x561E, ReadStatusParamBlockMessage.class);
        MESSAGES.put(Service.STATUS_PARAM.getServiceID(), statusParamMessages);
    }

    @Getter
    @Setter
    private MessagePriority messagePriority = MessagePriority.NORMAL;

    protected byte[] getData() throws Exception {
        return new byte[0];
    }

    public abstract Service getService();

    public abstract short getCommand();

    protected void parse(ByteBuf byteBuf) throws Exception {
    }

    protected boolean inCRC() {
        return false;
    }

    protected boolean outCRC() {
        return false;
    }

    public byte[] serialize() throws Exception {
        byte[] data = getData();
        ByteBuf byteBuf = new ByteBuf(4 + data.length + (outCRC() ? 2 : 0));
        byteBuf.putByte(VERSION);
        byteBuf.putByte(getService().getServiceID());
        byteBuf.putShort(getCommand());
        byteBuf.putBytes(data);
        if (outCRC()) byteBuf.putUInt16LE(Cryptograph.calculateCRC(data));
        return byteBuf.getBytes();
    }

    public static AppLayerMessage deserialize(ByteBuf byteBuf) throws Exception {
        byte version = byteBuf.readByte();
        byte service = byteBuf.readByte();
        short command = byteBuf.readShort();
        short error = byteBuf.readShort();
        byte[] data = byteBuf.readBytes();
        if (version != VERSION) throw new InvalidAppVersionException(version, VERSION);
        if (!MESSAGES.containsKey(service)) throw new UnknownServiceException(service);
        Class<? extends AppLayerMessage> clazz = MESSAGES.get(service).get(command);
        if (clazz == null) throw new UnknownAppMessageException(service, command);
        if (error != 0x0000) {
            Class<? extends AppError> errorClass = AppError.ERRORS.get(error);
            if (errorClass != null) throw errorClass.getConstructor(Class.class, short.class).newInstance(clazz, error);
            else throw new UnknownAppErrorCodeError(clazz, error);
        }
        AppLayerMessage message = clazz.newInstance();
        ByteBuf dataBuf = new ByteBuf(data.length);
        dataBuf.putBytes(data);
        if (message.inCRC()) {
            int crc = dataBuf.getUInt16LE(data.length - 2);
            byte[] bytes = dataBuf.getBytes(data.length - 2);
            int calculatedCRC = Cryptograph.calculateCRC(bytes);
            if (crc != calculatedCRC) throw new InvalidAppCRCException(crc, calculatedCRC);
            dataBuf = new ByteBuf(bytes.length);
            dataBuf.putBytes(bytes);
        }
        message.parse(dataBuf);
        return message;
    }

}
