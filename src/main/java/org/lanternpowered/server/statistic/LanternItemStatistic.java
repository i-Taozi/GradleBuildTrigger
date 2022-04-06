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
package org.lanternpowered.server.statistic;

import org.lanternpowered.server.util.ToStringHelper;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.text.translation.Translation;

import java.text.NumberFormat;

import javax.annotation.Nullable;

public class LanternItemStatistic extends LanternStatistic implements ItemStatistic {

    private final ItemType itemType;

    public LanternItemStatistic(CatalogKey key, Translation translation, String internalId, NumberFormat format,
            @Nullable Criterion criterion, StatisticType type, ItemType itemType) {
        super(key, translation, internalId, format, criterion, type);
        this.itemType = itemType;
    }

    @Override
    public ItemType getItemType() {
        return this.itemType;
    }

    @Override
    public ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("itemType", this.itemType.getKey());
    }
}
