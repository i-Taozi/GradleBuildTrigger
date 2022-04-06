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
package com.canoo.dp.impl.platform.core;

import com.canoo.dp.impl.platform.core.context.ContextManagerImpl;
import com.canoo.platform.core.concurrent.PlatformThreadFactory;
import org.apiguardian.api.API;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.canoo.dp.impl.platform.core.PlatformConstants.THREAD_CONTEXT;
import static com.canoo.dp.impl.platform.core.PlatformConstants.THREAD_GROUP_NAME;
import static com.canoo.dp.impl.platform.core.PlatformConstants.THREAD_NAME_PREFIX;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class SimpleDolphinPlatformThreadFactory implements PlatformThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(0);

    private final Lock uncaughtExceptionHandlerLock = new ReentrantLock();

    private final ThreadGroup group;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public SimpleDolphinPlatformThreadFactory(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = Assert.requireNonNull(uncaughtExceptionHandler, "uncaughtExceptionHandler");
        this.group = new ThreadGroup(THREAD_GROUP_NAME);
    }

    public SimpleDolphinPlatformThreadFactory() {
        this(new SimpleUncaughtExceptionHandler());
    }

    @Override
    public Thread newThread(final Runnable task) {
        Assert.requireNonNull(task, "task");
        return AccessController.doPrivileged(new PrivilegedAction<Thread>() {
            @Override
            public Thread run() {
                final String name = THREAD_NAME_PREFIX + threadNumber.getAndIncrement();
                final Thread backgroundThread = new Thread(group, () -> {
                    ContextManagerImpl.getInstance().addThreadContext(THREAD_CONTEXT, name);
                    task.run();
                });
                backgroundThread.setName(name);
                backgroundThread.setDaemon(false);
                uncaughtExceptionHandlerLock.lock();
                try {
                    backgroundThread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                } finally {
                    uncaughtExceptionHandlerLock.unlock();
                }
                return backgroundThread;
            }
        });
    }

    public void setUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        Assert.requireNonNull(uncaughtExceptionHandler, "uncaughtExceptionHandler");
        uncaughtExceptionHandlerLock.lock();
        try {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        } finally {
            uncaughtExceptionHandlerLock.unlock();
        }
    }
}
