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
package org.lanternpowered.server.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Represents a weak reference to a {@link World}.
 */
public final class WeakWorldReference {

    @Nullable private WeakReference<World> world;
    private final UUID uniqueId;

    /**
     * Creates a new weak world reference.
     * 
     * @param world the world
     */
    public WeakWorldReference(World world) {
        this.world = new WeakReference<>(checkNotNull(world, "world"));
        this.uniqueId = world.getUniqueId();
    }

    /**
     * Creates a new weak world reference with the unique id of the world.
     * 
     * @param uniqueId the unique id
     */
    public WeakWorldReference(UUID uniqueId) {
        this.uniqueId = checkNotNull(uniqueId, "uniqueId");
    }

    /**
     * Gets the unique id of the world of this reference.
     * 
     * @return the unique id
     */
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    /**
     * Gets the world of this reference, this world may be
     * {@link Optional#empty()} if it couldn't be found.
     * 
     * @return the world if present, otherwise {@link Optional#empty()}
     */
    public Optional<World> getWorld() {
        World world = this.world == null ? null : this.world.get();
        if (world != null) {
            return Optional.of(world);
        }
        world = Sponge.getServer().getWorld(this.uniqueId).orElse(null);
        if (world != null) {
            this.world = new WeakReference<>(world);
            return Optional.of(world);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("uniqueId", this.uniqueId)
                .add("name", getWorld().map(World::getName).orElse(null))
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WeakWorldReference)) {
            return false;
        }
        final WeakWorldReference other = (WeakWorldReference) obj;
        return other.uniqueId.equals(this.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.uniqueId);
    }
}
