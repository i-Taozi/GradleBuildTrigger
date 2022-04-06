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
import com.canoo.dp.impl.remoting.BeanManagerImpl;
import com.canoo.dp.impl.remoting.ClassRepository;
import com.canoo.dp.impl.remoting.ClassRepositoryImpl;
import com.canoo.dp.impl.remoting.Converters;
import com.canoo.dp.impl.remoting.EventDispatcher;
import com.canoo.dp.impl.remoting.InternalAttributesBean;
import com.canoo.dp.impl.remoting.ListMapper;
import com.canoo.dp.impl.remoting.PresentationModelBuilderFactory;
import com.canoo.dp.impl.remoting.codec.OptimizedJsonCodec;
import com.canoo.dp.impl.remoting.collections.ListMapperImpl;
import com.canoo.dp.impl.remoting.commands.CallActionCommand;
import com.canoo.dp.impl.remoting.commands.CreateContextCommand;
import com.canoo.dp.impl.remoting.commands.CreateControllerCommand;
import com.canoo.dp.impl.remoting.commands.DestroyContextCommand;
import com.canoo.dp.impl.remoting.commands.DestroyControllerCommand;
import com.canoo.dp.impl.remoting.legacy.commands.InterruptLongPollCommand;
import com.canoo.dp.impl.remoting.legacy.commands.StartLongPollCommand;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.dp.impl.server.config.RemotingConfiguration;
import com.canoo.dp.impl.server.controller.ControllerHandler;
import com.canoo.dp.impl.server.controller.ControllerRepository;
import com.canoo.dp.impl.server.gc.GarbageCollectionCallback;
import com.canoo.dp.impl.server.gc.GarbageCollector;
import com.canoo.dp.impl.server.gc.Instance;
import com.canoo.dp.impl.server.legacy.ServerConnector;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import com.canoo.dp.impl.server.legacy.action.DolphinServerAction;
import com.canoo.dp.impl.server.legacy.communication.ActionRegistry;
import com.canoo.dp.impl.server.legacy.communication.CommandHandler;
import com.canoo.dp.impl.server.mbean.DolphinContextMBeanRegistry;
import com.canoo.dp.impl.server.model.ServerBeanBuilder;
import com.canoo.dp.impl.server.model.ServerBeanBuilderImpl;
import com.canoo.dp.impl.server.model.ServerBeanRepository;
import com.canoo.dp.impl.server.model.ServerBeanRepositoryImpl;
import com.canoo.dp.impl.server.model.ServerControllerActionCallBean;
import com.canoo.dp.impl.server.model.ServerEventDispatcher;
import com.canoo.dp.impl.server.model.ServerPlatformBeanRepository;
import com.canoo.dp.impl.server.model.ServerPresentationModelBuilderFactory;
import com.canoo.dp.impl.server.servlet.ServerTimingFilter;
import com.canoo.platform.core.functional.Subscription;
import com.canoo.platform.remoting.BeanManager;
import com.canoo.platform.server.client.ClientSession;
import com.canoo.platform.server.spi.components.ManagedBeanFactory;
import com.canoo.platform.server.timing.Metric;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * This class defines the central entry point for a Dolphin Platform session on the server.
 * Each Dolphin Platform client context on the client side is connected with one {@link DolphinContext}.
 */
@API(since = "0.x", status = INTERNAL)
public class DolphinContext {

    private static final Logger LOG = LoggerFactory.getLogger(DolphinContext.class);

    private final RemotingConfiguration configuration;

    private final ServerModelStore serverModelStore;

    private final ServerConnector serverConnector;

    private final ServerBeanRepository beanRepository;

    private final Converters converters;

    private final BeanManager beanManager;

    private final ControllerHandler controllerHandler;

    private final EventDispatcher dispatcher;

    private ServerPlatformBeanRepository platformBeanRepository;

    private final DolphinContextMBeanRegistry mBeanRegistry;

    private final Consumer<DolphinContext> onDestroyCallback;

    private final Subscription mBeanSubscription;

    private final GarbageCollector garbageCollector;

    private final DolphinContextTaskQueue taskQueue;

    private final ClientSession clientSession;

    private boolean hasResponseCommands = false;

    private boolean active = false;

