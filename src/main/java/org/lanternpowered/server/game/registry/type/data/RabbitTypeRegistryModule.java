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
package org.lanternpowered.server.game.registry.type.data;

import org.lanternpowered.server.data.type.LanternRabbitType;
import org.lanternpowered.server.game.registry.InternalPluginCatalogRegistryModule;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;

public class RabbitTypeRegistryModule extends InternalPluginCatalogRegistryModule<RabbitType> {

    public RabbitTypeRegistryModule() {
        super(RabbitTypes.class);
    }

    @Override
    public void registerDefaults() {
        register(new LanternRabbitType(CatalogKey.minecraft("brown"), 0));
        register(new LanternRabbitType(CatalogKey.minecraft("white"), 1));
        register(new LanternRabbitType(CatalogKey.minecraft("black"), 2));
        register(new LanternRabbitType(CatalogKey.minecraft("black_and_white"), 3));
        register(new LanternRabbitType(CatalogKey.minecraft("gold"), 4));
        register(new LanternRabbitType(CatalogKey.minecraft("salt_and_pepper"), 5));
        register(new LanternRabbitType(CatalogKey.minecraft("killer"), 99));
    }
}
