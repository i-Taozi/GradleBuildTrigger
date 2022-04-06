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
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import com.canoo.dp.impl.platform.client.session.StrictClientSessionResponseHandler;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.codec.OptimizedJsonCodec;
import com.canoo.platform.client.ClientConfiguration;
import com.canoo.platform.client.PlatformClient;
import com.canoo.platform.client.session.ClientSessionStore;
import com.canoo.platform.core.http.HttpClient;
import com.canoo.platform.core.http.HttpURLConnectionHandler;
import com.canoo.platform.remoting.client.ClientContext;
import com.canoo.platform.remoting.client.ClientContextFactory;
import com.canoo.platform.remoting.client.ClientInitializationException;
import org.apiguardian.api.API;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Factory to create a {@link ClientContext}. Normally you will create a {@link ClientContext} at the bootstrap of your
 * client by using the {@link #create(ClientConfiguration, URI)} method and use this context as a singleton in your client.
 * The {@link ClientContext} defines the connection between the client and the Dolphin Platform server endpoint.
 */
@API(since = "0.x", status = INTERNAL)
public class ClientContextFactoryImpl implements ClientContextFactory {

    public ClientContextFactoryImpl() {
    }

    /**
     * Create a {@link ClientContext} based on the given configuration. This method doesn't block and returns a
     * {@link CompletableFuture} to receive its result. If the {@link ClientContext} can't be created the
     * {@link CompletableFuture#get()} will throw a {@link ClientInitializationException}.
     *
     * @param clientConfiguration the configuration
     * @return the future
     */
    public ClientContext create(final ClientConfiguration clientConfiguration, final URI endpoint) {
        Assert.requireNonNull(clientConfiguration, "clientConfiguration");
        final HttpClient httpClient = PlatformClient.getService(HttpClient.class);
        final HttpURLConnectionHandler clientSessionCheckResponseHandler = new StrictClientSessionResponseHandler(endpoint);
        httpClient.addResponseHandler(clientSessionCheckResponseHandler);
        final Function<ClientModelStore, AbstractClientConnector> connectionProvider = s -> {
            return new DolphinPlatformHttpClientConnector(endpoint, clientConfiguration, s, OptimizedJsonCodec.getInstance(), e -> {}, httpClient);
        };


        return new ClientContextImpl(clientConfiguration, endpoint, connectionProvider, PlatformClient.getService(ClientSessionStore.class));
    }

}
