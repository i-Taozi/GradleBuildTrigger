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
package org.lanternpowered.api.script.function.value;

import org.lanternpowered.api.script.Parameter;
import org.lanternpowered.api.script.ScriptContext;
import org.lanternpowered.server.script.LanternRandom;

@FunctionalInterface
public interface IntValueProvider {

    static Constant constant(int value) {
        return new Constant(value);
    }

    static Range range(int min, int max) {
        return new Range(min, max);
    }

    int get(@Parameter(ScriptContext.CONTEXT_PARAMETER) ScriptContext scriptContext);

    final class Constant implements IntValueProvider {

        private final int value;

        private Constant(int value) {
            this.value = value;
        }

        @Override
        public int get(@Parameter(ScriptContext.CONTEXT_PARAMETER) ScriptContext scriptContext) {
            return this.value;
        }
    }

    final class Range implements IntValueProvider {

        private final int min;
        private final int max;

        private Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public int get(@Parameter(ScriptContext.CONTEXT_PARAMETER) ScriptContext scriptContext) {
            return LanternRandom.$random.range(this.min, this.max);
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }
    }
}
