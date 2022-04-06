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
package org.lanternpowered.server.network.vanilla.message.type.play;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.lanternpowered.server.network.message.Message;

import java.util.Arrays;

public abstract class MessagePlayOutUnlockRecipes implements Message {

    private final boolean openRecipeBook;
    private final boolean craftingFilter;
    private final IntList recipeIds;

    private MessagePlayOutUnlockRecipes(boolean openRecipeBook, boolean craftingFilter, IntList recipeIds) {
        this.recipeIds = IntLists.unmodifiable(recipeIds);
        this.openRecipeBook = openRecipeBook;
        this.craftingFilter = craftingFilter;
    }

    public boolean hasOpenCraftingBook() {
        return this.openRecipeBook;
    }

    public boolean hasCraftingFilter() {
        return this.craftingFilter;
    }

    public MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(getClass().getSuperclass().getSimpleName() + "." + getClass().getSimpleName())
                .add("openRecipeBook", this.openRecipeBook)
                .add("craftingFilter", this.craftingFilter)
                .add("recipeIds", Arrays.toString(this.recipeIds.toIntArray()));
    }

    public IntList getRecipeIds() {
        return this.recipeIds;
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    public final static class Remove extends MessagePlayOutUnlockRecipes {

        public Remove(boolean openRecipeBook, boolean craftingFilter, IntList recipeIds) {
            super(openRecipeBook, craftingFilter, recipeIds);
        }
    }

    public final static class Init extends MessagePlayOutUnlockRecipes {

        private final IntList recipeIdsToBeDisplayed;

        public Init(boolean openRecipeBook, boolean craftingFilter, IntList recipeIds, IntList recipeIdsToBeDisplayed) {
            super(openRecipeBook, craftingFilter, recipeIds);
            this.recipeIdsToBeDisplayed = IntLists.unmodifiable(recipeIdsToBeDisplayed);
        }

        public IntList getRecipeIdsToBeDisplayed() {
            return this.recipeIdsToBeDisplayed;
        }

        @Override
        public MoreObjects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("recipeIdsToBeDisplayed", Arrays.toString(this.recipeIdsToBeDisplayed.toIntArray()));
        }
    }

    public final static class Add extends MessagePlayOutUnlockRecipes {

        public Add(boolean openRecipeBook, boolean craftingFilter, IntList recipeIds) {
            super(openRecipeBook, craftingFilter, recipeIds);
        }
    }
}
