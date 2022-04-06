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
@file:JvmName("EnchantmentTypeFactory")
@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package org.lanternpowered.api.item.enchantment

import org.lanternpowered.api.ext.*
import org.lanternpowered.server.text.translation.TranslationHelper.tr
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias EnchantmentType = org.spongepowered.api.item.enchantment.EnchantmentType
typealias EnchantmentTypes = org.spongepowered.api.item.enchantment.EnchantmentTypes

/**
 * Constructs a new [EnchantmentType] with the given id and the builder function.
 *
 * @param id The id of the enchantment type
 * @param fn The builder function to apply
 */
@JvmName("of")
inline fun EnchantmentType(id: String, fn: EnchantmentTypeBuilder.() -> Unit): EnchantmentType {
    contract {
        callsInPlace(fn, InvocationKind.EXACTLY_ONCE)
    }
    return EnchantmentTypeBuilder().id(id).name(tr("enchantment.$id")).apply(fn).build()
}

/**
 * Constructs a new [EnchantmentTypeBuilder].
 *
 * @return The constructed enchantment type builder
 */
@JvmName("builder")
inline fun EnchantmentTypeBuilder(): EnchantmentTypeBuilder = builderOf()
