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
package org.lanternpowered.server.world.chunk;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.lanternpowered.server.util.Conditions.checkPlugin;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lanternpowered.server.config.GlobalConfig;
import org.lanternpowered.server.world.LanternWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class LanternChunkTicketManager implements ChunkTicketManager {

    private final Multimap<String, Callback> callbacks = HashMultimap.create();
    private final Provider<GlobalConfig> globalConfig;

    @Inject
    private LanternChunkTicketManager(Provider<GlobalConfig> globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * Gets all the registered callbacks.
     * 
     * @return The callbacks
     */
    public Multimap<String, Callback> getCallbacks() {
        return ImmutableMultimap.copyOf(this.callbacks);
    }

    /**
     * Gets the maximum amount of tickets the player can have.
     * 
     * @param playerUUID The player unique id
     * @return The maximum amount of tickets
     */
    public int getMaxTicketsForPlayer(UUID playerUUID) {
        return this.globalConfig.get().getPlayerTicketCount();
    }

    @Override
    public void registerCallback(Object plugin, Callback callback) {
        this.callbacks.put(checkPlugin(plugin, "plugin").getId(), checkNotNull(callback, "callback"));
    }

    @Override
    public Optional<LoadingTicket> createTicket(Object plugin, World world) {
        return ((LanternWorld) checkNotNull(world, "world")).getChunkManager().createTicket(plugin);
    }

    @Override
    public Optional<EntityLoadingTicket> createEntityTicket(Object plugin, World world) {
        return ((LanternWorld) checkNotNull(world, "world")).getChunkManager().createEntityTicket(plugin);
    }

    @Override
    public Optional<PlayerLoadingTicket> createPlayerTicket(Object plugin, World world, UUID player) {
        return ((LanternWorld) checkNotNull(world, "world")).getChunkManager().createPlayerTicket(plugin, player);
    }

    @Override
    public Optional<PlayerEntityLoadingTicket> createPlayerEntityTicket(Object plugin, World world, UUID player) {
        return ((LanternWorld) checkNotNull(world, "world")).getChunkManager().createPlayerEntityTicket(plugin, player);
    }

    @Override
    public int getMaxTickets(Object plugin) {
        return this.getMaxTicketsById(checkPlugin(plugin, "plugin").getId());
    }

    public int getMaxTicketsById(String plugin) {
        return this.globalConfig.get().getChunkLoadingTickets(checkNotNull(plugin, "plugin")).getMaximumTicketCount();
    }

    @Override
    public int getAvailableTickets(Object plugin, World world) {
        final LanternChunkManager chunkManager = ((LanternWorld) checkNotNull(world, "world")).getChunkManager();
        final String pluginId = checkPlugin(plugin, "plugin").getId();
        return chunkManager.getMaxTicketsForPlugin(pluginId) - chunkManager.getTicketsForPlugin(pluginId);
    }

    @Override
    public int getAvailableTickets(UUID player) {
        checkNotNull(player, "player");
        int count = 0;
        for (World world : Sponge.getServer().getWorlds()) {
            count += ((LanternWorld) world).getChunkManager().getTicketsForPlayer(player);
        }
        return this.getMaxTicketsForPlayer(player) - count;
    }

    @Override
    public ImmutableSetMultimap<Vector3i, LoadingTicket> getForcedChunks(World world) {
        return ((LanternWorld) checkNotNull(world, "world")).getChunkManager().getForced();
    }

}
