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
package org.lanternpowered.server.inventory;

import org.lanternpowered.server.catalog.DefaultCatalogType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;

import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class LanternInventoryArchetype<T extends AbstractInventory> extends DefaultCatalogType
        implements InventoryArchetype, InventoryPropertyHolder {

    LanternInventoryArchetype(CatalogKey key) {
        super(key);
    }

    public abstract AbstractArchetypeBuilder<T, ? super T, ?> getBuilder();

    @Override
    public abstract <P extends InventoryProperty<String, ?>> Optional<P> getProperty(Class<P> property);

    /**
     * Constructs a {@link AbstractInventory}.
     *
     * @return The inventory
     */
    public T build() {
        return getBuilder().build0(false, null, this);
    }

    /**
     * Constructs a {@link LanternInventoryBuilder}
     * with this archetype.
     *
     * @return The inventory builder
     */
    public LanternInventoryBuilder<T> builder() {
        return LanternInventoryBuilder.create().of(this);
    }
}
