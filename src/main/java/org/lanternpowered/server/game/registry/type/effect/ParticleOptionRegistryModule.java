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
package org.lanternpowered.server.game.registry.type.effect;

import com.flowpowered.math.vector.Vector3d;
import kotlin.jvm.functions.Function1;
import org.lanternpowered.server.effect.particle.LanternParticleOption;
import org.lanternpowered.server.game.registry.DefaultCatalogRegistryModule;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;

import java.util.List;

import javax.annotation.Nullable;

public class ParticleOptionRegistryModule extends DefaultCatalogRegistryModule<ParticleOption> {

    public ParticleOptionRegistryModule() {
        super(ParticleOptions.class);
    }

    @Override
    public void registerDefaults() {
        registerOption("block_state", BlockState.class);
        registerOption("color", Color.class);
        registerOption("direction", Direction.class);
        registerOption("firework_effects", List.class,
                value -> value.isEmpty() ? new IllegalArgumentException("The firework effects list may not be empty") : null);
        registerOption("quantity", Integer.class,
                value -> value < 1 ? new IllegalArgumentException("Quantity must be at least 1") : null);
        registerOption("item_stack_snapshot", ItemStackSnapshot.class);
        registerOption("note", NotePitch.class);
        registerOption("offset", Vector3d.class);
        registerOption("potion_effect_type", PotionEffectType.class);
        registerOption("scale", Double.class,
                value -> value < 0 ? new IllegalArgumentException("Scale may not be negative") : null);
        registerOption("velocity", Vector3d.class);
        registerOption("slow_horizontal_velocity", Boolean.class);
    }

    private <V> void registerOption(String id, Class<V> valueType) {
        registerOption(id, valueType, null);
    }

    private <V> void registerOption(String id, Class<V> valueType, @Nullable Function1<V, IllegalArgumentException> valueValidator) {
        register(new LanternParticleOption<>(CatalogKey.minecraft(id), valueType,
                valueValidator == null ? v -> null : valueValidator));
    }
}
