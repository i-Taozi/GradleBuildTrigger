package edu.wpi.first.wpilibj.hal;

import java.nio.ByteBuffer;

public class JNIWrapper {

    public static int getPort(byte port) {
        return getPortWithModule((byte)0, port);
    }

    public static int getPortWithModule(byte module, byte channel) {
        byte[] data = { module, channel };
        return (int)ByteBuffer.wrap(data).getShort();
    }

    public static byte[] getPortAndModuleFromHandle(int handle) {
        return ByteBuffer.allocate(2).putShort((short)handle).array();
    }

}
