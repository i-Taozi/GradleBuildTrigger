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
package org.lanternpowered.server.config;

import static org.lanternpowered.server.util.Conditions.checkPlugin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import org.lanternpowered.server.game.DirectoryKeys;
import org.lanternpowered.server.plugin.LanternPluginContainer;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Singleton
public final class LanternConfigManager implements ConfigManager {

    private final Path configFolder;

    @Inject
    public LanternConfigManager(@Named(DirectoryKeys.CONFIG) Path configFolder) {
        this.configFolder = configFolder;
    }

    @Override
    public ConfigRoot getSharedConfig(Object instance) {
        final PluginContainer pluginContainer = checkPlugin(instance, "instance");
        final String name = pluginContainer.getId().toLowerCase();
        return new LanternConfigRoot(getMapperFactory(pluginContainer), name, this.configFolder);
    }

    @Override
    public ConfigRoot getPluginConfig(Object instance) {
        final PluginContainer pluginContainer = checkPlugin(instance, "instance");
        final String name = pluginContainer.getId().toLowerCase();
        return new LanternConfigRoot(getMapperFactory(pluginContainer), name, this.configFolder.resolve(name));
    }

    private static ObjectMapperFactory getMapperFactory(PluginContainer container) {
        if (container instanceof LanternPluginContainer) {
            return ((LanternPluginContainer) container).getInjector().getInstance(GuiceObjectMapperFactory.class);
        }
        return DefaultObjectMapperFactory.getInstance();
    }
}
