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
package com.canoo.dp.impl.client;

import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.DefaultModelSynchronizer;
import com.canoo.dp.impl.client.legacy.ModelSynchronizer;
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.BeanManagerImpl;
import com.canoo.dp.impl.remoting.BeanRepository;
import com.canoo.dp.impl.remoting.BeanRepositoryImpl;
import com.canoo.dp.impl.remoting.ClassRepository;
import com.canoo.dp.impl.remoting.ClassRepositoryImpl;
import com.canoo.dp.impl.remoting.Converters;
import com.canoo.dp.impl.remoting.EventDispatcher;
import com.canoo.dp.impl.remoting.PresentationModelBuilderFactory;
import com.canoo.dp.impl.remoting.collections.ListMapperImpl;
import com.canoo.dp.impl.remoting.commands.CreateContextCommand;
import com.canoo.dp.impl.remoting.commands.DestroyContextCommand;
import com.canoo.platform.client.ClientConfiguration;
import com.canoo.platform.client.session.ClientSessionStore;
import com.canoo.platform.remoting.BeanManager;
import com.canoo.platform.remoting.DolphinRemotingException;
import com.canoo.platform.remoting.client.ClientContext;
import com.canoo.platform.remoting.client.ClientInitializationException;
import com.canoo.platform.remoting.client.ControllerInitalizationException;
import com.canoo.platform.remoting.client.ControllerProxy;
import org.apiguardian.api.API;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ClientContextImpl implements ClientContext {

    private final ClientConfiguration clientConfiguration;

    private final Function<ClientModelStore, AbstractClientConnector> connectorProvider;

    private final URI endpoint;

    private final ClientSessionStore clientSessionStore;

    private final AbstractClientConnector clientConnector;

    private final ClientModelStore modelStore;

    @Deprecated
    private  final BeanManager clientBeanManager;

    private final ControllerProxyFactory controllerProxyFactory;

    private final DolphinCommandHandler dolphinCommandHandler;

    public ClientContextImpl(final ClientConfiguration clientConfiguration, final URI endpoint, final Function<ClientModelStore, AbstractClientConnector> connectorProvider, final ClientSessionStore clientSessionStore) {
        this.clientConfiguration = Assert.requireNonNull(clientConfiguration, "clientConfiguration");
        this.connectorProvider = Assert.requireNonNull(connectorProvider, "connectorProvider");
        this.clientSessionStore = Assert.requireNonNull(clientSessionStore, "clientSessionStore");
        this.endpoint = Assert.requireNonNull(endpoint, "endpoint");

        final ModelSynchronizer defaultModelSynchronizer = new DefaultModelSynchronizer(new Supplier<AbstractClientConnector>() {
            @Override
            public AbstractClientConnector get() {
                return clientConnector;
            }
        });

        this.modelStore = new ClientModelStore(defaultModelSynchronizer);
        this.clientConnector = connectorProvider.apply(modelStore);

        final EventDispatcher dispatcher = new ClientEventDispatcher(modelStore);
        final BeanRepository beanRepository = new BeanRepositoryImpl(modelStore, dispatcher);
        final Converters converters = new Converters(beanRepository);
        final PresentationModelBuilderFactory builderFactory = new ClientPresentationModelBuilderFactory(modelStore);
        final ClassRepository classRepository = new ClassRepositoryImpl(modelStore, converters, builderFactory);

        this.dolphinCommandHandler = new DolphinCommandHandler(clientConnector);
        this.controllerProxyFactory = new ControllerProxyFactory(dolphinCommandHandler, clientConnector, modelStore, beanRepository, dispatcher, converters);
        this.clientBeanManager = new BeanManagerImpl(beanRepository, new ClientBeanBuilderImpl(classRepository, beanRepository, new ListMapperImpl(modelStore, classRepository, beanRepository, builderFactory, dispatcher), builderFactory, dispatcher));
    }

    protected DolphinCommandHandler getDolphinCommandHandler() {
        return dolphinCommandHandler;
    }

    @Override
    public synchronized <T> CompletableFuture<ControllerProxy<T>> createController(final String name) {
        Assert.requireNonBlank(name, "name");

        if (controllerProxyFactory == null) {
            throw new IllegalStateException("connect was not called!");
        }

        return controllerProxyFactory.<T>create(name).handle((ControllerProxy<T> controllerProxy, Throwable throwable) -> {
            if (throwable != null) {
                throw new ControllerInitalizationException(throwable);
            }
            return controllerProxy;
        });
    }

    @Override
    public synchronized BeanManager getBeanManager() {
        return clientBeanManager;
    }

    @Override
    public synchronized CompletableFuture<Void> disconnect() {
        final CompletableFuture<Void> result = new CompletableFuture<>();

        clientConfiguration.getBackgroundExecutor().execute(() -> {
            dolphinCommandHandler.invokeDolphinCommand(new DestroyContextCommand()).handle((Void aVoid, Throwable throwable) -> {

                clientConnector.disconnect();
                clientSessionStore.resetSession(endpoint);
                if (throwable != null) {
                    result.completeExceptionally(new DolphinRemotingException("Can't disconnect", throwable));
                } else {
                    result.complete(null);
                }
                return null;
            });
        });
        return result;
    }

    @Override
    public CompletableFuture<Void> connect() {

        final CompletableFuture<Void> result = new CompletableFuture<>();
        clientConnector.connect();

        clientConfiguration.getBackgroundExecutor().execute(() -> {
            dolphinCommandHandler.invokeDolphinCommand(new CreateContextCommand()).handle((Void aVoid, Throwable throwable) -> {
                if (throwable != null) {
                    result.completeExceptionally(new ClientInitializationException("Can't call init action!", throwable));
                } else {
                }
                result.complete(null);
                return null;
            });
        });
        return result;
    }

    @Override
    public String getClientId() {
        return clientSessionStore.getClientIdentifierForUrl(endpoint);
    }

}
