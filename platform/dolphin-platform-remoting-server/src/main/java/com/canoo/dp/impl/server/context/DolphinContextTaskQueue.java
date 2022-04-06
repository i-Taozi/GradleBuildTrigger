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
package com.canoo.dp.impl.server.context;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.platform.server.client.ClientSession;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Internal class to call tasks (see {@link Runnable}) in a Dolphin Platform context
 * (see {@link DolphinContext}). Tasks can come from an "invokeLater" call or the event bus.
 */
@API(since = "0.x", status = INTERNAL)
public class DolphinContextTaskQueue {

    private static final Logger LOG = LoggerFactory.getLogger(DolphinContextTaskQueue.class);

    private final BlockingQueue<Runnable> tasks;

    private final String dolphinSessionId;

    private final long maxExecutionTime;

    private final TimeUnit maxExecutionTimeUnit;

    private final ClientSessionProvider sessionProvider;

    private final CommunicationManager communicationManager;

    private final Lock taskLock = new ReentrantLock();

    private final Condition taskCondition = taskLock.newCondition();

    private final AtomicBoolean interrupted = new AtomicBoolean(false);

    public DolphinContextTaskQueue(final String dolphinSessionId, final ClientSessionProvider sessionProvider, final CommunicationManager communicationManager, final long maxExecutionTime, final TimeUnit maxExecutionTimeUnit) {
        this.dolphinSessionId = Assert.requireNonBlank(dolphinSessionId, "dolphinSessionId");
        this.tasks = new LinkedBlockingQueue<>();
        this.communicationManager = Assert.requireNonNull(communicationManager, "communicationManager");
        this.sessionProvider = Assert.requireNonNull(sessionProvider, "sessionProvider");
        this.maxExecutionTime = maxExecutionTime;
        this.maxExecutionTimeUnit = Assert.requireNonNull(maxExecutionTimeUnit, "maxExecutionTimeUnit");
    }

    public <T> Future<T> addTask(final Callable<T> task) {
        Assert.requireNonNull(task, "task");
        final CompletableFuture<T> future = new CompletableFuture<>();
        tasks.offer(new Runnable() {
            @Override
            public void run() {
                try {
                    final T result = task.call();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });
        LOG.trace("Tasks added to Dolphin Platform context {}", dolphinSessionId);
        taskLock.lock();
        try {
            taskCondition.signal();
        } finally {
            taskLock.unlock();
        }
        return future;
    }

    public void interrupt() {
        taskLock.lock();
        try {
            interrupted.set(true);
            LOG.trace("Tasks in Dolphin Platform context {} interrupted", dolphinSessionId);
            taskCondition.signal();
        } finally {
            taskLock.unlock();
        }
    }

    public void executeTasks() {
        final ClientSession currentSession = sessionProvider.getCurrentClientSession();
        if (currentSession == null || !dolphinSessionId.equals(currentSession.getId())) {
            throw new IllegalStateException("Not in Dolphin Platform session " + dolphinSessionId);
        }

        LOG.trace("Running {} tasks in Dolphin Platform session {}", tasks.size(), dolphinSessionId);
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + maxExecutionTimeUnit.toMillis(maxExecutionTime);

        while (!communicationManager.hasResponseCommands()) {
            if (interrupted.get()) {
                interrupted.set(false);
                break;
            }
            final Runnable task = tasks.poll();
            if (task == null) {
                try {
                    taskLock.lock();
                    try {
                        if (tasks.isEmpty() && !taskCondition.await(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)) {
                            interrupted.set(false);
                            break;
                        }
                    } finally {
                        taskLock.unlock();
                    }
                    if (tasks.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    String exceptionMessage =
                        String.format(
                            "Concurrency error in task executor for Dolphin Platform session %s",
                            dolphinSessionId);
                    LOG.error(exceptionMessage, e);
                    throw new IllegalStateException(exceptionMessage, e);
                }
            } else {
                try {
                    task.run();
                    LOG.trace("Task executor executed task in Dolphin Platform session {}", dolphinSessionId);
                } catch (Exception e) {
                    String exceptionMessage =
                        String.format(
                            "Error in running task in Dolphin Platform session %s",
                            dolphinSessionId);
                    LOG.error(exceptionMessage, e);
                    throw new DolphinTaskException(exceptionMessage, e);
                }
            }
        }
        final long runTime = System.currentTimeMillis() - startTime;
        LOG.trace("Task executor for Dolphin Platform session {} ended after {} seconds with {} task still open", dolphinSessionId, maxExecutionTimeUnit.toSeconds(runTime), tasks.size());
    }
}