    public DolphinContext(final RemotingConfiguration configuration, ClientSession clientSession, ClientSessionProvider clientSessionProvider, ManagedBeanFactory beanFactory, ControllerRepository controllerRepository, Consumer<DolphinContext> onDestroyCallback) {
        this.configuration = Assert.requireNonNull(configuration, "configuration");
        Assert.requireNonNull(beanFactory, "beanFactory");
        Assert.requireNonNull(controllerRepository, "controllerRepository");
        this.onDestroyCallback = Assert.requireNonNull(onDestroyCallback, "onDestroyCallback");
        this.clientSession = Assert.requireNonNull(clientSession, "clientSession");

        //Init Open Dolphin
        serverModelStore = new ServerModelStore();

        //Init Server Connector
        serverConnector = new ServerConnector();
        serverConnector.setCodec(OptimizedJsonCodec.getInstance());
        serverConnector.setServerModelStore(serverModelStore);
        serverConnector.registerDefaultActions();

        //Init Garbage Collection
        garbageCollector = new GarbageCollector(configuration, new GarbageCollectionCallback() {
            @Override
            public void onReject(Set<Instance> instances) {
                for (Instance instance : instances) {
                    beanRepository.onGarbageCollectionRejection(instance.getBean());
                }
            }
        });

        CommunicationManager manager = new CommunicationManager() {
            @Override
            public boolean hasResponseCommands() {
                return hasResponseCommands || serverModelStore.hasResponseCommands();
            }
        };
        taskQueue = new DolphinContextTaskQueue(clientSession.getId(), clientSessionProvider, manager, configuration.getMaxPollTime(), TimeUnit.MILLISECONDS);

        //Init BeanRepository
        dispatcher = new ServerEventDispatcher(serverModelStore);
        beanRepository = new ServerBeanRepositoryImpl(serverModelStore, dispatcher, garbageCollector);
        converters = new Converters(beanRepository);

        //Init BeanManager
        final PresentationModelBuilderFactory builderFactory = new ServerPresentationModelBuilderFactory(serverModelStore);
        final ClassRepository classRepository = new ClassRepositoryImpl(serverModelStore, converters, builderFactory);
        final ListMapper listMapper = new ListMapperImpl(serverModelStore, classRepository, beanRepository, builderFactory, dispatcher);
        final ServerBeanBuilder beanBuilder = new ServerBeanBuilderImpl(classRepository, beanRepository, listMapper, builderFactory, dispatcher, garbageCollector);
        beanManager = new BeanManagerImpl(beanRepository, beanBuilder);

        //Init MBean Support
        mBeanRegistry = new DolphinContextMBeanRegistry(clientSession.getId());

        //Init ControllerHandler
        controllerHandler = new ControllerHandler(mBeanRegistry, beanFactory, beanBuilder, beanRepository, controllerRepository, converters);

        //Register commands
        registerDolphinPlatformDefaultCommands();
        mBeanSubscription = mBeanRegistry.registerDolphinContext(clientSession, garbageCollector);
    }

    protected <T extends Command> void registerCommand(final ActionRegistry registry, final Class<T> commandClass, final Consumer<T> handler) {
        Assert.requireNonNull(registry, "registry");
        Assert.requireNonNull(commandClass, "commandClass");
        Assert.requireNonNull(handler, "handler");
        registry.register(commandClass, new CommandHandler() {
            @Override
            public void handleCommand(final Command command, final List response) {
                LOG.trace("Handling {} for DolphinContext {}", commandClass.getSimpleName(), getId());
                handler.accept((T) command);
            }
        });
    }

    private void registerDolphinPlatformDefaultCommands() {
        serverConnector.register(new DolphinServerAction() {
            @Override
            public void registerIn(ActionRegistry registry) {
                registerCommand(registry, CreateContextCommand.class, (c) -> onInitContext());
                registerCommand(registry, DestroyContextCommand.class, (c) -> onDestroyContext());
                registerCommand(registry, CreateControllerCommand.class, (createControllerCommand) -> {
                    Assert.requireNonNull(createControllerCommand, "createControllerCommand");
                    onCreateController(createControllerCommand.getControllerName(), createControllerCommand.getParentControllerId());
                });
                registerCommand(registry, DestroyControllerCommand.class, (destroyControllerCommand) -> {
                    Assert.requireNonNull(destroyControllerCommand, "destroyControllerCommand");
                    onDestroyController(destroyControllerCommand.getControllerId());
                });
                registerCommand(registry, CallActionCommand.class, (callActionCommand) -> {
                    Assert.requireNonNull(callActionCommand, "callActionCommand");
                    onCallControllerAction(callActionCommand.getControllerId(), callActionCommand.getActionName(), callActionCommand.getParams());
                });
                registerCommand(registry, StartLongPollCommand.class, (c) -> onLongPoll());
                registerCommand(registry, InterruptLongPollCommand.class, (c) -> interrupt());
            }
        });
    }

