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
package org.lanternpowered.server.data.value.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.lanternpowered.server.data.key.LanternKey;
import org.lanternpowered.server.data.value.immutable.ImmutableLanternOptionalValue;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked", "ConstantConditions"})
public class LanternOptionalValue<E> extends LanternValue<Optional<E>> implements OptionalValue<E> {

    public LanternOptionalValue(Key<? extends BaseValue<Optional<E>>> key) {
        this(key, Optional.empty());
    }

    public LanternOptionalValue(Key<? extends BaseValue<Optional<E>>> key, Optional<E> actualValue) {
        this(key, Optional.empty(), actualValue);
    }

    public LanternOptionalValue(Key<? extends BaseValue<Optional<E>>> key, Optional<E> defaultValue, Optional<E> actualValue) {
        super(key, defaultValue, actualValue);
    }

    @Override
    public OptionalValue<E> set(Optional<E> value) {
        this.actualValue = checkNotNull(value);
        return this;
    }

    @Override
    public OptionalValue<E> transform(Function<Optional<E>, Optional<E>> function) {
        this.actualValue = checkNotNull(function.apply(this.actualValue));
        return this;
    }

    @Override
    public ImmutableOptionalValue<E> asImmutable() {
        return new ImmutableLanternOptionalValue<>(getKey(), getDefault(), getActualValue());
    }

    @Override
    public OptionalValue<E> setTo(@Nullable E value) {
        return set(Optional.ofNullable(value));
    }

    @Override
    public Value<E> or(E defaultValue) {
        checkNotNull(defaultValue);
        return new LanternValue<>(((LanternKey) getKey()).getOptionalUnwrappedKey(), defaultValue, get().orElse(defaultValue));
    }

    @Override
    public LanternOptionalValue<E> copy() {
        return new LanternOptionalValue<>(getKey(), getDefault(), getActualValue());
    }
}
