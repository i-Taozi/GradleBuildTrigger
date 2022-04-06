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

import io.netty.buffer.Unpooled;

final class UnpooledByteBufferAllocator implements ByteBufferAllocator {

    static final UnpooledByteBufferAllocator INSTANCE = new UnpooledByteBufferAllocator();

    @Override
    public ByteBuffer buffer() {
        return new LanternByteBuffer(Unpooled.buffer());
    }

    @Override
    public ByteBuffer buffer(int initialCapacity) {
        return new LanternByteBuffer(Unpooled.buffer(initialCapacity));
    }

    @Override
    public ByteBuffer heapBuffer() {
        throw new UnsupportedOperationException("Heap buffers are not supported by the unpooled allocator.");
    }

    @Override
    public ByteBuffer heapBuffer(int initialCapacity) {
        throw new UnsupportedOperationException("Heap buffers are not supported by the unpooled allocator.");
    }

    @Override
    public ByteBuffer directBuffer() {
        return new LanternByteBuffer(Unpooled.directBuffer());
    }

    @Override
    public ByteBuffer directBuffer(int initialCapacity) {
        return new LanternByteBuffer(Unpooled.directBuffer(initialCapacity));
    }

    @Override
    public ByteBuffer wrappedBuffer(byte[] byteArray) {
        return new LanternByteBuffer(Unpooled.wrappedBuffer(byteArray));
    }
}
