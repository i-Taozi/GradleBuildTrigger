/*
 * Copyright 2015-2018 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.canoo.impl.dp.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class DolphinLoggerThreadFactory implements ThreadFactory {

    private final static Logger LOG = LoggerFactory.getLogger(DolphinLoggerThreadFactory.class);

    private final static Marker DOLPHIN_LOGGING_MARKER = DolphinLogger.createMarker("DolphinLogging");

    private final static String NAME = "Dolphin Platform Logging Thread";

    private AtomicLong counter = new AtomicLong(0);

    @Override
    public synchronized Thread newThread(Runnable task) {
        Objects.requireNonNull(task, "task");
        return AccessController.doPrivileged(new PrivilegedAction<Thread>() {
            @Override
            public Thread run() {
                final Thread backgroundThread = new Thread(task);
                backgroundThread.setName(NAME + " " + counter.incrementAndGet());
                backgroundThread.setDaemon(false);
                backgroundThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error(DOLPHIN_LOGGING_MARKER, "Error in Dolphin Logger Thread", e);
                    }
                });
                return backgroundThread;
            }
        });
    }
}
