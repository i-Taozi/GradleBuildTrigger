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
package org.lanternpowered.server.network.vanilla.message.handler.play;

import org.lanternpowered.api.cause.CauseStack;
import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.inventory.AbstractSlot;
import org.lanternpowered.server.inventory.LanternItemStackSnapshot;
import org.lanternpowered.server.inventory.PlayerInventoryContainer;
import org.lanternpowered.server.inventory.vanilla.LanternPlayerInventory;
import org.lanternpowered.server.network.NetworkContext;
import org.lanternpowered.server.network.message.handler.Handler;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInSwapHandItems;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public final class HandlerPlayInSwapHandItems implements Handler<MessagePlayInSwapHandItems> {

    @Override
    public void handle(NetworkContext context, MessagePlayInSwapHandItems message) {
        final LanternPlayer player = context.getSession().getPlayer();
        final LanternPlayerInventory inventory = player.getInventory();

        final AbstractSlot hotbarSlot = inventory.getHotbar().getSelectedSlot();
        final AbstractSlot offHandSlot = inventory.getOffhand();

        final ItemStackSnapshot hotbarItem = LanternItemStackSnapshot.wrap(hotbarSlot.peek());
        final ItemStackSnapshot offHandItem = LanternItemStackSnapshot.wrap(offHandSlot.peek());

        final List<SlotTransaction> transactions = new ArrayList<>();
        transactions.add(new SlotTransaction(hotbarSlot, hotbarItem, offHandItem));
        transactions.add(new SlotTransaction(offHandSlot, offHandItem, hotbarItem));

        try (CauseStack.Frame frame = CauseStack.current().pushCauseFrame()) {
            frame.addContext(EventContextKeys.PLAYER, player);
            frame.pushCause(player);

            final ChangeInventoryEvent.SwapHand event = SpongeEventFactory.createChangeInventoryEventSwapHand(
                    frame.getCurrentCause(), inventory, transactions);
            Sponge.getEventManager().post(event);
            if (!event.isCancelled()) {
                transactions.stream().filter(Transaction::isValid).forEach(
                        transaction -> transaction.getSlot().set(transaction.getFinal().createStack()));

                final PlayerInventoryContainer inventoryContainer = context.getSession().getPlayer().getInventoryContainer();
                inventoryContainer.getClientContainer().queueSilentSlotChange(hotbarSlot);
            }
        }
    }
}
