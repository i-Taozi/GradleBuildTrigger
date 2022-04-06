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
package com.canoo.dolphin.client.util;

import com.canoo.dp.impl.client.ClientBeanBuilderImpl;
import com.canoo.dp.impl.client.ClientEventDispatcher;
import com.canoo.dp.impl.client.ClientPresentationModelBuilderFactory;
import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.DefaultModelSynchronizer;
import com.canoo.dp.impl.client.legacy.ModelSynchronizer;
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import com.canoo.dp.impl.remoting.BeanBuilder;
import com.canoo.dp.impl.remoting.BeanManagerImpl;
import com.canoo.dp.impl.remoting.BeanRepository;
import com.canoo.dp.impl.remoting.BeanRepositoryImpl;
import com.canoo.dp.impl.remoting.ClassRepository;
import com.canoo.dp.impl.remoting.ClassRepositoryImpl;
import com.canoo.dp.impl.remoting.Converters;
import com.canoo.dp.impl.remoting.EventDispatcher;
import com.canoo.dp.impl.remoting.ListMapper;
import com.canoo.dp.impl.remoting.PresentationModelBuilderFactory;
import com.canoo.dp.impl.remoting.collections.ListMapperImpl;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.remoting.legacy.util.DirectExecutor;
import com.canoo.dp.impl.server.legacy.ServerConnector;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import com.canoo.platform.remoting.BeanManager;

import java.util.ArrayList;

public abstract class AbstractDolphinBasedTest {

    public class DolphinTestConfiguration {

        private final ClientModelStore clientModelStore;

        private final ServerModelStore serverModelStore;

        private final InMemoryClientConnector clientConnector;

        private final ServerConnector serverConnector;


        public ClientModelStore getClientModelStore() {
            return clientModelStore;
        }

        public ServerModelStore getServerModelStore() {
            return serverModelStore;
        }

        public InMemoryClientConnector getClientConnector() {
            return clientConnector;
        }

        public ServerConnector getServerConnector() {
            return serverConnector;
        }

        public DolphinTestConfiguration(ClientModelStore clientModelStore, ServerModelStore serverModelStore, InMemoryClientConnector clientConnector, ServerConnector serverConnector) {
            this.clientModelStore = clientModelStore;
            this.serverModelStore = serverModelStore;
            this.clientConnector = clientConnector;
            this.serverConnector = serverConnector;
        }
    }

    protected ClientModelStore createClientModelStore(final AbstractClientConnector connector) {
        ModelSynchronizer defaultModelSynchronizer = new DefaultModelSynchronizer(() -> connector);
        ClientModelStore clientModelStore = new ClientModelStore(defaultModelSynchronizer);

        return clientModelStore;
    }

    protected DolphinTestConfiguration createDolphinTestConfiguration() {
        DefaultInMemoryConfig config = new DefaultInMemoryConfig(DirectExecutor.getInstance());
        config.getServerConnector().registerDefaultActions();
        ServerModelStore store = config.getServerModelStore();
        store.setCurrentResponse(new ArrayList<Command>());

        return new DolphinTestConfiguration(config.getClientModelStore(), config.getServerModelStore(), config.getClientConnector(), config.getServerConnector());
    }

    protected EventDispatcher createEventDispatcher(final ClientModelStore clientModelStore) {
        final EventDispatcher dispatcher = new ClientEventDispatcher(clientModelStore);
        return dispatcher;
    }

    protected BeanRepository createBeanRepository(final ClientModelStore clientModelStore, final EventDispatcher dispatcher) {
        final BeanRepositoryImpl beanRepository = new BeanRepositoryImpl(clientModelStore, dispatcher);
        return beanRepository;
    }

    protected BeanManager createBeanManager(final ClientModelStore clientModelStore, final BeanRepository beanRepository, final EventDispatcher dispatcher) {
        final Converters converters = new Converters(beanRepository);
        final PresentationModelBuilderFactory builderFactory = new ClientPresentationModelBuilderFactory(clientModelStore);
        final ClassRepository classRepository = new ClassRepositoryImpl(clientModelStore, converters, builderFactory);
        final ListMapper listMapper = new ListMapperImpl(clientModelStore, classRepository, beanRepository, builderFactory, dispatcher);
        final BeanBuilder beanBuilder = new ClientBeanBuilderImpl(classRepository, beanRepository, listMapper, builderFactory, dispatcher);
        return new BeanManagerImpl(beanRepository, beanBuilder);
    }

    protected BeanManager createBeanManager(final ClientModelStore clientModelStore) {
        final EventDispatcher dispatcher = createEventDispatcher(clientModelStore);
        final BeanRepository repository = createBeanRepository(clientModelStore, dispatcher);
        return createBeanManager(clientModelStore, repository, dispatcher);
    }
}
