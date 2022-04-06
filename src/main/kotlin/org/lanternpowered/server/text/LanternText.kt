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
package org.lanternpowered.server.text

import com.google.common.base.Objects
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterators
import org.lanternpowered.api.ext.*
import org.lanternpowered.api.text.Text
import org.lanternpowered.api.text.TextBuilder
import org.lanternpowered.api.text.format.TextColor
import org.lanternpowered.api.text.format.TextFormat
import org.lanternpowered.api.text.format.TextStyle
import org.lanternpowered.api.text.serializer.TextSerializers
import org.lanternpowered.api.x.text.format.XTextStyle
import org.lanternpowered.server.util.ToStringHelper
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.Queries
import org.spongepowered.api.text.action.ClickAction
import org.spongepowered.api.text.action.HoverAction
import org.spongepowered.api.text.action.ShiftClickAction
import java.util.ArrayList
import java.util.Collections
import java.util.Optional

abstract class LanternText(
        private val format: TextFormat,
        private val children: ImmutableList<Text>,
        private val clickAction: ClickAction<*>?,
        private val hoverAction: HoverAction<*>?,
        private val shiftClickAction: ShiftClickAction<*>?
) : Text {

    internal constructor(): this(TextFormat.of(), ImmutableList.of(), null, null, null)

    override fun getFormat(): TextFormat = this.format
    override fun getColor(): TextColor = this.format.color
    override fun getStyle(): TextStyle = this.format.style
    override fun getChildren(): ImmutableList<Text> = this.children
    override fun getClickAction(): Optional<ClickAction<*>> = this.clickAction.optional()
    override fun getHoverAction(): Optional<HoverAction<*>> = this.hoverAction.optional()
    override fun getShiftClickAction(): Optional<ShiftClickAction<*>> = this.shiftClickAction.optional()
    override fun isEmpty(): Boolean = this === LanternLiteralText.EMPTY
    override fun toPlain(): String = TextSerializers.PLAIN.serialize(this)
    override fun toPlainSingle(): String = TextSerializers.PLAIN.serializeSingle(this)
    override fun concat(other: Text): Text = toBuilder().append(other).build()
    override fun trim(): Text = toBuilder().trim().build()
    override fun getContentVersion(): Int = 1

    override fun withChildren() = Iterable {
        if (this.children.isEmpty()) Iterators.singletonIterator(this) else TextIterator(this)
    }

    override fun toContainer(): DataContainer {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion)
                .set(Queries.JSON, TextSerializers.JSON.serialize(this))
    }

    override fun compareTo(other: Text): Int = toPlain().compareTo(other.toPlain())

    override fun equals(other: Any?): Boolean {
        return other === this || (other is LanternText &&
                this.format == other.format &&
                this.children == other.children &&
                this.clickAction == other.clickAction &&
                this.hoverAction == other.hoverAction &&
                this.shiftClickAction == other.shiftClickAction)
    }

    override fun hashCode() = Objects.hashCode(this.format, this.children, this.clickAction, this.hoverAction, this.shiftClickAction)
    override fun toString() = toStringHelper().toString()
    override fun toText() = this

    abstract override fun toBuilder(): TextBuilder

    internal open fun toStringHelper(): ToStringHelper {
        return ToStringHelper(this)
                .omitNullValues()
                .add("format", if (this.format.isEmpty) null else this.format)
                .add("children", if (this.children.isEmpty()) null else this.children)
                .add("clickAction", this.clickAction)
                .add("hoverAction", this.hoverAction)
                .add("shiftClickAction", this.shiftClickAction)
    }

    abstract class AbstractBuilder<B : AbstractBuilder<B>> : TextBuilder {

        internal var format = TextFormat.of()
        internal var children: MutableList<Text> = ArrayList()
        internal var clickAction: ClickAction<*>? = null
        internal var hoverAction: HoverAction<*>? = null
        internal var shiftClickAction: ShiftClickAction<*>? = null

        /**
         * Constructs a new empty [TextBuilder].
         */
        internal constructor()

        /**
         * Constructs a new [TextBuilder] with the properties of the given
         * [Text] as initial values.
         *
         * @param text The text to copy the values from
         */
        internal constructor(text: Text) {
            text as LanternText
            this.format = text.format
            this.children = ArrayList(text.children)
            this.clickAction = text.clickAction
            this.hoverAction = text.hoverAction
            this.shiftClickAction = text.shiftClickAction
        }

        internal inline fun apply(fn: B.() -> Unit): B {
            val cast: B = uncheckedCast()
            fn(cast)
            return cast
        }

        override fun getFormat(): TextFormat = this.format
        override fun getColor(): TextColor = this.format.color
        override fun getStyle(): XTextStyle = this.format.style as XTextStyle
        override fun getClickAction(): Optional<ClickAction<*>> = this.clickAction.optional()
        override fun getHoverAction(): Optional<HoverAction<*>> = this.hoverAction.optional()
        override fun getShiftClickAction(): Optional<ShiftClickAction<*>> = this.shiftClickAction.optional()
        override fun getChildren(): List<Text> = Collections.unmodifiableList(this.children)

        override fun format(format: TextFormat) = apply { this.format = format }
        override fun color(color: TextColor) = format(this.format.color(color))
        override fun style(vararg styles: TextStyle) = format(this.format.style(style.and(styles.asList())))
        override fun onClick(clickAction: ClickAction<*>?) = apply { this.clickAction = clickAction }
        override fun onHover(hoverAction: HoverAction<*>?) = apply { this.hoverAction = hoverAction }
        override fun onShiftClick(shiftClickAction: ShiftClickAction<*>?) = apply { this.shiftClickAction = shiftClickAction }

        override fun append(vararg children: Text) = append(children.asIterable())
        override fun append(children: Collection<Text>) = apply { this.children.addAll(children) }
        override fun append(children: Iterable<Text>) = apply { this.children.addAll(children) }
        override fun append(children: Iterator<Text>) = apply { this.children.addAll(children.asSequence()) }

        override fun insert(pos: Int, vararg children: Text) = apply { this.children.addAll(pos, children.asList()) }
        override fun insert(pos: Int, children: Collection<Text>) = apply { this.children.addAll(pos, children) }

        override fun insert(pos: Int, children: Iterable<Text>) = apply {
            var thePos = pos
            children.forEach { this.children.add(thePos++, it) }
        }

        override fun insert(pos: Int, children: Iterator<Text>) = apply {
            var thePos = pos
            children.forEach { this.children.add(thePos++, it) }
        }

        override fun remove(vararg children: Text) = apply { this.children.removeAll(children.asList()) }
        override fun remove(children: Collection<Text>) = apply { this.children.removeAll(children) }
        override fun remove(children: Iterable<Text>) = apply { this.children.removeAll(children) }
        override fun remove(children: Iterator<Text>) = apply { this.children.removeAll(children.asSequence()) }
        override fun removeAll() = apply { this.children.clear() }

        override fun trim() = apply {
            val front = this.children.iterator()
            while (front.hasNext()) {
                if (front.next().isEmpty) {
                    front.remove()
                } else {
                    break
                }
            }
            val back = this.children.listIterator(this.children.size)
            while (back.hasPrevious()) {
                if (back.previous().isEmpty) {
                    back.remove()
                } else {
                    break
                }
            }
        }

        abstract override fun build(): Text
        abstract override fun from(value: Text): B
        abstract override fun reset(): B

        override fun equals(other: Any?): Boolean {
            return this === other || (other is AbstractBuilder<*> &&
                    this.format == other.format &&
                    this.children == other.children &&
                    this.clickAction == other.clickAction &&
                    this.hoverAction == other.hoverAction &&
                    this.shiftClickAction == other.shiftClickAction)
        }

        override fun hashCode() = Objects.hashCode(this.format, this.clickAction, this.hoverAction, this.shiftClickAction, this.children)
        override fun toString() = toStringHelper().toString()
        override fun toText() = build()

        internal open fun toStringHelper(): ToStringHelper {
            return ToStringHelper(this)
                    .omitNullValues()
                    .add("format", if (this.format.isEmpty) null else this.format)
                    .add("children", if (this.children.isEmpty()) null else this.children)
                    .add("clickAction", this.clickAction)
                    .add("hoverAction", this.hoverAction)
                    .add("shiftClickAction", this.shiftClickAction)
        }
    }
}
