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
package org.lanternpowered.server.network.entity.vanilla;

import static org.lanternpowered.server.network.vanilla.message.codec.play.CodecUtils.wrapAngle;

import com.flowpowered.math.vector.Vector3d;
import org.lanternpowered.server.entity.LanternEntity;
import org.lanternpowered.server.entity.LanternLiving;
import org.lanternpowered.server.network.entity.EntityProtocolUpdateContext;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutSpawnMob;

public abstract class CreatureEntityProtocol<E extends LanternEntity> extends LivingEntityProtocol<E> {

    public CreatureEntityProtocol(E entity) {
        super(entity);
    }

    /**
     * Gets the mob type.
     *
     * @return The mob type
     */
    protected abstract int getMobType();

    @Override
    protected void spawn(EntityProtocolUpdateContext context) {
        final Vector3d rot = this.entity.getRotation();
        final Vector3d headRot = this.entity instanceof LanternLiving ? ((LanternLiving) this.entity).getHeadRotation() : null;
        final Vector3d pos = this.entity.getPosition();
        final Vector3d vel = this.entity.getVelocity();

        final double yaw = rot.getY();
        final double pitch = headRot != null ? headRot.getX() : rot.getX();
        final double headYaw = headRot != null ? headRot.getY() : 0;

        context.sendToAllExceptSelf(() -> new MessagePlayOutSpawnMob(getRootEntityId(), this.entity.getUniqueId(), getMobType(),
                pos, wrapAngle(yaw), wrapAngle(pitch), wrapAngle(headYaw), vel, fillSpawnParameters()));
        spawnWithEquipment(context);
    }
}
