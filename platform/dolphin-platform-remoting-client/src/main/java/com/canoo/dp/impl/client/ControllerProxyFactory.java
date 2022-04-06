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

import com.canoo.platform.remoting.client.ControllerProxy;
import com.canoo.dp.impl.remoting.Converters;
import com.canoo.dp.impl.remoting.InternalAttributesBean;
import com.canoo.dp.impl.remoting.commands.CreateControllerCommand;
import com.canoo.dp.impl.remoting.BeanRepository;
import com.canoo.dp.impl.remoting.EventDispatcher;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import org.apiguardian.api.API;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ControllerProxyFactory {

    private final ClientPlatformBeanRepository platformBeanRepository;

    private final DolphinCommandHandler dolphinCommandHandler;

    private final AbstractClientConnector clientConnector;

    private final Converters converters;

    public ControllerProxyFactory(final DolphinCommandHandler dolphinCommandHandler, final AbstractClientConnector clientConnector, final ClientModelStore modelStore, final BeanRepository beanRepository, final EventDispatcher dispatcher, final Converters converters) {
        this.converters = Assert.requireNonNull(converters, "converters");
        this.platformBeanRepository = new ClientPlatformBeanRepository(modelStore, beanRepository, dispatcher, converters);
        this.dolphinCommandHandler = Assert.requireNonNull(dolphinCommandHandler, "dolphinCommandHandler");
        this.clientConnector = Assert.requireNonNull(clientConnector, "clientConnector");
    }

    public <T> CompletableFuture<ControllerProxy<T>> create(String name) {
       return create(name, null);
    }

    public <T> CompletableFuture<ControllerProxy<T>> create(String name, String parentControllerId) {
        Assert.requireNonBlank(name, "name");
        final InternalAttributesBean bean = platformBeanRepository.getInternalAttributesBean();

        final CreateControllerCommand createControllerCommand = new CreateControllerCommand();
        createControllerCommand.setControllerName(name);
        if(parentControllerId != null) {
            createControllerCommand.setParentControllerId(parentControllerId);
        }

        return dolphinCommandHandler.invokeDolphinCommand(createControllerCommand).thenApply(new Function<Void, ControllerProxy<T>>() {
            @Override
            public ControllerProxy<T> apply(Void aVoid) {
                return new ControllerProxyImpl<T>(bean.getControllerId(), (T) bean.getModel(), clientConnector, platformBeanRepository, ControllerProxyFactory.this, converters);
            }
        });
    }
}
