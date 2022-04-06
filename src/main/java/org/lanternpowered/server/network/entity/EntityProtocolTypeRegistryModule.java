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
package org.lanternpowered.server.network.entity;

import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.game.registry.DefaultCatalogRegistryModule;
import org.lanternpowered.server.network.entity.vanilla.ArmorStandEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.BatEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.ChickenEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.EnderDragonEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.EndermiteEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.ExperienceOrbEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.GiantEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.HumanEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.HuskEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.IronGolemEntityProcotol;
import org.lanternpowered.server.network.entity.vanilla.ItemEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.LightningEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.MagmaCubeEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.PaintingEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.PigEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.PlayerEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.RabbitEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.SheepEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.SilverfishEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.SlimeEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.SnowmanEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.VillagerEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.ZombieEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.ZombieVillagerEntityProtocol;
import org.spongepowered.api.CatalogKey;

public class EntityProtocolTypeRegistryModule extends DefaultCatalogRegistryModule<EntityProtocolType> {

    public EntityProtocolTypeRegistryModule() {
        super(EntityProtocolTypes.class);
    }

    @SuppressWarnings("Convert2MethodRef")
    @Override
    public void registerDefaults() {
        // Now you are probably thinking, use the method reference: ChickenEntityProtocol::new ??
        // well it's not working, at least not outside the development environment, java is throwing
        // "no such constructor" exceptions...
        // Tested with: oracle jre1.8.0_101
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("armor_stand"),
                entity -> new ArmorStandEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("bat"),
                entity -> new BatEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("chicken"),
                entity -> new ChickenEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("ender_dragon"),
                entity -> new EnderDragonEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("endermite"),
                entity -> new EndermiteEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("experience_orb"),
                entity -> new ExperienceOrbEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("giant"),
                entity -> new GiantEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("human"),
                entity -> new HumanEntityProtocol(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("husk"),
                entity -> new HuskEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("iron_golem"),
                entity -> new IronGolemEntityProcotol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("item"),
                entity -> new ItemEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("lightning"),
                entity -> new LightningEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("magma_cube"),
                entity -> new MagmaCubeEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("painting"),
                entity -> new PaintingEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("pig"),
                entity -> new PigEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("player"), LanternPlayer.class,
                entity -> new PlayerEntityProtocol(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("rabbit"),
                entity -> new RabbitEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("sheep"),
                entity -> new SheepEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("silverfish"),
                entity -> new SilverfishEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("slime"),
                entity -> new SlimeEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("snowman"),
                entity -> new SnowmanEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("villager"),
                entity -> new VillagerEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("zombie"),
                entity -> new ZombieEntityProtocol<>(entity)));
        register(LanternEntityProtocolType.of(CatalogKey.minecraft("zombie_villager"),
                entity -> new ZombieVillagerEntityProtocol<>(entity)));
    }
}
