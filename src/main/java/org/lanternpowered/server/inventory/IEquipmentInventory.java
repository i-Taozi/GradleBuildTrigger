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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.Property;
import org.spongepowered.api.entity.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

import java.util.Optional;

public interface IEquipmentInventory extends ICarriedInventory<Equipable>, EquipmentInventory {

    @Override
    default Optional<ItemStack> poll(EquipmentSlotType equipmentType) {
        return getSlot(equipmentType).map(Inventory::poll);
    }

    @Override
    default Optional<ItemStack> poll(EquipmentSlotType equipmentType, int limit) {
        return getSlot(equipmentType).map(slot -> slot.poll(limit));
    }

    @Override
    default Optional<ItemStack> poll(EquipmentType equipmentType) {
        return getSlot(equipmentType).map(Inventory::poll);
    }

    @Override
    default Optional<ItemStack> poll(EquipmentType equipmentType, int limit) {
        return getSlot(equipmentType).map(slot -> slot.poll(limit));
    }

    @Override
    default Optional<ItemStack> peek(EquipmentSlotType equipmentType) {
        return getSlot(equipmentType).map(Inventory::peek);
    }

    @Override
    default Optional<ItemStack> peek(EquipmentSlotType equipmentType, int limit) {
        return getSlot(equipmentType).map(slot -> slot.peek(limit));
    }

    @Override
    default Optional<ItemStack> peek(EquipmentType equipmentType) {
        return getSlot(equipmentType).map(Inventory::peek);
    }

    @Override
    default Optional<ItemStack> peek(EquipmentType equipmentType, int limit) {
        return getSlot(equipmentType).map(slot -> slot.peek(limit));
    }

    @Override
    default InventoryTransactionResult set(EquipmentSlotType equipmentType, ItemStack stack) {
        checkNotNull(equipmentType, "equipmentType");
        return getSlot(equipmentType).map(slot -> slot.set(stack))
                .orElseGet(() -> InventoryTransactionResult.builder()
                        .type(InventoryTransactionResult.Type.FAILURE)
                        .reject(LanternItemStack.toSnapshot(stack).createStack())
                        .build());
    }

    @Override
    default InventoryTransactionResult set(EquipmentType equipmentType, ItemStack stack) {
        checkNotNull(equipmentType, "equipmentType");
        return getSlot(equipmentType).map(slot -> slot.set(stack))
                .orElseGet(() -> InventoryTransactionResult.builder()
                        .type(InventoryTransactionResult.Type.FAILURE)
                        .reject(LanternItemStack.toSnapshot(stack).createStack())
                        .build());
    }

    @Override
    default Optional<Slot> getSlot(EquipmentSlotType equipmentType) {
        checkNotNull(equipmentType, "equipmentType");
        if (equipmentType.getValue() == null || equipmentType.getOperator() != Property.Operator.EQUAL) {
            return Optional.empty();
        }
        return getSlot(equipmentType.getValue());
    }

    /**
     * Get the {@link Slot} for the specified equipment type.
     *
     * @param equipmentType Type of equipment slot to set
     * @return The matching slot or {@link Optional#empty()} if no matching slot
     */
    @SuppressWarnings("unchecked")
    default Optional<Slot> getSlot(EquipmentType equipmentType) {
        checkNotNull(equipmentType, "equipmentType");
        return this.slots().stream().filter(s -> ((AbstractSlot) s).isValidItem(equipmentType)).findFirst();
    }
}
