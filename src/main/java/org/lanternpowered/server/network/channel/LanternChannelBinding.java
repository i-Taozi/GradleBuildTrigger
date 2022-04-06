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
package org.lanternpowered.server.network.channel;

import com.google.common.base.MoreObjects;
import org.lanternpowered.server.network.buffer.ByteBuffer;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;

abstract class LanternChannelBinding implements ChannelBinding {

    private final LanternChannelRegistrar registrar;
    private final PluginContainer owner;
    private final String name;

    boolean bound;

    LanternChannelBinding(LanternChannelRegistrar registrar, String name, PluginContainer owner) {
        this.registrar = registrar;
        this.owner = owner;
        this.name = name;
    }

    @Override
    public LanternChannelRegistrar getRegistrar() {
        return this.registrar;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PluginContainer getOwner() {
        return this.owner;
    }

    abstract void handlePayload(ByteBuffer buf, RemoteConnection connection);

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("plugin", this.owner)
                .add("name", this.name)
                .toString();
    }
}
