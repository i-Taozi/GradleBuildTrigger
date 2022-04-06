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
package com.canoo.impl.server.util;

import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.DefaultModelSynchronizer;
import com.canoo.dp.impl.client.legacy.ModelSynchronizer;
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import com.canoo.dp.impl.client.legacy.communication.CommandBatcher;
import com.canoo.dp.impl.server.legacy.ServerConnector;
import com.canoo.dp.impl.server.legacy.ServerModelStore;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Base class for running a client and server dolphin inside the same VM.
 * <p>
 * Subclasses JavaFxInMemoryConfig and SwingInMemoryConfig additionally set the threading model
 * as appropriate for the UI (JavaFX or Swing, respectively.)
 */
public class DefaultInMemoryConfig implements Supplier<AbstractClientConnector> {

    private final ClientModelStore clientModelStore;

    private final ServerModelStore serverModelStore;

    private final InMemoryClientConnector clientConnector;

    private final ServerConnector serverConnector;

    public DefaultInMemoryConfig(final Executor uiExecutor) {
        serverModelStore = new ServerModelStore();
        serverConnector = new ServerConnector();
        serverConnector.setServerModelStore(serverModelStore);
        ModelSynchronizer defaultModelSynchronizer = new DefaultModelSynchronizer(this);
        clientModelStore = new ClientModelStore(defaultModelSynchronizer);
        clientConnector = new InMemoryClientConnector(clientModelStore, serverConnector, new CommandBatcher(), uiExecutor);
        clientConnector.setSleepMillis(100);
    }

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

    @Override
    public AbstractClientConnector get() {
        return clientConnector;
    }
}
