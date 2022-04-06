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
package org.lanternpowered.server.config.user;

import com.google.common.collect.ImmutableList;
import ninja.leaping.configurate.ConfigurationOptions;
import org.lanternpowered.server.config.ConfigBase;
import org.spongepowered.api.profile.GameProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class UserConfig<T extends UserEntry> extends ConfigBase implements UserStorage<T> {

    private final Map<UUID, T> byUUID = new ConcurrentHashMap<>();
    private final Map<String, T> byName = new ConcurrentHashMap<>();

    public UserConfig(Path path, boolean hocon) throws IOException {
        super(path, hocon);
    }

    public UserConfig(Path path, ConfigurationOptions options, boolean hocon) throws IOException {
        super(path, options, hocon);
    }

    protected abstract List<T> getBackingList();

    @Override
    public void save() throws IOException {
        synchronized (this) {
            getBackingList().clear();
            for (T entry : this.byUUID.values()) {
                getBackingList().add(entry);
            }
            super.save();
        }
    }

    @Override
    public void load() throws IOException {
        synchronized (this) {
            super.load();
            this.byUUID.clear();
            this.byName.clear();
            for (T entry : getBackingList()) {
                this.byUUID.put(entry.getProfile().getUniqueId(), entry);
                final Optional<String> optName = entry.getProfile().getName();
                optName.ifPresent(s -> this.byName.put(s.toLowerCase(), entry));
            }
        }
    }

    @Override
    public Optional<T> getEntryByUUID(UUID uniqueId) {
        return Optional.ofNullable(this.byUUID.get(uniqueId));
    }

    @Override
    public Optional<T> getEntryByName(String username) {
        return Optional.ofNullable(this.byName.get(username.toLowerCase()));
    }

    @Override
    public Optional<T> getEntryByProfile(GameProfile gameProfile) {
        return getEntryByUUID(gameProfile.getUniqueId());
    }

    @Override
    public void addEntry(T entry) {
        final GameProfile gameProfile = entry.getProfile();
        this.byUUID.put(gameProfile.getUniqueId(), entry);
        final Optional<String> optName = entry.getProfile().getName();
        optName.ifPresent(s -> this.byName.put(s.toLowerCase(), entry));
    }

    @Override
    public boolean removeEntry(UUID uniqueId) {
        T entry = this.byUUID.remove(uniqueId);
        if (entry != null) {
            final Optional<String> optName = entry.getProfile().getName();
            optName.ifPresent(s -> this.byName.remove(s.toLowerCase()));
            return true;
        }
        return false;
    }

    @Override
    public Collection<T> getEntries() {
        return ImmutableList.copyOf(this.byUUID.values());
    }

}
