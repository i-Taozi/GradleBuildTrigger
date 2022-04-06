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
package org.lanternpowered.server.game.registry.type.world;

import org.lanternpowered.server.game.registry.AdditionalPluginCatalogRegistryModule;
import org.lanternpowered.server.world.dimension.LanternDimensionEnd;
import org.lanternpowered.server.world.dimension.LanternDimensionNether;
import org.lanternpowered.server.world.dimension.LanternDimensionOverworld;
import org.lanternpowered.server.world.dimension.LanternDimensionType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;

@RegistrationDependency(GeneratorTypeRegistryModule.class)
public class DimensionTypeRegistryModule extends AdditionalPluginCatalogRegistryModule<DimensionType> {

    public DimensionTypeRegistryModule() {
        super(DimensionTypes.class);
    }

    @Override
    public void registerDefaults() {
        register(new LanternDimensionType<>(CatalogKey.minecraft("nether"), -1, LanternDimensionNether.class,
                GeneratorTypes.NETHER, true, true, false, false, LanternDimensionNether::new));
        register(new LanternDimensionType<>(CatalogKey.minecraft("overworld"), 0, LanternDimensionOverworld.class,
                GeneratorTypes.OVERWORLD, true, false, true, true, LanternDimensionOverworld::new));
        register(new LanternDimensionType<>(CatalogKey.minecraft("the_end"), 1, LanternDimensionEnd.class,
                GeneratorTypes.THE_END, true, false, false, false, LanternDimensionEnd::new));
    }
}
