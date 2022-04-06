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
package org.lanternpowered.server.text.action;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.action.TextActions;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class LanternClickActionCallbacks {

    /**
     * The base of a {@link TextActions#executeCallback(Consumer)} command line.
     */
    public static final String COMMAND_BASE = "/lantern:textclickcallback ";

    /**
     * The pattern of a valid {@link TextActions#executeCallback(Consumer)} command line.
     */
    public static final Pattern COMMAND_PATTERN = Pattern.compile(
        "^/lantern:textclickcallback ([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$");

    private static final LanternClickActionCallbacks INSTANCE = new LanternClickActionCallbacks();

    public static LanternClickActionCallbacks get() {
        return INSTANCE;
    }

    private final Map<UUID, Consumer<CommandSource>> reverseMap = new ConcurrentHashMap<>();
    private final LoadingCache<Consumer<CommandSource>, UUID> callbackCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener((RemovalListener<Consumer<CommandSource>, UUID>) (key, value, cause) -> this.reverseMap.remove(value, key))
            .build((CacheLoader<Consumer<CommandSource>, UUID>) key -> {
                final UUID ret = UUID.randomUUID();
                this.reverseMap.putIfAbsent(ret, key);
                return ret;
            });

    /**
     * Gets or generates a {@link UUID} for the specified callback.
     *
     * @param callback The callback
     * @return The unique id
     */
    public UUID getOrCreateIdForCallback(Consumer<CommandSource> callback) {
        return this.callbackCache.get(checkNotNull(callback, "callback"));
    }

    /**
     * Gets the callback for the specified {@link UUID}, may return {@link Optional#empty()}
     * if there is no callback for the specified unique id.
     *
     * @param uniqueId The unique id
     * @return The callback
     */
    public Optional<Consumer<CommandSource>> getCallbackForUUID(UUID uniqueId) {
        return Optional.ofNullable(this.reverseMap.get(checkNotNull(uniqueId, "uniqueId")));
    }
}
