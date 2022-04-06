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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UnknownInventoryArchetype extends LanternInventoryArchetype<AbstractInventory> {

    public UnknownInventoryArchetype(CatalogKey key) {
        super(key);
    }

    @Override
    public AbstractArchetypeBuilder<AbstractInventory, ? super AbstractInventory, ?> getBuilder() {
        throw new IllegalStateException("The " + getName() + " inventory archetype cannot be constructed.");
    }

    @Override
    public List<InventoryArchetype> getChildArchetypes() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, InventoryProperty<String, ?>> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<InventoryProperty<String, ?>> getProperty(String key) {
        return Optional.empty();
    }

    @Override
    public <P extends InventoryProperty<String, ?>> Optional<P> getProperty(Class<P> property) {
        return Optional.empty();
    }

    @Override
    public <T extends InventoryProperty<String, ?>> Optional<T> getProperty(Class<T> type, String key) {
        return Optional.empty();
    }
}
