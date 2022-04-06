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
package org.lanternpowered.server.catalog

import org.lanternpowered.api.catalog.CatalogKey
import org.lanternpowered.api.catalog.CatalogKeys
import org.lanternpowered.api.ext.*
import org.lanternpowered.api.text.translation.Translatable
import org.lanternpowered.api.text.translation.Translation

open class DefaultCatalogType @JvmOverloads constructor(key: CatalogKey, name: Translation? = null) : AbstractCatalogType() {

    private val key: CatalogKey

    private val theName: String by lazy {
        name?.get() ?: (key as? NamedCatalogKey)?.name ?: (this as? Translatable)?.translation?.get() ?: key.value
    }

    init {
        check(key.namespace.isNotEmpty()) { "plugin id (key namespace) cannot be empty" }
        check(key.value.isNotEmpty()) { "id (key value) cannot be empty" }
        check(key.name.isNotEmpty()) { "name cannot be empty" }
        this.key = key
    }

    override fun getKey() = this.key
    override fun getName() = this.theName

    companion object {

        fun minecraft(id: String, name: String = id): DefaultCatalogType =
                DefaultCatalogType(CatalogKeys.minecraft(id, name))

        fun sponge(id: String, name: String = id): DefaultCatalogType =
                DefaultCatalogType(CatalogKeys.sponge(id, name))

        fun lantern(id: String, name: String = id): DefaultCatalogType =
                DefaultCatalogType(CatalogKeys.lantern(id, name))
    }
}
