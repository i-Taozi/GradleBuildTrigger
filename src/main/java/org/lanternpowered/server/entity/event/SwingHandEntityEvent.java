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
package org.lanternpowered.server.entity.event;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;

public final class SwingHandEntityEvent implements EntityEvent {

    public static SwingHandEntityEvent of(HandType handType) {
        checkNotNull(handType, "handType");
        return handType == HandTypes.MAIN_HAND ? Holder.MAIN_HAND :
                handType == HandTypes.OFF_HAND ? Holder.OFF_HAND : new SwingHandEntityEvent(handType);
    }

    private final HandType handType;

    private SwingHandEntityEvent(HandType handType) {
        this.handType = handType;
    }

    public HandType getHandType() {
        return this.handType;
    }

    @Override
    public EntityEventType type() {
        return EntityEventType.ALIVE;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("handType", this.handType.getKey()).toString();
    }

    private static class Holder {
        static final SwingHandEntityEvent MAIN_HAND = new SwingHandEntityEvent(HandTypes.MAIN_HAND);
        static final SwingHandEntityEvent OFF_HAND = new SwingHandEntityEvent(HandTypes.OFF_HAND);
    }
}
