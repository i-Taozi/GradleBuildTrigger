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
package org.lanternpowered.server.util;

import org.lanternpowered.server.game.Lantern;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Thread started on shutdown that monitors for and kills rogue non-daemon threads.
 */
public class ShutdownMonitorThread extends Thread {

    private final long timeoutMillis;

    public ShutdownMonitorThread(long timeout, TimeUnit unit) {
        this.timeoutMillis = unit.toMillis(timeout);
        setName("shutdown-monitor");
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.timeoutMillis);
        } catch (InterruptedException e) {
            Lantern.getLogger().error("Shutdown monitor interrupted", e);
            System.exit(0);
            return;
        }

        Lantern.getLogger().warn("Still running after shutdown, finding rogue threads...");

        final Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
            final Thread thread = entry.getKey();
            final StackTraceElement[] stack = entry.getValue();

            if (thread.isDaemon() || !thread.isAlive() || stack.length == 0) {
                // won't keep JVM from exiting
                continue;
            }

            if (thread instanceof LanternThread) {
                final LanternThread lanternThread = (LanternThread) thread;
                Lantern.getLogger().warn("Rogue thread (lantern): " + lanternThread);
                Lantern.getLogger().warn("    construction location:");
                final Throwable constructionSource = lanternThread.getConstructionSite();
                for (StackTraceElement trace : constructionSource.getStackTrace()) {
                    Lantern.getLogger().warn("        at " + trace);
                }
            } else {
                Lantern.getLogger().warn("Rogue thread: " + thread);
            }
            Lantern.getLogger().warn("    interrupt location:");
            for (StackTraceElement trace : stack) {
                Lantern.getLogger().warn("        at " + trace);
            }

            // ask nicely to kill them
            thread.interrupt();
            // wait for them to die on their own
            if (thread.isAlive()) {
                try {
                    thread.join(1000);
                } catch (InterruptedException ex) {
                    Lantern.getLogger().error("Shutdown monitor interrupted", ex);
                    System.exit(0);
                    return;
                }
            }
        }
        // kill them forcefully
        System.exit(0);
    }

}
