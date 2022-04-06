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
package org.lanternpowered.server.inventory.behavior;

import org.lanternpowered.api.cause.CauseStack;
import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.event.LanternEventHelper;
import org.lanternpowered.server.game.Lantern;
import org.lanternpowered.server.inventory.AbstractInventory;
import org.lanternpowered.server.inventory.AbstractSlot;
import org.lanternpowered.server.inventory.IInventory;
import org.lanternpowered.server.inventory.ISlot;
import org.lanternpowered.server.inventory.LanternItemStack;
import org.lanternpowered.server.inventory.LanternItemStackSnapshot;
import org.lanternpowered.server.inventory.PeekedOfferTransactionResult;
import org.lanternpowered.server.inventory.PeekedPollTransactionResult;
import org.lanternpowered.server.inventory.PeekedSetTransactionResult;
import org.lanternpowered.server.inventory.PlayerInventoryContainer;
import org.lanternpowered.server.inventory.PlayerTopBottomContainer;
import org.lanternpowered.server.inventory.client.ClientContainer;
import org.lanternpowered.server.inventory.client.ClientSlot;
import org.lanternpowered.server.inventory.query.QueryOperations;
import org.lanternpowered.server.inventory.transformation.InventoryTransforms;
import org.lanternpowered.server.inventory.vanilla.LanternHotbarInventory;
import org.lanternpowered.server.item.predicate.ItemPredicates;
import org.lanternpowered.server.item.recipe.crafting.CraftingMatrix;
import org.lanternpowered.server.item.recipe.crafting.ExtendedCraftingResult;
import org.lanternpowered.server.item.recipe.crafting.MatrixResult;
import org.lanternpowered.server.world.LanternWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingOutput;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.slot.OutputSlot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingResult;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class VanillaContainerInteractionBehavior extends AbstractContainerInteractionBehavior {

    private final PlayerTopBottomContainer container;

    public VanillaContainerInteractionBehavior(PlayerTopBottomContainer container) {
        this.container = container;
    }

    /**
     * Sets the cursor item.
     *
     * @param cursorItem The cursor item
     */
    private void setCursorItem(ItemStack cursorItem) {
        this.container.getCursorSlot().setRawItemStack(cursorItem);
    }

    /**
     * Gets the {@link ItemStack} in the cursor.
     *
     * @return The cursor item
     */
    private LanternItemStack getCursorItem() {
        return this.container.getCursorSlot().getRawItemStack();
    }

    @Override
    public void handleShiftClick(ClientContainer clientContainer, ClientSlot clientSlot, MouseButton mouseButton) {
        final LanternPlayer player = clientContainer.getPlayer();
        if (player != this.container.getPlayerInventory().getCarrier().orElse(null) ||
                !(clientSlot instanceof ClientSlot.Slot) || mouseButton == MouseButton.MIDDLE) {
            return;
        }
        final AbstractSlot slot = ((ClientSlot.Slot) clientSlot).getSlot();
        final ItemStack itemStack = slot.peek();

        final Transaction<ItemStackSnapshot> cursorTransaction;
        final List<SlotTransaction> transactions = new ArrayList<>();

        if (slot instanceof CraftingOutput) {
            final ItemStackSnapshot cursorItem = LanternItemStack.toSnapshot(getCursorItem());
            cursorTransaction = new Transaction<>(cursorItem, cursorItem);

            final AbstractInventory parent = slot.parent();
            if (parent instanceof CraftingInventory) {
                final CraftingInventory inventory = (CraftingInventory) parent;
                final Optional<ExtendedCraftingResult> optResult = Lantern.getRegistry().getCraftingRecipeRegistry()
                        .getExtendedResult(inventory.getCraftingGrid(), player.getWorld());
                if (optResult.isPresent()) {
                    final ExtendedCraftingResult result = optResult.get();
                    final ItemStackSnapshot resultItem = result.getResult().getResult();

                    int times = result.getMaxTimes();
                    final ItemStack itemStack1 = resultItem.createStack();
                    final int quantity = times * itemStack1.getQuantity();
                    itemStack1.setQuantity(quantity);

                    final AbstractInventory targetInventory = (AbstractInventory) this.container.getPlayerInventory()
                            .getPrimary().transform(InventoryTransforms.REVERSE);
                    PeekedOfferTransactionResult peekResult = targetInventory.peekOffer(itemStack1);

                    if (!peekResult.isEmpty()) {
                        transactions.add(new SlotTransaction(slot, resultItem, ItemStackSnapshot.NONE));

                        if (!itemStack1.isEmpty()) {
                            final int added = quantity - itemStack1.getQuantity();
                            times = added / resultItem.getQuantity();
                            final int diff = added % resultItem.getQuantity();
                            if (diff != 0) {
                                itemStack1.setQuantity(resultItem.getQuantity() * times);
                                peekResult = targetInventory.peekOffer(itemStack1);
                            }
                        }

                        transactions.addAll(peekResult.getTransactions());
                        updateCraftingGrid(player, inventory, result.getMatrixResult(times), transactions);
                    }
                } else {
                    // No actual transaction, there shouldn't have been a item in the crafting result slot
                    transactions.add(new SlotTransaction(slot, ItemStackSnapshot.NONE, ItemStackSnapshot.NONE));
                }
            } else {
                Lantern.getLogger().warn("Found a CraftingOutput slot without a CraftingInventory as parent.");
                return;
            }
        } else {
            final ItemStackSnapshot cursorItem = LanternItemStack.toSnapshot(getCursorItem());
            cursorTransaction = new Transaction<>(cursorItem, cursorItem);

            if (!itemStack.isEmpty()) {
                final IInventory target = this.container.getOpenInventory().getShiftClickBehavior().getTarget(this.container, slot);
                if (target != null) {
                    final LanternItemStack itemStack1 = (LanternItemStack) itemStack.copy();
                    final PeekedOfferTransactionResult result = target.peekOffer(itemStack1);
                    if (!result.isEmpty()) {
                        transactions.addAll(result.getTransactions());
                        if (itemStack1.isEmpty()) {
                            transactions.addAll(slot.peekPoll(
                                    itemStack.getQuantity() - itemStack1.getQuantity(), stack -> true).getTransactions());
                        } else {
                            transactions.addAll(slot.peekPoll(stack -> true).getTransactions());
                        }
                    }
                }
            }
        }

        final List<SlotTransaction> transactions1 = this.container.transformSlots(transactions);
        final CauseStack causeStack = CauseStack.current();
        final ClickInventoryEvent.Shift event;
        if (mouseButton == MouseButton.LEFT) {
            event = SpongeEventFactory.createClickInventoryEventShiftPrimary(
                    causeStack.getCurrentCause(), cursorTransaction, this.container, transactions1);
        } else {
            event = SpongeEventFactory.createClickInventoryEventShiftSecondary(
                    causeStack.getCurrentCause(), cursorTransaction, this.container, transactions1);
        }
        finishInventoryEvent(event);
    }

    @Override
    public void handleDoubleClick(ClientContainer clientContainer, ClientSlot clientSlot) {
        final LanternPlayer player = clientContainer.getPlayer();
        if (player != this.container.getPlayerInventory().getCarrier().orElse(null) ||
                !(clientSlot instanceof ClientSlot.Slot)) {
            return;
        }
        final AbstractSlot slot = ((ClientSlot.Slot) clientSlot).getSlot();
        final ItemStackSnapshot oldItem = LanternItemStack.toSnapshot(getCursorItem());
        ItemStackSnapshot newItem = oldItem;

        final List<SlotTransaction> transactions = new ArrayList<>();
        if (!getCursorItem().isEmpty() && !(slot instanceof OutputSlot)) {
            final LanternItemStack cursorItem = getCursorItem().copy();
            int quantity = cursorItem.getQuantity();
            final int maxQuantity = cursorItem.getMaxStackQuantity();
            if (quantity < maxQuantity) {
                final Predicate<ItemStack> hasMaxQuantity = stack -> stack.getQuantity() >= stack.getMaxStackQuantity();
                // Try first to get enough unfinished stacks
                QueryOperation<?> operation = QueryOperations.UNSAFE_ITEM_STACK_PREDICATE.of(hasMaxQuantity.negate());

                PeekedPollTransactionResult peekResult = this.container.query(operation).peekPoll(
                        maxQuantity - quantity, ItemPredicates.similarItemStack(cursorItem));

                quantity += peekResult.getPolledItem().getQuantity();
                transactions.addAll(peekResult.getTransactions());
                // Get the last items for the stack from a full stack
                if (quantity < maxQuantity) {
                    operation = QueryOperations.UNSAFE_ITEM_STACK_PREDICATE.of(hasMaxQuantity);

                    peekResult = this.container.query(operation).peekPoll(
                            maxQuantity - quantity, ItemPredicates.similarItemStack(cursorItem));
                    quantity += peekResult.getPolledItem().getQuantity();
                    transactions.addAll(peekResult.getTransactions());
                }

                cursorItem.setQuantity(quantity);
                newItem = cursorItem.createSnapshot();
            }
        }

        final CauseStack causeStack = CauseStack.current();

        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(oldItem, newItem);
        final ClickInventoryEvent.Double event = SpongeEventFactory.createClickInventoryEventDouble(
                causeStack.getCurrentCause(), cursorTransaction, this.container, this.container.transformSlots(transactions));
        finishInventoryEvent(event);
    }

    @Override
    public void handleClick(ClientContainer clientContainer, @Nullable ClientSlot clientSlot, MouseButton mouseButton) {
        final LanternPlayer player = clientContainer.getPlayer();
        if (player != this.container.getPlayerInventory().getCarrier().orElse(null) ||
                (clientSlot != null && !(clientSlot instanceof ClientSlot.Slot))) {
            return;
        }
        final CauseStack causeStack = CauseStack.current();
        if (clientSlot == null) {
            causeStack.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            final List<Entity> entities = new ArrayList<>();
            final Transaction<ItemStackSnapshot> cursorTransaction;

            // Clicking outside the container
            ItemStackSnapshot oldItem = ItemStackSnapshot.NONE;
            ItemStackSnapshot newItem = ItemStackSnapshot.NONE;
            if (!getCursorItem().isEmpty()) {
                oldItem = getCursorItem().createSnapshot();
                final ItemStackSnapshot droppedItem;
                if (mouseButton != MouseButton.LEFT) {
                    final ItemStack stack = getCursorItem().copy();
                    stack.setQuantity(stack.getQuantity() - 1);
                    newItem = LanternItemStack.toSnapshot(stack);
                    stack.setQuantity(1);
                    droppedItem = LanternItemStack.toSnapshot(stack);
                } else {
                    droppedItem = oldItem;
                }
                LanternEventHelper.handlePreDroppedItemSpawning(player.getTransform(), droppedItem).ifPresent(entities::add);
            }
            cursorTransaction = new Transaction<>(oldItem, newItem);
            final ClickInventoryEvent.Drop event;
            if (mouseButton == MouseButton.LEFT) {
                event = SpongeEventFactory.createClickInventoryEventDropOutsidePrimary(
                        causeStack.getCurrentCause(), cursorTransaction, entities, this.container, Collections.emptyList());
            } else {
                event = SpongeEventFactory.createClickInventoryEventDropOutsideSecondary(
                        causeStack.getCurrentCause(), cursorTransaction, entities, this.container, Collections.emptyList());
            }
            finishInventoryEvent(event);
            return;
        }
        // Clicking inside the container
        final AbstractSlot slot = ((ClientSlot.Slot) clientSlot).getSlot();
        if (mouseButton == MouseButton.MIDDLE) {
            final ItemStackSnapshot oldItem = LanternItemStack.toSnapshot(getCursorItem());
            Transaction<ItemStackSnapshot> cursorTransaction = null;

            final Optional<GameMode> gameMode = player.get(Keys.GAME_MODE);
            if (gameMode.isPresent() && gameMode.get().equals(GameModes.CREATIVE) && getCursorItem().isEmpty()) {
                final ItemStack stack = slot.peek();
                if (!stack.isEmpty()) {
                    stack.setQuantity(stack.getMaxStackQuantity());
                    cursorTransaction = new Transaction<>(oldItem, stack.createSnapshot());
                }
            }
            if (cursorTransaction == null) {
                cursorTransaction = new Transaction<>(oldItem, oldItem);
            }

            final ClickInventoryEvent.Middle event = SpongeEventFactory.createClickInventoryEventMiddle(
                    causeStack.getCurrentCause(), cursorTransaction, this.container, Collections.emptyList());
            finishInventoryEvent(event);
        } else {
            // Crafting slots have special click behavior
            if (slot instanceof CraftingOutput) {
                List<SlotTransaction> transactions = new ArrayList<>();
                Transaction<ItemStackSnapshot> cursorTransaction;

                final AbstractInventory parent = slot.parent();
                if (parent instanceof CraftingInventory) {
                    ClickInventoryEvent event;

                    final CraftingInventory inventory = (CraftingInventory) parent;
                    final Optional<ExtendedCraftingResult> optResult = Lantern.getRegistry().getCraftingRecipeRegistry()
                            .getExtendedResult(inventory.getCraftingGrid(), player.getWorld());
                    final ItemStackSnapshot originalCursorItem = LanternItemStack.toSnapshot(getCursorItem());
                    if (optResult.isPresent()) {
                        final CraftingResult result = optResult.get().getResult();
                        final ItemStackSnapshot resultItem = result.getResult();

                        int quantity = -1;
                        if (getCursorItem().isEmpty()) {
                            quantity = resultItem.getQuantity();
                        } else if (LanternItemStack.areSimilar(resultItem.createStack(), getCursorItem())) {
                            final int quantity1 = resultItem.getQuantity() + getCursorItem().getQuantity();
                            if (quantity1 < getCursorItem().getMaxStackQuantity()) {
                                quantity = quantity1;
                            }
                        }
                        if (quantity == -1) {
                            cursorTransaction = new Transaction<>(originalCursorItem, originalCursorItem);
                            transactions.add(new SlotTransaction(slot, resultItem, resultItem));
                        } else {
                            final LanternItemStack itemStack = (LanternItemStack) resultItem.createStack();
                            itemStack.setQuantity(quantity);
                            cursorTransaction = new Transaction<>(originalCursorItem, itemStack.createSnapshot());
                            transactions.add(new SlotTransaction(slot, resultItem, ItemStackSnapshot.NONE));
                            updateCraftingGrid(player, inventory, optResult.get().getMatrixResult(1), transactions);
                        }
                    } else {
                        cursorTransaction = new Transaction<>(originalCursorItem, originalCursorItem);
                        // No actual transaction, there shouldn't have been a item in the crafting result slot
                        transactions.add(new SlotTransaction(slot, ItemStackSnapshot.NONE, ItemStackSnapshot.NONE));
                    }

                    transactions = this.container.transformSlots(transactions);
                    if (mouseButton == MouseButton.LEFT) {
                        event = SpongeEventFactory.createClickInventoryEventPrimary(causeStack.getCurrentCause(), cursorTransaction,
                                this.container, transactions);
                    } else {
                        event = SpongeEventFactory.createClickInventoryEventSecondary(causeStack.getCurrentCause(), cursorTransaction,
                                this.container, transactions);
                    }
                    finishInventoryEvent(event);
                    return;
                } else {
                    Lantern.getLogger().warn("Found a CraftingOutput slot without a CraftingInventory as parent.");
                }
            }

            ClickInventoryEvent event;
            if (mouseButton == MouseButton.LEFT) {
                final List<SlotTransaction> transactions = new ArrayList<>();
                Transaction<ItemStackSnapshot> cursorTransaction = null;

                if (getCursorItem().isFilled() && !(slot instanceof OutputSlot)) {
                    final PeekedOfferTransactionResult result = slot.peekOffer(getCursorItem().copy());
                    if (!result.isEmpty()) {
                        transactions.addAll(result.getTransactions());
                        cursorTransaction = new Transaction<>(getCursorItem().createSnapshot(), result.getRejectedItem());
                    } else {
                        final LanternItemStack stack = getCursorItem();
                        final PeekedSetTransactionResult result1 = slot.peekSet(stack);
                        if (!result1.isEmpty()) {
                            cursorTransaction = new Transaction<>(getCursorItem().createSnapshot(), stack.createSnapshot());
                            transactions.addAll(result1.getTransactions());
                        }
                    }
                } else if (getCursorItem().isEmpty()) {
                    final PeekedPollTransactionResult result = slot.peekPoll(stack -> true);
                    cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, LanternItemStack.toSnapshot(result.getPolledItem()));
                    transactions.addAll(result.getTransactions());
                }
                if (cursorTransaction == null) {
                    final ItemStackSnapshot cursorItem = LanternItemStack.toSnapshot(getCursorItem());
                    cursorTransaction = new Transaction<>(cursorItem, cursorItem);
                }
                event = SpongeEventFactory.createClickInventoryEventPrimary(causeStack.getCurrentCause(),
                        cursorTransaction, this.container, this.container.transformSlots(transactions));
            } else {
                final List<SlotTransaction> transactions = new ArrayList<>();
                Transaction<ItemStackSnapshot> cursorTransaction = null;

                if (getCursorItem().isEmpty()) {
                    int stackSize = slot.getStackSize();
                    if (stackSize != 0) {
                        stackSize = stackSize - (stackSize / 2);
                        final PeekedPollTransactionResult result = slot.peekPoll(stackSize, stack -> true);
                        transactions.addAll(result.getTransactions());
                        cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, result.getPolledItem().createSnapshot());
                    }
                } else {
                    final ItemStack itemStack = getCursorItem().copy();
                    itemStack.setQuantity(1);

                    final PeekedOfferTransactionResult result = slot.peekOffer(itemStack);
                    if (!result.isEmpty()) {
                        final ItemStackSnapshot oldCursor = getCursorItem().createSnapshot();
                        int quantity = getCursorItem().getQuantity() - 1;
                        if (quantity <= 0) {
                            cursorTransaction = new Transaction<>(oldCursor, ItemStackSnapshot.NONE);
                        } else {
                            final LanternItemStack newCursorItem = getCursorItem().copy();
                            newCursorItem.setQuantity(quantity);
                            cursorTransaction = new Transaction<>(oldCursor, newCursorItem.createSnapshot());
                        }
                        transactions.addAll(result.getTransactions());
                    } else {
                        final PeekedSetTransactionResult result1 = slot.peekSet(getCursorItem());
                        if (!result1.isEmpty()) {
                            final LanternItemStack replacedItem = slot.peek();
                            if (replacedItem.isFilled()) {
                                setCursorItem(replacedItem);
                            }
                            cursorTransaction = new Transaction<>(getCursorItem().createSnapshot(), replacedItem.toSnapshot());
                            transactions.addAll(result1.getTransactions());
                        }
                    }
                }
                if (cursorTransaction == null) {
                    final ItemStackSnapshot cursorItem = LanternItemStack.toSnapshot(getCursorItem());
                    cursorTransaction = new Transaction<>(cursorItem, cursorItem);
                }
                event = SpongeEventFactory.createClickInventoryEventSecondary(
                        causeStack.getCurrentCause(), cursorTransaction, this.container,
                        this.container.transformSlots(transactions));
            }
            finishInventoryEvent(event);
        }
    }

    @Override
    public void handleDropKey(ClientContainer clientContainer, ClientSlot clientSlot, boolean ctrl) {
        final LanternPlayer player = clientContainer.getPlayer();
        if (player != this.container.getPlayerInventory().getCarrier().orElse(null) ||
                !(clientSlot instanceof ClientSlot.Slot)) {
            return;
        }
        final AbstractSlot slot = ((ClientSlot.Slot) clientSlot).getSlot();

        final CauseStack causeStack = CauseStack.current();
        causeStack.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

        final List<Entity> entities = new ArrayList<>();
        final Transaction<ItemStackSnapshot> cursorTransaction;
        List<SlotTransaction> slotTransactions = new ArrayList<>();

        final ItemStackSnapshot item = LanternItemStack.toSnapshot(getCursorItem());
        cursorTransaction = new Transaction<>(item, item);
        final PeekedPollTransactionResult result = ctrl ? slot.peekPoll(itemStack -> true) :
                slot.peekPoll(1, itemStack -> true);
        if (!result.isEmpty()) {
            final List<SlotTransaction> transactions = result.getTransactions();
            slotTransactions.addAll(transactions);
            final ItemStack itemStack = transactions.get(0).getOriginal().createStack();
            itemStack.setQuantity(itemStack.getQuantity() - transactions.get(0).getFinal().getQuantity());
            LanternEventHelper.handlePreDroppedItemSpawning(
                    player.getTransform(), LanternItemStackSnapshot.wrap(itemStack)).ifPresent(entities::add);
        }
        slotTransactions = this.container.transformSlots(slotTransactions);
        final ClickInventoryEvent.Drop event;
        if (ctrl) {
            event = SpongeEventFactory.createClickInventoryEventDropFull(causeStack.getCurrentCause(), cursorTransaction, entities,
                    this.container, slotTransactions);
        } else {
            event = SpongeEventFactory.createClickInventoryEventDropSingle(causeStack.getCurrentCause(), cursorTransaction, entities,
                    this.container, slotTransactions);
        }
        finishInventoryEvent(event);
    }

    @Override
    public void handleNumberKey(ClientContainer clientContainer, ClientSlot clientSlot, int number) {
        if (clientContainer.getPlayer() != this.container.getPlayerInventory().getCarrier().orElse(null) ||
                !(clientSlot instanceof ClientSlot.Slot)) {
            return;
        }
        final ClientSlot hotbarSlot = clientContainer.getClientSlot(clientContainer.getHotbarSlotIndex(number - 1))
                .orElseThrow(() -> new IllegalStateException("Missing hotbar client slot: " + number));
        if (!(hotbarSlot instanceof ClientSlot.Slot)) {
            return;
        }
        final AbstractSlot slot1 = ((ClientSlot.Slot) clientSlot).getSlot();
        final AbstractSlot hotbarSlot1 = ((ClientSlot.Slot) hotbarSlot).getSlot();
        if (slot1 != hotbarSlot1) {
            final List<SlotTransaction> transactions = new ArrayList<>();
            final Transaction<ItemStackSnapshot> cursorTransaction;

            if (getCursorItem().isEmpty()) {
                cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);

                final ItemStack itemStack = slot1.getRawItemStack();
                final ItemStack hotbarItemStack = hotbarSlot1.getRawItemStack();

                final ItemStackSnapshot itemStackSnapshot = LanternItemStack.toSnapshot(itemStack);
                final ItemStackSnapshot hotbarItemStackSnapshot = LanternItemStack.toSnapshot(hotbarItemStack);

                if (!(itemStackSnapshot != ItemStackSnapshot.NONE && (!hotbarSlot1.isValidItem(itemStack) ||
                        itemStackSnapshot.getQuantity() > hotbarSlot1.getMaxStackSize())) &&
                        !(hotbarItemStackSnapshot != ItemStackSnapshot.NONE && (!slot1.isValidItem(hotbarItemStack) ||
                                hotbarItemStack.getQuantity() > slot1.getMaxStackSize()))) {
                    transactions.add(new SlotTransaction(slot1, itemStackSnapshot, hotbarItemStackSnapshot));
                    transactions.add(new SlotTransaction(hotbarSlot1, hotbarItemStackSnapshot, itemStackSnapshot));
                }
            } else {
                final ItemStackSnapshot cursorItem = getCursorItem().createSnapshot();
                cursorTransaction = new Transaction<>(cursorItem, cursorItem);
            }

            final CauseStack causeStack = CauseStack.current();
            final ClickInventoryEvent.NumberPress event = SpongeEventFactory.createClickInventoryEventNumberPress(
                    causeStack.getCurrentCause(), cursorTransaction, this.container,
                    this.container.transformSlots(transactions), number - 1);
            finishInventoryEvent(event);
        }
    }

    @Override
    public void handleDrag(ClientContainer clientContainer, List<ClientSlot> clientSlots, MouseButton mouseButton) {
        final LanternPlayer player = clientContainer.getPlayer();
        if (player != this.container.getPlayerInventory().getCarrier().orElse(null)) {
            return;
        }
        final List<AbstractSlot> slots = clientSlots.stream()
                .filter(clientSlot -> clientSlot instanceof ClientSlot.Slot)
                .map(clientSlot -> ((ClientSlot.Slot) clientSlot).getSlot())
                .collect(Collectors.toList());
        if (slots.size() != clientSlots.size()) {
            // TODO: Is this the behavior we want?
            return;
        }
        final ItemStack cursorItem = getCursorItem();
        if (cursorItem.isEmpty()) {
            return;
        }

        final CauseStack causeStack = CauseStack.current();
        if (mouseButton == MouseButton.LEFT) {
            final int quantity = cursorItem.getQuantity();
            final int slotCount = slots.size();
            final int itemsPerSlot = quantity / slotCount;
            int rest = quantity - itemsPerSlot * slotCount;

            final List<SlotTransaction> transactions = new ArrayList<>();
            for (AbstractSlot slot : slots) {
                final ItemStack itemStack = cursorItem.copy();
                itemStack.setQuantity(itemsPerSlot);
                transactions.addAll(slot.peekOffer(itemStack).getTransactions());
                // In case not all the items are consumed, add them to the rest item
                rest += itemStack.getQuantity();
            }

            ItemStackSnapshot newCursorItem = ItemStackSnapshot.NONE;
            if (rest > 0) {
                final ItemStack itemStack = cursorItem.copy();
                itemStack.setQuantity(rest);
                newCursorItem = LanternItemStackSnapshot.wrap(itemStack);
            }
            final ItemStackSnapshot oldCursorItem = cursorItem.createSnapshot();
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(oldCursorItem, newCursorItem);

            final ClickInventoryEvent.Drag.Primary event = SpongeEventFactory.createClickInventoryEventDragPrimary(
                    causeStack.getCurrentCause(), cursorTransaction, this.container, this.container.transformSlots(transactions));
            finishInventoryEvent(event);
        } else if (mouseButton == MouseButton.RIGHT) {
            int quantity = cursorItem.getQuantity();
            final int size = Math.min(slots.size(), quantity);

            final List<SlotTransaction> transactions = new ArrayList<>();
            for (AbstractSlot slot : slots) {
                final ItemStack itemStack = cursorItem.copy();
                itemStack.setQuantity(1);
                transactions.addAll(slot.peekOffer(itemStack).getTransactions());
                // In case the item isn't consumed, add it back to the cursor
                quantity += itemStack.getQuantity();
            }
            quantity -= size;

            ItemStackSnapshot newCursorItem = ItemStackSnapshot.NONE;
            if (quantity > 0) {
                final ItemStack itemStack = cursorItem.copy();
                itemStack.setQuantity(quantity);
                newCursorItem = LanternItemStackSnapshot.wrap(itemStack);
            }
            final ItemStackSnapshot oldCursorItem = getCursorItem().createSnapshot();
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(oldCursorItem, newCursorItem);

            final ClickInventoryEvent.Drag.Secondary event = SpongeEventFactory.createClickInventoryEventDragSecondary(
                    causeStack.getCurrentCause(), cursorTransaction, this.container, this.container.transformSlots(transactions));
            finishInventoryEvent(event);
        } else {
            // TODO: Middle mouse drag mode
        }
    }

    @Override
    public void handleCreativeClick(ClientContainer clientContainer, @Nullable ClientSlot clientSlot, ItemStack itemStack) {
        final LanternPlayer player = clientContainer.getPlayer();

        final CauseStack causeStack = CauseStack.current();
        if (clientSlot == null) {
            if (!itemStack.isEmpty()) {
                causeStack.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                LanternEventHelper.handleDroppedItemSpawning(player.getTransform(), itemStack.createSnapshot());
            }
        } else if (clientSlot instanceof ClientSlot.Slot) {
            final AbstractSlot slot = ((ClientSlot.Slot) clientSlot).getSlot();
            final PeekedSetTransactionResult result = slot.peekSet(itemStack);

            // We do not know the remaining stack in the cursor,
            // so just use none as new item
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(
                    LanternItemStack.toSnapshot(itemStack), ItemStackSnapshot.NONE);

            final ClickInventoryEvent.Creative event = SpongeEventFactory.createClickInventoryEventCreative(
                    causeStack.getCurrentCause(), cursorTransaction, this.container,
                    this.container.transformSlots(result.getTransactions()));
            finishInventoryEvent(event);
        }
    }

    @Override
    public void handlePick(ClientContainer clientContainer, @Nullable ClientSlot clientSlot) {
        final LanternPlayer player = clientContainer.getPlayer();
        if (player != this.container.getPlayerInventory().getCarrier().orElse(null) ||
                !(clientSlot instanceof ClientSlot.Slot)) {
            return;
        }
        final PlayerInventoryContainer inventoryContainer = player.getInventoryContainer();
        final ClientSlot hotbarClientSlot = inventoryContainer.getClientContainer().getSelectedHotbarSlot();
        if (!(hotbarClientSlot instanceof ClientSlot.Slot)) {
            return;
        }
        final LanternHotbarInventory hotbar = player.getInventory().getHotbar();
        final AbstractSlot slot = ((ClientSlot.Slot) clientSlot).getSlot();

        // The slot we will swap items with
        AbstractSlot hotbarSlot = hotbar.getSelectedSlot();
        if (hotbarSlot.peek().isFilled()) {
            final Optional<Slot> optSlot = hotbar.slots().stream()
                    .filter(slot1 -> slot1.peek().isEmpty())
                    .findFirst();
            if (optSlot.isPresent()) {
                hotbarSlot = (AbstractSlot) optSlot.get();
            }
        }

        final ItemStack slotItem = slot.peek();
        final ItemStack hotbarItem = hotbarSlot.peek();

        hotbarSlot.set(slotItem);
        hotbar.setSelectedSlotIndex(hotbar.getSlotIndex(hotbarSlot));
        slot.set(hotbarItem);
    }

    private void updateCraftingGrid(Player player, CraftingInventory craftingInventory,
            MatrixResult matrixResult, List<SlotTransaction> transactions) {
        final CraftingMatrix matrix = matrixResult.getCraftingMatrix();
        final CraftingGridInventory grid = craftingInventory.getCraftingGrid();
        for (int x = 0; x < matrix.width(); x++) {
            for (int y = 0; y < matrix.height(); y++) {
                final ItemStack itemStack = matrix.get(x, y);
                final Slot slot = grid.getSlot(x, y).get();
                transactions.add(new SlotTransaction(slot, LanternItemStackSnapshot.wrap(slot.peek()), LanternItemStackSnapshot.wrap(itemStack)));
            }
        }

        final CauseStack causeStack = CauseStack.current();
        causeStack.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

        final Transform<World> transform = player.getTransform();
        final List<Entity> entities = LanternEventHelper.handlePreDroppedItemSpawning(matrixResult.getRest().stream()
                .map(itemStack -> new Tuple<ItemStackSnapshot, Transform<World>>(
                        LanternItemStackSnapshot.wrap(itemStack), transform))
                .collect(Collectors.toList()));
        final SpawnEntityEvent event = SpongeEventFactory.createDropItemEventDispense(causeStack.getCurrentCause(), entities);
        Sponge.getEventManager().post(event);

        // Spawn all the entities in the world if the event isn't cancelled
        LanternWorld.finishSpawnEntityEvent(event);
    }

    private void finishInventoryEvent(ChangeInventoryEvent event) {
        final List<SlotTransaction> slotTransactions = event.getTransactions();
        Sponge.getEventManager().post(event);
        if (!event.isCancelled()) {
            if (!(event instanceof ClickInventoryEvent.Creative) && event instanceof ClickInventoryEvent) {
                final Transaction<ItemStackSnapshot> cursorTransaction = ((ClickInventoryEvent) event).getCursorTransaction();
                if (cursorTransaction.isValid() && cursorTransaction.getOriginal() != cursorTransaction.getFinal()) {
                    setCursorItem(cursorTransaction.getFinal().createStack());
                }
            }
            slotTransactions.stream()
                    .filter(transaction -> transaction.isValid() && transaction.getOriginal() != transaction.getFinal())
                    .forEach(transaction -> ((ISlot) transaction.getSlot()).setForced(transaction.getFinal().createStack()));
            if (event instanceof SpawnEntityEvent) {
                LanternWorld.finishSpawnEntityEvent((SpawnEntityEvent) event);
            }
        }
    }
}
