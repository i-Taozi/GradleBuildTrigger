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
package org.lanternpowered.server.network.status;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.lanternpowered.server.game.version.LanternMinecraftVersion;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import javax.annotation.Nullable;

public class LanternStatusResponse implements ClientPingServerEvent.Response {

    private MinecraftVersion version;
    private Optional<Favicon> favicon;
    private Text description;
    private Players players;

    private boolean hidePlayers;

    public LanternStatusResponse(MinecraftVersion version, Optional<Favicon> favicon, Text description, Players players) {
        this.description = checkNotNull(description, "description");
        this.favicon = checkNotNull(favicon, "favicon");
        this.version = checkNotNull(version, "version");
        this.players = checkNotNull(players, "players");
    }

    /**
     * Sets the {@link MinecraftVersion} of the status response.
     *
     * <p>The name of this version will be displayed on the client when the
     * server or the client is outdated.</p>
     *
     * @param version The version
     */
    public void setVersion(MinecraftVersion version) {
        this.version = checkNotNull(version, "version");
    }

    /**
     * Sets the {@link MinecraftVersion} of the status response.
     *
     * <p>The name of this version will be displayed on the client when the
     * server or the client is outdated.</p>
     *
     * @param name The name
     * @param protocol The protocol version
     * @param legacy Whether the version is legacy
     */
    public void setVersion(String name, int protocol, boolean legacy) {
        setVersion(new LanternMinecraftVersion(name, protocol, legacy));
    }

    @Override
    public Text getDescription() {
        return this.description;
    }

    @Override
    public MinecraftVersion getVersion() {
        return this.version;
    }

    @Override
    public Optional<Favicon> getFavicon() {
        return this.favicon;
    }

    @Override
    public void setDescription(Text description) {
        this.description = checkNotNull(description, "description");
    }

    @Override
    public void setHidePlayers(boolean hide) {
        this.hidePlayers = hide;
    }

    @Override
    public void setFavicon(@Nullable Favicon favicon) {
        this.favicon = Optional.ofNullable(favicon);
    }

    @Override
    public Optional<Players> getPlayers() {
        return this.hidePlayers ? Optional.empty() : Optional.of(this.players);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("version", this.version)
                .add("description", this.description)
                .add("players", this.players)
                .add("hidePlayers", this.hidePlayers)
                .add("favicon", this.favicon.orElse(null))
                .toString();
    }
}
