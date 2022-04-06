/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.network.buffer;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.lanternpowered.server.data.persistence.nbt.NbtDataContainerInputStream;
import org.lanternpowered.server.data.persistence.nbt.NbtStreamUtils;
import org.lanternpowered.server.network.objects.RawItemStack;
import org.spongepowered.api.data.DataView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class LanternByteBuffer implements ByteBuffer {

    private final ByteBuf buf;

    @Nullable
    private LanternByteBuffer opposite;

    public LanternByteBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    public ByteBuf getDelegate() {
        return this.buf;
    }

    @Override
    public int getCapacity() {
        return this.buf.capacity();
    }

    @Override
    public int available() {
        return this.buf.readableBytes();
    }

    // TODO: Deprecate in the api
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int refCnt() {
        return this.buf.refCnt();
    }

    @Override
    public OutputStream asOutputStream() {
        return new ByteBufOutputStream(this.buf);
    }

    @Override
    public ByteBuffer retain() {
        this.buf.retain();
        return this;
    }

    @Override
    public ByteBuffer retain(int increment) {
        this.buf.retain(increment);
        return this;
    }

    @Override
    public ByteBuffer touch() {
        this.buf.touch();
        return this;
    }

    @Override
    public ByteBuffer touch(Object hint) {
        this.buf.touch(hint);
        return this;
    }

    @Override
    public LanternByteBuffer order(ByteOrder order) {
        if (this.buf.order().equals(order)) {
            return this;
        } else {
            if (this.opposite == null) {
                this.opposite = new LanternByteBuffer(this.buf.order(order));
                this.opposite.opposite = this;
            }
            return this.opposite;
        }
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ByteOrder getByteOrder() {
        return this.buf.order();
    }

    @Override
    public int readerIndex() {
        return this.buf.readerIndex();
    }

    @Override
    public LanternByteBuffer setReadIndex(int index) {
        this.buf.readerIndex(index);
        return this;
    }

    @Override
    public int writerIndex() {
        return this.buf.writerIndex();
    }

    @Override
    public LanternByteBuffer setWriteIndex(int index) {
        this.buf.writerIndex(index);
        return this;
    }

    @Override
    public LanternByteBuffer setIndex(int readIndex, int writeIndex) {
        this.buf.setIndex(readIndex, writeIndex);
        return this;
    }

    @Override
    public LanternByteBuffer clear() {
        this.buf.clear();
        return this;
    }

    @Override
    public LanternByteBuffer markRead() {
        this.buf.markReaderIndex();
        return this;
    }

    @Override
    public LanternByteBuffer markWrite() {
        this.buf.markWriterIndex();
        return this;
    }

    @Override
    public LanternByteBuffer resetRead() {
        this.buf.resetReaderIndex();
        return this;
    }

    @Override
    public LanternByteBuffer resetWrite() {
        this.buf.resetWriterIndex();
        return this;
    }

    @Override
    public LanternByteBuffer slice() {
        return new LanternByteBuffer(this.buf.slice());
    }

    @Override
    public LanternByteBuffer slice(int index, int length) {
        return new LanternByteBuffer(this.buf.slice(index, length));
    }

    @Override
    public boolean hasArray() {
        return this.buf.hasArray();
    }

    @Override
    public byte[] array() {
        return this.buf.array();
    }

    @Override
    public LanternByteBuffer writeBoolean(boolean data) {
        this.buf.writeBoolean(data);
        return this;
    }

    @Override
    public LanternByteBuffer setBoolean(int index, boolean data) {
        this.buf.setBoolean(index, data);
        return this;
    }

    @Override
    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    @Override
    public boolean getBoolean(int index) {
        return this.buf.getBoolean(index);
    }

    @Override
    public LanternByteBuffer writeByte(byte data) {
        this.buf.writeByte(data);
        return this;
    }

    @Override
    public LanternByteBuffer setByte(int index, byte data) {
        this.buf.setByte(index, data);
        return this;
    }

    @Override
    public byte readByte() {
        return this.buf.readByte();
    }

    @Override
    public byte getByte(int index) {
        return this.buf.getByte(index);
    }

    @Override
    public LanternByteBuffer writeByteArray(byte[] data) {
        writeVarInt(data.length);
        writeBytes(data);
        return this;
    }

    @Override
    public LanternByteBuffer writeByteArray(byte[] data, int start, int length) {
        writeVarInt(length);
        writeBytes(data, start, length);
        return this;
    }

    @Override
    public LanternByteBuffer setByteArray(int index, byte[] data) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        writeByteArray(data);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public LanternByteBuffer setByteArray(int index, byte[] data, int start, int length) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        writeVarInt(length);
        writeBytes(data, start, length);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public byte[] readLimitedByteArray(int maxLength) throws DecoderException {
        final int length = readVarInt();
        if (length < 0) {
            throw new DecoderException("Byte array length may not be negative.");
        }
        if (length > maxLength) {
            throw new DecoderException("Exceeded the maximum allowed length, got " + length + " which is greater then " + maxLength);
        }
        final byte[] bytes = new byte[length];
        this.buf.readBytes(bytes);
        return bytes;
    }

    @Override
    public byte[] readByteArray() {
        return readLimitedByteArray(Integer.MAX_VALUE);
    }

    @Override
    public byte[] readByteArray(int index) {
        final int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        final byte[] data = readByteArray();
        this.buf.readerIndex(oldIndex);
        return data;
    }

    @Override
    public LanternByteBuffer writeBytes(byte[] data) {
        this.buf.writeBytes(data);
        return this;
    }

    @Override
    public LanternByteBuffer writeBytes(byte[] data, int start, int length) {
        this.buf.writeBytes(data, start, length);
        return this;
    }

    @Override
    public LanternByteBuffer setBytes(int index, byte[] data) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        this.buf.writeBytes(data);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public LanternByteBuffer setBytes(int index, byte[] data, int start, int length) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        this.buf.writeBytes(data, start, length);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public byte[] readBytes(int length) {
        final byte[] data = new byte[length];
        this.buf.readBytes(data);
        return data;
    }

    @Override
    public byte[] readBytes(int index, int length) {
        final int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        final byte[] data = new byte[length];
        this.buf.readBytes(data);
        this.buf.readerIndex(oldIndex);
        return data;
    }

    @Override
    public LanternByteBuffer writeShort(short data) {
        this.buf.writeShort(data);
        return this;
    }

    @Override
    public LanternByteBuffer setShort(int index, short data) {
        this.buf.setShort(index, data);
        return this;
    }

    @Override
    public short readShort() {
        return this.buf.readShort();
    }

    @Override
    public short getShort(int index) {
        return this.buf.getShort(index);
    }

    @Override
    public LanternByteBuffer writeChar(char data) {
        this.buf.writeChar(data);
        return this;
    }

    @Override
    public LanternByteBuffer setChar(int index, char data) {
        this.buf.setChar(index, data);
        return this;
    }

    @Override
    public char readChar() {
        return this.buf.readChar();
    }

    @Override
    public char getChar(int index) {
        return this.buf.getChar(index);
    }

    @Override
    public LanternByteBuffer writeInteger(int data) {
        this.buf.writeInt(data);
        return this;
    }

    @Override
    public LanternByteBuffer setInteger(int index, int data) {
        this.buf.setInt(index, data);
        return this;
    }

    @Override
    public int readInteger() {
        return this.buf.readInt();
    }

    @Override
    public int getInteger(int index) {
        return this.buf.getInt(index);
    }

    @Override
    public LanternByteBuffer writeLong(long data) {
        this.buf.writeLong(data);
        return this;
    }

    @Override
    public LanternByteBuffer setLong(int index, long data) {
        this.buf.setLong(index, data);
        return this;
    }

    @Override
    public long readLong() {
        return this.buf.readLong();
    }

    @Override
    public long getLong(int index) {
        return this.buf.getLong(index);
    }

    @Override
    public LanternByteBuffer writeFloat(float data) {
        this.buf.writeFloat(data);
        return this;
    }

    @Override
    public LanternByteBuffer setFloat(int index, float data) {
        this.buf.setFloat(index, data);
        return this;
    }

    @Override
    public float readFloat() {
        return this.buf.readFloat();
    }

    @Override
    public float getFloat(int index) {
        return this.buf.getFloat(index);
    }

    @Override
    public LanternByteBuffer writeDouble(double data) {
        this.buf.writeDouble(data);
        return this;
    }

    @Override
    public LanternByteBuffer setDouble(int index, double data) {
        this.buf.setDouble(index, data);
        return this;
    }

    @Override
    public double readDouble() {
        return this.buf.readDouble();
    }

    @Override
    public double getDouble(int index) {
        return this.buf.getDouble(index);
    }

    public static void writeVarInt(ByteBuf byteBuf, int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            byteBuf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        byteBuf.writeByte(value & 0x7F);
    }

    public static int readVarInt(ByteBuf byteBuf) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = byteBuf.readByte()) & 0x80) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 35) {
                throw new DecoderException("Variable length is too long!");
            }
        }
        return value | (b << i);
    }

    @Override
    public LanternByteBuffer writeVarInt(int value) {
        writeVarInt(this.buf, value);
        return this;
    }

    @Override
    public LanternByteBuffer setVarInt(int index, int value) {
        int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        this.writeVarInt(value);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public int readVarInt() {
        return readVarInt(this.buf);
    }

    @Override
    public int getVarInt(int index) {
        final int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        final int data = readVarInt();
        this.buf.readerIndex(oldIndex);
        return data;
    }

    @Override
    public LanternByteBuffer writeString(String data) {
        writeByteArray(data.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    @Override
    public LanternByteBuffer setString(int index, String data) {
        setByteArray(index, data.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    @Override
    public String readLimitedString(int maxLength) throws DecoderException {
        return new String(readLimitedByteArray(maxLength * 4), StandardCharsets.UTF_8);
    }

    @Override
    public String readString() {
        return readLimitedString(Short.MAX_VALUE);
    }

    @Override
    public String getString(int index) {
        return new String(readByteArray(index), StandardCharsets.UTF_8);
    }

    @Override
    public LanternByteBuffer writeUTF(String data) {
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 32767) {
            throw new EncoderException("String too big (was " + data.length() + " bytes encoded, max " + 32767 + ")");
        }
        this.buf.writeShort(bytes.length);
        this.buf.writeBytes(bytes);
        return this;
    }

    @Override
    public LanternByteBuffer setUTF(int index, String data) {
        checkNotNull(data, "data");
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        writeUTF(data);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public String readUTF() {
        final int length = readShort();
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }

    @Override
    public String getUTF(int index) {
        final int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        final String data = readUTF();
        this.buf.readerIndex(oldIndex);
        return data;
    }

    @Override
    public LanternByteBuffer writeUniqueId(UUID data) {
        this.buf.writeLong(data.getMostSignificantBits());
        this.buf.writeLong(data.getLeastSignificantBits());
        return this;
    }

    @Override
    public LanternByteBuffer setUniqueId(int index, UUID data) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        writeUniqueId(data);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public UUID readUniqueId() {
        final long most = this.buf.readLong();
        final long least = this.buf.readLong();
        return new UUID(most, least);
    }

    @Override
    public UUID getUniqueId(int index) {
        return getAt(index, this::readUniqueId);
    }

    @Override
    public LanternByteBuffer writeDataView(@Nullable DataView data) {
        if (data == null) {
            this.buf.writeByte(0);
            return this;
        }
        try {
            NbtStreamUtils.write(data, new ByteBufOutputStream(this.buf), false);
        } catch (IOException e) {
            throw new CodecException(e);
        }
        return this;
    }

    @Override
    public LanternByteBuffer setDataView(int index, @Nullable DataView data) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        writeDataView(data);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Nullable
    @Override
    public DataView readLimitedDataView(int maximumDepth, int maxBytes) {
        final int index = this.buf.readerIndex();
        if (this.buf.readByte() == 0) {
            return null;
        }
        this.buf.readerIndex(index);
        try {
            try (NbtDataContainerInputStream input = new NbtDataContainerInputStream(
                    ByteStreams.limit(new ByteBufInputStream(this.buf), maxBytes), maximumDepth)) {
                return input.read();
            }
        } catch (IOException e) {
            throw new CodecException(e);
        }
    }

    @Nullable
    @Override
    public DataView readDataView() {
        return readLimitedDataView(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Nullable
    @Override
    public DataView getDataView(int index) {
        return getAt(index, this::readDataView);
    }

    @Override
    public ByteBuffer writeBytes(ByteBuffer byteBuffer) {
        this.buf.writeBytes(((LanternByteBuffer) byteBuffer).getDelegate());
        return this;
    }

    @Override
    public ByteBuffer readBytes(byte[] byteArray) {
        this.buf.readBytes(byteArray);
        return this;
    }

    @Override
    public ByteBuffer readBytes(byte[] dst, int dstIndex, int length) {
        this.buf.readBytes(dst, dstIndex, length);
        return this;
    }

    @Override
    public ByteBuffer readBytes(ByteBuffer byteBuffer) {
        this.buf.readBytes(((LanternByteBuffer) byteBuffer).getDelegate());
        return this;
    }

    @Override
    public ByteBuffer readBytes(ByteBuffer dst, int dstIndex, int length) {
        this.buf.readBytes(((LanternByteBuffer) dst).getDelegate(), dstIndex, length);
        return this;
    }

    @Override
    public LanternByteBuffer writeVarLong(long value) {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            this.buf.writeByte(((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }
        this.buf.writeByte((int) value & 0x7F);
        return this;
    }

    @Override
    public LanternByteBuffer setVarLong(int index, long value) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        writeVarLong(value);
        this.buf.writerIndex(oldIndex);
        return this;
    }

    @Override
    public long readVarLong() {
        long value = 0L;
        int i = 0;
        long b;
        while (((b = this.buf.readByte()) & 0x80L) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 63) {
                throw new DecoderException("Variable length is too long!");
            }
        }
        return value | (b << i);
    }

    @Override
    public long getVarLong(int index) {
        final int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        final long data = readVarLong();
        this.buf.readerIndex(oldIndex);
        return data;
    }

    @Override
    public Vector3i getVector3i(int index) {
        return getAt(index, this::readVector3i);
    }

    @Override
    public LanternByteBuffer setVector3i(int index, Vector3i vector) {
        return setAt(index, vector, this::writeVector3i);
    }

    @Override
    public Vector3i readVector3i() {
        final long value = this.buf.readLong();
        final int x = (int) (value >> 38);
        final int y = (int) (value << 26 >> 52);
        final int z = (int) (value << 38 >> 38);
        return new Vector3i(x, y, z);
    }

    @Override
    public LanternByteBuffer writeVector3i(int x, int y, int z) {
        this.buf.writeLong(((long) x & 0x3ffffff) << 38 | ((long) y & 0xfff) << 26 | ((long) z & 0x3ffffff));
        return this;
    }

    @Override
    public LanternByteBuffer writeVector3i(Vector3i vector) {
        return writeVector3i(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public Vector3f getVector3f(int index) {
        return getAt(index, this::readVector3f);
    }

    @Override
    public LanternByteBuffer setVector3f(int index, Vector3f vector) {
        return setAt(index, vector, this::writeVector3f);
    }

    @Override
    public Vector3f readVector3f() {
        final float x = this.buf.readFloat();
        final float y = this.buf.readFloat();
        final float z = this.buf.readFloat();
        return new Vector3f(x, y, z);
    }

    @Override
    public LanternByteBuffer writeVector3f(float x, float y, float z) {
        this.buf.ensureWritable(Float.BYTES * 3);
        this.buf.writeFloat(x);
        this.buf.writeFloat(y);
        this.buf.writeFloat(z);
        return this;
    }

    @Override
    public LanternByteBuffer writeVector3f(Vector3f vector) {
        return writeVector3f(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public Vector3d getVector3d(int index) {
        return getAt(index, this::readVector3d);
    }

    @Override
    public LanternByteBuffer setVector3d(int index, Vector3d vector) {
        return setAt(index, vector, this::writeVector3d);
    }

    @Override
    public Vector3d readVector3d() {
        final double x = this.buf.readDouble();
        final double y = this.buf.readDouble();
        final double z = this.buf.readDouble();
        return new Vector3d(x, y, z);
    }

    @Override
    public LanternByteBuffer writeVector3d(double x, double y, double z) {
        this.buf.ensureWritable(Double.BYTES * 3);
        this.buf.writeDouble(x);
        this.buf.writeDouble(y);
        this.buf.writeDouble(z);
        return this;
    }

    @Override
    public LanternByteBuffer writeVector3d(Vector3d vector) {
        return writeVector3d(vector.getX(), vector.getY(), vector.getZ());
    }

    @Nullable
    @Override
    public RawItemStack getRawItemStack(int index) {
        return getAt(index, this::readRawItemStack);
    }

    @Override
    public LanternByteBuffer setRawItemStack(int index, @Nullable RawItemStack rawItemStack) {
        return setAt(index, rawItemStack, this::writeRawItemStack);
    }

    @Nullable
    @Override
    public RawItemStack readRawItemStack() {
        final short id = this.buf.readShort();
        if (id == -1) {
            return null;
        }
        final int amount = this.buf.readByte();
        final int data = this.buf.readShort();
        final DataView dataView = readDataView();
        return new RawItemStack(id, data, amount, dataView);
    }

    @Override
    public LanternByteBuffer writeRawItemStack(@Nullable RawItemStack rawItemStack) {
        if (rawItemStack == null) {
            this.buf.writeShort((short) -1);
        } else {
            this.buf.writeShort((short) rawItemStack.getItemType());
            this.buf.writeByte((byte) rawItemStack.getAmount());
            this.buf.writeShort((short) rawItemStack.getData());
            writeDataView(rawItemStack.getDataView());
        }
        return this;
    }

    @Override
    public LanternByteBuffer ensureWritable(int minWritableBytes) {
        this.buf.ensureWritable(minWritableBytes);
        return this;
    }

    @Override
    public boolean release() {
        return this.buf.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.buf.release(decrement);
    }

    @Override
    public LanternByteBuffer copy() {
        return new LanternByteBuffer(this.buf.copy());
    }

    private <T> T getAt(int index, Supplier<T> supplier) {
        final int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        final T data = supplier.get();
        this.buf.readerIndex(oldIndex);
        return data;
    }

    private <T> LanternByteBuffer setAt(int index, T object, Consumer<T> consumer) {
        final int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        consumer.accept(object);
        this.buf.writerIndex(oldIndex);
        return this;
    }
}
