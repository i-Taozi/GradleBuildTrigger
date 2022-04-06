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
package org.lanternpowered.server.effect.potion

import org.lanternpowered.api.catalog.CatalogKey
import org.lanternpowered.api.effect.potion.PotionEffect
import org.lanternpowered.api.effect.potion.PotionEffectType
import org.lanternpowered.api.text.translation.Translatable
import org.lanternpowered.api.text.translation.Translation
import org.lanternpowered.server.catalog.DefaultCatalogType
import org.lanternpowered.server.catalog.InternalCatalogType
import org.lanternpowered.server.text.translation.Translated
import org.lanternpowered.server.text.translation.TranslationHelper.tr
import org.spongepowered.api.entity.Entity

class LanternPotionEffectType @JvmOverloads constructor(
        key: CatalogKey, override val internalId: Int, translation: Translation,
        private val potionTranslation: Translation,
        val effectConsumer: (Entity, PotionEffect) -> Unit = { _,_ -> },
        private var instant: Boolean = false
) : DefaultCatalogType(key), PotionEffectType, InternalCatalogType, Translatable by Translated(translation) {

    @JvmOverloads constructor(key: CatalogKey, internalId: Int, translationPart: String,
                              effectConsumer: (Entity, PotionEffect) -> Unit = { _,_ -> }, instant: Boolean = false) :
            this(key, internalId,
                    tr("effect.%s", translationPart),
                    tr("potion.effect.%s", translationPart),
                    effectConsumer, instant)

    fun instant(): LanternPotionEffectType = apply { this.instant = true }

    override fun isInstant(): Boolean = this.instant
    override fun getPotionTranslation(): Translation = this.potionTranslation
}
