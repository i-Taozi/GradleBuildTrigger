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
package org.lanternpowered.server.data.io.store;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Map;
import java.util.Optional;

public final class SimpleValueContainer {

    private final Map<Key<?>, Object> values;

    public SimpleValueContainer(Map<Key<?>, Object> values) {
        this.values = values;
    }

    /**
     * Gets the value for the specified {@link Key} if present.
     *
     * @param key The key
     * @param <E> The type of the value
     * @return The value if present
     */
    @SuppressWarnings("unchecked")
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return Optional.ofNullable((E) this.values.get(checkNotNull(key, "key")));
    }

    /**
     * Sets the value for the specified {@link Key} and
     * returns the old value if it was present.
     *
     * @param key The key
     * @param <E> The type of the value
     * @return The value if present
     */
    @SuppressWarnings("unchecked")
    public <E> Optional<E> set(Key<? extends BaseValue<E>> key, E value) {
        return Optional.ofNullable((E) this.values.put(checkNotNull(key, "key"), checkNotNull(value, "value")));
    }

    /**
     * Removes the value for the specified {@link Key} and
     * returns the old value if it was present.
     *
     * @param key The key
     * @param <E> The type of the value
     * @return The value if present
     */
    @SuppressWarnings("unchecked")
    public <E> Optional<E> remove(Key<? extends BaseValue<E>> key) {
        return Optional.ofNullable((E) this.values.remove(checkNotNull(key, "key")));
    }

    /**
     * Gets the {@link Map} with all the values.
     *
     * @return The values
     */
    public Map<Key<?>, Object> getValues() {
        return this.values;
    }
}
