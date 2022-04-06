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
package org.lanternpowered.server.config.world.chunk;

import static org.lanternpowered.server.config.ConfigConstants.DEFAULTS;
import static org.lanternpowered.server.config.ConfigConstants.ENABLED;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WorldChunkLoading extends ChunkLoading {

    @Setting(value = ENABLED, comment =
            "Whether this configuration file should override the globally specified chunk\n " +
            "loading settings, if set false, this sections won't affect anything.")
    private boolean enabled = false;

    @Setting(value = DEFAULTS, comment = "Default configuration for chunk loading control.")
    private WorldChunkLoadingTickets defaults = new WorldChunkLoadingTickets();

    /**
     * Gets whether this section is enabled.
     * 
     * @return is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    protected WorldChunkLoadingTickets getDefaults() {
        return this.defaults;
    }

}
