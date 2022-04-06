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
package org.lanternpowered.server.network.vanilla.message.type.play;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.lanternpowered.server.network.message.Message;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.util.Direction;

public final class MessagePlayInPlayerBlockPlacement implements Message {

    private final Vector3i position;
    private final Vector3d clickOffset;
    private final Direction face;
    private final HandType handType;

    public MessagePlayInPlayerBlockPlacement(Vector3i position, Vector3d clickOffset, Direction face, HandType handType) {
        this.clickOffset = clickOffset;
        this.position = position;
        this.handType = handType;
        this.face = face;
    }

    public Vector3i getPosition() {
        return this.position;
    }

    public Vector3d getClickOffset() {
        return this.clickOffset;
    }

    public Direction getFace() {
        return this.face;
    }

    public HandType getHandType() {
        return this.handType;
    }
}
