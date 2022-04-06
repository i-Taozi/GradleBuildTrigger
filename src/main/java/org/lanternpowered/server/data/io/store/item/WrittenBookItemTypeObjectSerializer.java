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
package org.lanternpowered.server.data.io.store.item;

import org.lanternpowered.server.data.io.store.SimpleValueContainer;
import org.lanternpowered.server.text.LanternTexts;
import org.lanternpowered.server.text.gson.JsonTextSerializer;
import org.lanternpowered.server.text.translation.TranslationContext;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Locale;
import java.util.stream.Collectors;

public class WrittenBookItemTypeObjectSerializer extends WritableBookItemTypeObjectSerializer {

    public static final DataQuery AUTHOR = DataQuery.of("author");
    public static final DataQuery TITLE = DataQuery.of("title");
    private static final DataQuery GENERATION = DataQuery.of("generation");

    @Override
    public void serializeValues(ItemStack itemStack, SimpleValueContainer valueContainer, DataView dataView) {
        super.serializeValues(itemStack, valueContainer, dataView);
        valueContainer.remove(Keys.BOOK_PAGES).ifPresent(lines ->
                dataView.set(PAGES, lines.stream().map(TextSerializers.JSON::serialize).collect(Collectors.toList())));
        valueContainer.remove(Keys.BOOK_AUTHOR).ifPresent(text ->
                dataView.set(AUTHOR, LanternTexts.toLegacy(text)));
        valueContainer.remove(Keys.DISPLAY_NAME).ifPresent(text ->
                dataView.set(TITLE, LanternTexts.toLegacy(text)));
        valueContainer.remove(Keys.GENERATION).ifPresent(value ->
                dataView.set(GENERATION, value));
    }

    @Override
    public void deserializeValues(ItemStack itemStack, SimpleValueContainer valueContainer, DataView dataView) {
        super.deserializeValues(itemStack, valueContainer, dataView);
        dataView.getStringList(PAGES).ifPresent(lines -> valueContainer.set(Keys.BOOK_PAGES,
                lines.stream().map(TextSerializers.JSON::deserializeUnchecked).collect(Collectors.toList())));
        dataView.getString(AUTHOR).ifPresent(author -> valueContainer.set(Keys.BOOK_AUTHOR, LanternTexts.fromLegacy(author)));
        dataView.getString(TITLE).ifPresent(title -> valueContainer.set(Keys.DISPLAY_NAME, LanternTexts.fromLegacy(title)));
        dataView.getInt(GENERATION).ifPresent(value -> valueContainer.set(Keys.GENERATION, value));
    }

    public static void writeBookData(DataView dataView, BookView bookView, Locale locale) {
        try (TranslationContext ignored = TranslationContext.enter()
                .locale(locale)
                .enableForcedTranslations()) {
            dataView.set(AUTHOR, LanternTexts.toLegacy(bookView.getAuthor()));
            dataView.set(TITLE, LanternTexts.toLegacy(bookView.getTitle()));
            dataView.set(PAGES, bookView.getPages().stream().map(JsonTextSerializer.getGson()::toJson).collect(Collectors.toList()));
        }
    }
}
