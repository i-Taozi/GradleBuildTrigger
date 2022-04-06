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
package com.canoo.impl.dp.spring.test;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.legacy.commands.StartLongPollCommand;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.dp.impl.server.config.RemotingConfiguration;
import com.canoo.dp.impl.server.context.DolphinContext;
import com.canoo.dp.impl.server.controller.ControllerRepository;
import com.canoo.dp.impl.server.legacy.communication.CommandHandler;
import com.canoo.platform.server.client.ClientSession;
import com.canoo.platform.server.spi.components.ManagedBeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Created by hendrikebbers on 05.12.17.
 */
public class TestDolphinContext extends DolphinContext {

    private final BlockingQueue<Runnable> callLaterTasks = new LinkedBlockingQueue<>();

    public TestDolphinContext(final RemotingConfiguration configuration, final ClientSession clientSession, final ClientSessionProvider clientSessionProvider, final ManagedBeanFactory beanFactory, final ControllerRepository controllerRepository, final Consumer<DolphinContext> onDestroyCallback) {
        super(configuration, clientSession, clientSessionProvider, beanFactory, controllerRepository, onDestroyCallback);

        getServerConnector().getRegistry().register(PingCommand.class, new CommandHandler() {
            @Override
            public void handleCommand(Command command, List response) {

            }
        });
    }

    @Override
    public List<Command> handle(final List<Command> commands) {
        final List<Command> commandsWithFackedLongPool = new ArrayList<>(commands);
        commandsWithFackedLongPool.add(new StartLongPollCommand());

        return super.handle(commandsWithFackedLongPool);
    }

    @Override
    protected void onLongPoll() {
        while (!callLaterTasks.isEmpty()) {
            callLaterTasks.poll().run();
        }
    }

    @Override
    public <T> Future<T> callLater(final Callable<T> callable) {
        Assert.requireNonNull(callable, "callable");
        final CompletableFuture<T> result = new CompletableFuture<T>();
        callLaterTasks.offer(() -> {
            try {
                T taskResult = callable.call();
                result.complete(taskResult);
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }
}
