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
package org.lanternpowered.server.inventory.view;

import static com.google.common.base.Preconditions.checkNotNull;

import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.inventory.AbstractChildrenInventory;
import org.lanternpowered.server.inventory.AbstractSlot;
import org.lanternpowered.server.inventory.PlayerTopBottomContainer;
import org.lanternpowered.server.inventory.behavior.VanillaContainerInteractionBehavior;
import org.lanternpowered.server.inventory.client.ChestClientContainer;
import org.lanternpowered.server.inventory.client.ClientContainer;
import org.lanternpowered.server.inventory.client.TopContainerPart;
import org.lanternpowered.server.inventory.vanilla.LanternPlayerInventory;
import org.lanternpowered.server.inventory.vanilla.VanillaInventoryConstants;
import org.lanternpowered.server.text.translation.TextTranslation;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;

/**
 * A fancy view container that allows specific chest slots to be assigned to an actual
 * slot. The slots that aren't assigned won't be usable and will replaced by a dummy
 * item. By default white glass pane because it fits nicely with the inventory layout.
 */
public abstract class FancyTopBottomGridViewContainer extends PlayerTopBottomContainer {

    public static abstract class Layout {

        final int rows;
        final AbstractSlot[][] slots;

        Layout(int columns, int rows) {
            this.rows = rows;
            this.slots = new AbstractSlot[columns][rows];
        }

        /**
         * Binds the {@link Slot} at the given x and y coordinate.
         *
         * @param x The x coordinate
         * @param y The y coordinate
         * @param slot The slot to bind
         */
        public void bind(int x, int y, Slot slot) {
            checkNotNull(slot, "slot");
            this.slots[x][y] = (AbstractSlot) slot;
        }

        /**
         * Represents a chest inventory layout with a 9 x rows grid.
         */
        public static final class Chest extends Layout {

            /**
             * Constructs a chest layout with the given amount of rows.
             *
             * @param rows The rows
             */
            public Chest(int rows) {
                super(VanillaInventoryConstants.CHEST_COLUMNS, rows);
            }
        }

        /**
         * Represents a hopper inventory layout with a 5 x 1 grid.
         */
        public static final class Hopper extends Layout {

            public Hopper() {
                super(VanillaInventoryConstants.HOPPER_SIZE, 1);
            }
        }

        /**
         * Represents a dispenser inventory layout with a 3 x 3 grid.
         */
        public static final class Dispenser extends Layout {

            public Dispenser() {
                super(3, 3);
            }
        }
    }

    public FancyTopBottomGridViewContainer(LanternPlayerInventory playerInventory, AbstractChildrenInventory openInventory) {
        super(playerInventory, openInventory);
    }

    /**
     * Constructs a {@link Layout} that will be displayed.
     *
     * @return The layout
     */
    protected abstract Layout buildLayout();

    @Override
    protected ClientContainer constructClientContainer() {
        final Layout layout = buildLayout();

        final ClientContainer clientContainer = new ChestClientContainer(layout.rows);
        clientContainer.bindCursor(getCursorSlot());
        clientContainer.bindInteractionBehavior(new VanillaContainerInteractionBehavior(this));
        clientContainer.setTitle(TextTranslation.toText(getOpenInventory().getName()));

        final ItemStack dummy = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        dummy.offer(Keys.DISPLAY_NAME, Text.of());

        final TopContainerPart topContainerPart = clientContainer.getTop();
        for (int x = 0; x < layout.slots.length; x++) {
            for (int y = 0; y < layout.rows; y++) {
                final AbstractSlot slot = layout.slots[x][y];
                final int index = x + y * layout.slots.length;
                if (slot != null) {
                    topContainerPart.bindSlot(index, slot);
                } else {
                    topContainerPart.bindButton(index).setItem(dummy);
                }
            }
        }

        // Bind the default bottom container part if the custom one is missing
        if (!clientContainer.getBottom().isPresent()) {
            final LanternPlayer player = (LanternPlayer) getPlayerInventory().getCarrier().get();
            clientContainer.bindBottom(player.getInventoryContainer().getClientContainer().getBottom().get());
        }

        return clientContainer;
    }

}
