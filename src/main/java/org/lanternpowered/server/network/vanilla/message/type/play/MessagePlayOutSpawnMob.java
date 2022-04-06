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
import org.lanternpowered.server.network.entity.parameter.ParameterList;
import org.lanternpowered.server.network.message.Message;

import java.util.UUID;

public final class MessagePlayOutSpawnMob implements Message {

    private final int entityId;
    private final UUID uniqueId;
    private final int mobType;
    private final Vector3d position;
    private final byte yaw;
    private final byte pitch;
    private final byte headPitch;
    private final Vector3d velocity;
    private final ParameterList parameterList;

    public MessagePlayOutSpawnMob(int entityId, UUID uniqueId, int mobType, Vector3d position, byte yaw, byte pitch,
            byte headPitch, Vector3d velocity, ParameterList parameterList) {
        this.entityId = entityId;
        this.uniqueId = uniqueId;
        this.mobType = mobType;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headPitch = headPitch;
        this.velocity = velocity;
        this.parameterList = parameterList;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public int getMobType() {
        return this.mobType;
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public byte getYaw() {
        return this.yaw;
    }

    public byte getPitch() {
        return this.pitch;
    }

    public byte getHeadPitch() {
        return this.headPitch;
    }

    public Vector3d getVelocity() {
        return this.velocity;
    }

    public ParameterList getParameterList() {
        return this.parameterList;
    }
}
