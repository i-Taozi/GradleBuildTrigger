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

import com.google.common.base.MoreObjects;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import org.lanternpowered.server.game.Lantern;
import org.spongepowered.api.config.ConfigRoot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class LanternConfigRoot implements ConfigRoot {

    private final ObjectMapperFactory mapperFactory;
    private final String pluginName;
    private final Path baseDir;

    LanternConfigRoot(ObjectMapperFactory mapperFactory, String pluginName, Path baseDir) {
        this.mapperFactory = mapperFactory;
        this.pluginName = pluginName;
        this.baseDir = baseDir;
    }

    @Override
    public Path getConfigPath() {
        final Path configFile = this.baseDir.resolve(this.pluginName + ".conf");
        if (!Files.exists(this.baseDir)) {
            try {
                Files.createDirectories(this.baseDir);
            } catch (IOException e) {
                Lantern.getLogger().error("Failed to create plugin dir for {} at {}", this.pluginName, this.baseDir, e);
            }
        }
        return configFile;
    }

    @Override
    public ConfigurationLoader<CommentedConfigurationNode> getConfig() {
        return HoconConfigurationLoader.builder()
                .setPath(getConfigPath())
                .setDefaultOptions(ConfigurationOptions.defaults().setObjectMapperFactory(this.mapperFactory))
                .build();
    }

    @Override
    public Path getDirectory() {
        return this.baseDir;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("plugin", this.pluginName)
                .add("directory", this.baseDir)
                .add("configPath", this.baseDir.resolve(this.pluginName + ".conf"))
                .toString();
    }
}
