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

import org.lanternpowered.server.network.message.Message;
import org.spongepowered.api.text.Text;

public abstract class MessagePlayOutScoreboardScore implements Message {

    private final String objectiveName;
    private final Text scoreName;

    public MessagePlayOutScoreboardScore(String objectiveName, Text scoreName) {
        this.objectiveName = objectiveName;
        this.scoreName = scoreName;
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public Text getScoreName() {
        return this.scoreName;
    }

    public static final class CreateOrUpdate extends MessagePlayOutScoreboardScore {

        private final int value;

        public CreateOrUpdate(String objectiveName, Text scoreName, int value) {
            super(objectiveName, scoreName);
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static final class Remove extends MessagePlayOutScoreboardScore {

        public Remove(String objectiveName, Text scoreName) {
            super(objectiveName, scoreName);
        }
    }
}
