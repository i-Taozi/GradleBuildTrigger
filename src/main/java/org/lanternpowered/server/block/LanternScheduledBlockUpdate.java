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
package org.lanternpowered.server.block;

import com.google.common.base.MoreObjects;
import org.lanternpowered.server.game.LanternGame;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LanternScheduledBlockUpdate implements ScheduledBlockUpdate, Comparable<LanternScheduledBlockUpdate> {

    private final Location<World> location;

    private long endTicks;
    private int priority;
    private int entryId;

    public LanternScheduledBlockUpdate(int entryId, Location<World> location, int ticks, int priority) {
        this.setTicks(ticks);
        this.priority = priority;
        this.location = location;
        this.entryId = entryId;
    }

    @Override
    public Location<World> getLocation() {
        return this.location;
    }

    @Override
    public int getTicks() {
        return (int) (this.endTicks - LanternGame.currentTimeTicks());
    }

    @Override
    public void setTicks(int ticks) {
        this.endTicks = LanternGame.currentTimeTicks() + ticks;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(LanternScheduledBlockUpdate o) {
        if (this.endTicks < o.endTicks) {
            return -1;
        }
        if (this.endTicks > o.endTicks) {
            return 1;
        }
        if (this.priority != o.priority) {
            return this.priority - o.priority;
        }
        if (this.entryId < o.entryId) {
            return -1;
        }
        if (this.entryId > o.entryId) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("entryId", this.entryId)
                .add("location", this.location)
                .add("ticks", this.getTicks())
                .add("priority", this.priority)
                .toString();
    }
}
