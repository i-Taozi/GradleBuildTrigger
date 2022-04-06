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

import static org.lanternpowered.server.text.translation.TranslationHelper.tr;

import com.google.common.collect.ImmutableList;
import org.lanternpowered.server.game.Lantern;
import org.lanternpowered.server.inventory.property.LanternIdentifiable;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.Identifiable;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
class LanternEmptyInventory extends AbstractInventory implements EmptyInventory, IQueryInventory {

    static class Name {
        static final Translation INSTANCE = tr("inventory.empty.name");
    }

    private static final Identifiable EMPTY_IDENTIFIABLE = new LanternIdentifiable(new UUID(0L, 0L), Property.Operator.DELEGATE);

    @Override
    public EmptyInventory empty() {
        return this;
    }

    @Override
    public void addChangeListener(SlotChangeListener listener) {
    }

    @Override
    public void addViewListener(InventoryViewerListener listener) {
    }

    @Override
    public void addCloseListener(InventoryCloseListener listener) {
    }

    @Override
    public LanternItemStack poll(ItemType itemType) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack poll(Predicate<ItemStack> matcher) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack poll(int limit, ItemType itemType) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack poll(int limit, Predicate<ItemStack> matcher) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack peek(ItemType itemType) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack peek(Predicate<ItemStack> matcher) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack peek(int limit, ItemType itemType) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack peek(int limit, Predicate<ItemStack> matcher) {
        return LanternItemStack.empty();
    }

    @Override
    public PeekedOfferTransactionResult peekOffer(ItemStack stack) {
        return new PeekedOfferTransactionResult(ImmutableList.of(), stack.createSnapshot());
    }

    @Override
    public PeekedPollTransactionResult peekPoll(Predicate<ItemStack> matcher) {
        return PeekedPollTransactionResult.empty();
    }

    @Override
    public PeekedPollTransactionResult peekPoll(int limit, Predicate<ItemStack> matcher) {
        return PeekedPollTransactionResult.empty();
    }

    @Override
    public PeekedSetTransactionResult peekSet(ItemStack stack) {
        return new PeekedSetTransactionResult(ImmutableList.of(), stack.createSnapshot());
    }

    @Override
    protected List<AbstractSlot> getSlots() {
        return Collections.emptyList();
    }

    @Override
    protected List<? extends AbstractInventory> getChildren() {
        return Collections.emptyList();
    }

    @Override
    protected void peekOffer(ItemStack stack, @Nullable Consumer<SlotTransaction> transactionAdder) {
    }

    @Override
    protected void offer(ItemStack stack, @Nullable Consumer<SlotTransaction> transactionAdder) {
    }

    @Override
    protected void set(ItemStack stack, boolean force, @Nullable Consumer<SlotTransaction> transactionAdder) {
    }

    @Override
    protected ViewableInventory toViewable() {
        return null;
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        return false;
    }

    @Override
    public IInventory intersect(Inventory inventory) {
        return this;
    }

    @Override
    public IInventory union(Inventory inventory) {
        return inventory instanceof EmptyInventory ? this : (IInventory) inventory;
    }

    @Override
    public boolean containsInventory(Inventory inventory) {
        return false;
    }

    @Override
    public LanternItemStack poll() {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack poll(int limit) {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack peek() {
        return LanternItemStack.empty();
    }

    @Override
    public LanternItemStack peek(int limit) {
        return LanternItemStack.empty();
    }

    @Override
    public InventoryTransactionResult offer(ItemStack stack) {
        return CachedInventoryTransactionResults.FAIL_NO_TRANSACTIONS;
    }

    @Override
    public boolean canFit(ItemStack stack) {
        return false;
    }

    @Override
    public InventoryTransactionResult setForced(@Nullable ItemStack stack) {
        return CachedInventoryTransactionResults.FAIL_NO_TRANSACTIONS;
    }

    @Override
    public Optional<ISlot> getSlot(int index) {
        return Optional.empty();
    }

    @Override
    public int getSlotIndex(Slot slot) {
        return INVALID_SLOT_INDEX;
    }

    @Override
    public InventoryTransactionResult set(@Nullable ItemStack stack) {
        return CachedInventoryTransactionResults.FAIL_NO_TRANSACTIONS;
    }

    @Override
    public void clear() {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int totalItems() {
        return 0;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean contains(ItemStack stack) {
        return false;
    }

    @Override
    public boolean contains(ItemType type) {
        return false;
    }

    @Override
    public boolean containsAny(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setMaxStackSize(int size) {
    }

    @Override
    public PluginContainer getPlugin() {
        // Use the plugin container from the parent if possible
        final AbstractInventory parent = parent();
        return parent == this ? Lantern.getMinecraftPlugin() : parent.getPlugin();
    }

    @Override
    public InventoryArchetype getArchetype() {
        return LanternInventoryArchetypes.EMPTY;
    }

    @Override
    public Translation getName() {
        return Name.INSTANCE;
    }

    @Override
    protected void queryInventories(QueryInventoryAdder adder) {
    }

    @Override
    protected <T extends InventoryProperty<?, ?>> Optional<T> tryGetProperty(Class<T> property, @Nullable Object key) {
        if (property == Identifiable.class) {
            return Optional.of((T) EMPTY_IDENTIFIABLE);
        }
        return super.tryGetProperty(property, key);
    }

    @Override
    protected <T extends InventoryProperty<?, ?>> List<T> tryGetProperties(Class<T> property) {
        final List<T> properties = super.tryGetProperties(property);
        if (property == Identifiable.class) {
            properties.add((T) EMPTY_IDENTIFIABLE);
        }
        return properties;
    }
}