    private void onInitContext() {
        platformBeanRepository = new ServerPlatformBeanRepository(serverModelStore, beanRepository, dispatcher, converters);
    }

    private void onDestroyContext() {
        destroy();
    }

    public void destroy() {
        controllerHandler.destroyAllControllers();

        if (mBeanSubscription != null) {
            mBeanSubscription.unsubscribe();
        }

        onDestroyCallback.accept(this);
    }

    private void onCreateController(final String controllerName, final String parentControllerId) {
        Assert.requireNonBlank(controllerName, "controllerName");

        if (platformBeanRepository == null) {
            throw new IllegalStateException("An action was called before the init-command was sent.");
        }
        //TODO: Remove this. Should be handled by commands. See https://github.com/canoo/dolphin-platform/issues/522
        final InternalAttributesBean bean = platformBeanRepository.getInternalAttributesBean();
        final String controllerId = controllerHandler.createController(controllerName, parentControllerId);

        bean.setControllerId(controllerId);
        Object model = controllerHandler.getControllerModel(controllerId);
        if (model != null) {
            bean.setModel(model);
        }
    }

    private void onDestroyController(final String controllerId) {
        Assert.requireNonBlank(controllerId, "controllerId");
        if (platformBeanRepository == null) {
            throw new IllegalStateException("An action was called before the init-command was sent.");
        }
        controllerHandler.destroyController(controllerId);
    }

    private void onCallControllerAction(final String controllerId, final String actionName, final Map<String, Object> params) {
        Assert.requireNonBlank(controllerId, "controllerId");
        Assert.requireNonBlank(actionName, "actionName");
        Assert.requireNonNull(params, "params");

        //TODO: Remove this. Should bve handled by commands.
        final ServerControllerActionCallBean bean = platformBeanRepository.getControllerActionCallBean();
        Assert.requireNonNull(bean, "bean");

        if (platformBeanRepository == null) {
            throw new IllegalStateException("An action was called before the init-command was sent.");
        }
        final Metric metric = ServerTimingFilter.getCurrentTiming().start("RemotingActionCall:"+actionName, "Remote action call");
        try {
            controllerHandler.invokeAction(controllerId, actionName, params);
        } catch (final Exception e) {
            LOG.error("Unexpected exception while invoking action {} on controller {}",
                    actionName, controllerId, e);
            bean.setError(true);
        } finally {
            metric.stop();
        }
    }

    public void interrupt() {
        taskQueue.interrupt();
    }

    protected void onLongPoll() {
        if (configuration.isUseGc()) {
            LOG.trace("Handling GarbageCollection for DolphinContext {}", getId());
            onGarbageCollection();
        }
        final Metric metric = ServerTimingFilter.getCurrentTiming().start("TaskExecution", "Execution of Tasks in Long Poll");
        try {
            taskQueue.executeTasks();
        } finally {
            metric.stop();
        }
    }

    private void onGarbageCollection() {
        final Metric metric = ServerTimingFilter.getCurrentTiming().start("RemotingGc", "Garbage collection for the remoting model");
        try {
            garbageCollector.gc();
        } finally {
            metric.stop();
        }
    }

    public ServerModelStore getServerModelStore() {
        return serverModelStore;
    }

    public ServerConnector getServerConnector() {
        return serverConnector;
    }

    public BeanManager getBeanManager() {
        return beanManager;
    }

    public String getId() {
        return clientSession.getId();
    }

    public List<Command> handle(final List<Command> commands) {
        active = true;
        try {
        final List<Command> results = new LinkedList<>();
            for (final Command command : commands) {
                results.addAll(serverConnector.receive(command));
                hasResponseCommands = !results.isEmpty();
            }
            return results;
        } finally {
            active = false;
        }
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DolphinContext that = (DolphinContext) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public Future<Void> runLater(final Runnable runnable) {
        Assert.requireNonNull(runnable, "runnable");
        return callLater(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
        });
    }

    public <T> Future<T> callLater(final Callable<T> callable) {
        return taskQueue.addTask(callable);
    }

    public boolean isActive() {
        return active;
    }
}
