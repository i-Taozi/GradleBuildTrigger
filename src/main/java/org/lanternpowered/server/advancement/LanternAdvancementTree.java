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
package org.lanternpowered.server.advancement;

import org.lanternpowered.server.catalog.DefaultCatalogType;
import org.lanternpowered.server.util.ToStringHelper;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.text.translation.Translation;

public class LanternAdvancementTree extends DefaultCatalogType implements AdvancementTree {

    private final Advancement rootAdvancement;
    private final String background;

    LanternAdvancementTree(CatalogKey key, Translation name, Advancement rootAdvancement, String background) {
        super(key, name);
        this.rootAdvancement = rootAdvancement;
        this.background = background;
        applyTree(rootAdvancement, this);
    }

    private static void applyTree(Advancement advancement, AdvancementTree tree) {
        ((LanternAdvancement) advancement).setTree(tree);
        for (Advancement child : advancement.getChildren()) {
            applyTree(child, tree);
        }
    }

    @Override
    public Advancement getRootAdvancement() {
        return this.rootAdvancement;
    }

    @Override
    public String getBackgroundPath() {
        return this.background;
    }

    @Override
    public ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("rootAdvancement", this.rootAdvancement.getKey())
                .add("background", this.background);
    }
}
