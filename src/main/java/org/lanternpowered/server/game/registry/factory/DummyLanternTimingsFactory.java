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
package org.lanternpowered.server.game.registry.factory;

import co.aikar.timings.Timing;
import co.aikar.timings.TimingsFactory;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.annotation.Nullable;

public class DummyLanternTimingsFactory implements TimingsFactory {

    private static final Timing DUMMY_TIMING = new DummyLanternTiming();

    public void init() {
        // Ignore
    }

    @Override
    public Timing of(Object plugin, String name, @Nullable Timing groupHandler) {
        return DUMMY_TIMING;
    }

    @Override
    public boolean isTimingsEnabled() {
        return false;
    }

    @Override
    public void setTimingsEnabled(boolean enabled) {
        // Ignore
    }

    @Override
    public boolean isVerboseTimingsEnabled() {
        return false;
    }

    @Override
    public void setVerboseTimingsEnabled(boolean enabled) {
        // Ignore
    }

    @Override
    public int getHistoryInterval() {
        return 0;
    }

    @Override
    public void setHistoryInterval(int interval) {
        // Ignore
    }

    @Override
    public int getHistoryLength() {
        return 0;
    }

    @Override
    public void setHistoryLength(int length) {
        // Ignore
    }

    @Override
    public void reset() {
        // Ignore
    }

    @Override
    public void generateReport(@Nullable CommandSource source) {
        // Ignore
    }

    @Override
    public void generateReport(MessageChannel channel) {
        // Ignore
    }
}
