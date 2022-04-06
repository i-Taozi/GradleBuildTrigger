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
package org.lanternpowered.server.network.vanilla.message.codec.play;

import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.EncoderException;
import org.lanternpowered.server.network.buffer.ByteBuffer;
import org.lanternpowered.server.network.message.codec.Codec;
import org.lanternpowered.server.network.message.codec.CodecContext;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutWorldBorder;

public final class CodecPlayOutWorldBorder implements Codec<MessagePlayOutWorldBorder> {

    @Override
    public ByteBuffer encode(CodecContext context, MessagePlayOutWorldBorder message) throws CodecException {
        ByteBuffer buf = context.byteBufAlloc().buffer();

        if (message instanceof MessagePlayOutWorldBorder.Initialize) {
            MessagePlayOutWorldBorder.Initialize message1 = (MessagePlayOutWorldBorder.Initialize) message;
            buf.writeVarInt(3);
            buf.writeDouble(message1.getCenterX());
            buf.writeDouble(message1.getCenterZ());
            buf.writeDouble(message1.getOldDiameter());
            buf.writeDouble(message1.getNewDiameter());
            buf.writeVarLong(message1.getLerpTime());
            buf.writeVarInt(message1.getWorldSize());
            buf.writeVarInt(message1.getWarningTime());
            buf.writeVarInt(message1.getWarningDistance());
        } else if (message instanceof MessagePlayOutWorldBorder.UpdateCenter) {
            MessagePlayOutWorldBorder.UpdateCenter message1 = (MessagePlayOutWorldBorder.UpdateCenter) message;
            buf.writeVarInt(2);
            buf.writeDouble(message1.getX());
            buf.writeDouble(message1.getZ());
        } else if (message instanceof MessagePlayOutWorldBorder.UpdateLerpedDiameter) {
            MessagePlayOutWorldBorder.UpdateLerpedDiameter message1 = (MessagePlayOutWorldBorder.UpdateLerpedDiameter) message;
            buf.writeVarInt(1);
            buf.writeDouble(message1.getOldDiameter());
            buf.writeDouble(message1.getNewDiameter());
            buf.writeVarLong(message1.getLerpTime());
        } else if (message instanceof MessagePlayOutWorldBorder.UpdateDiameter) {
            buf.writeVarInt(0);
            buf.writeDouble(((MessagePlayOutWorldBorder.UpdateDiameter) message).getDiameter());
        } else if (message instanceof MessagePlayOutWorldBorder.UpdateWarningDistance) {
            buf.writeVarInt(5);
            buf.writeVarInt(((MessagePlayOutWorldBorder.UpdateWarningDistance) message).getDistance());
        } else if (message instanceof MessagePlayOutWorldBorder.UpdateWarningTime) {
            buf.writeVarInt(4);
            buf.writeVarInt(((MessagePlayOutWorldBorder.UpdateWarningTime) message).getTime());
        } else {
            throw new EncoderException("Unsupported message type: " + message.getClass().getName());
        }

        return buf;
    }
}
