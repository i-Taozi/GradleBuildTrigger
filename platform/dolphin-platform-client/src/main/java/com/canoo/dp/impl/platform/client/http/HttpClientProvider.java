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
package com.canoo.dp.impl.platform.client.http;

import com.canoo.dp.impl.platform.client.AbstractServiceProvider;
import com.canoo.platform.client.ClientConfiguration;
import com.canoo.platform.client.PlatformClient;
import com.canoo.platform.core.http.HttpClient;
import com.canoo.platform.core.http.HttpURLConnectionFactory;
import com.canoo.platform.core.http.spi.RequestHandlerProvider;
import com.canoo.platform.core.http.spi.ResponseHandlerProvider;
import com.google.gson.Gson;
import org.apiguardian.api.API;

import java.util.Iterator;
import java.util.ServiceLoader;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class HttpClientProvider extends AbstractServiceProvider<HttpClient> {

    public HttpClientProvider() {
        super(HttpClient.class);
    }

    @Override
    protected HttpClient createService(ClientConfiguration configuration) {
        final HttpURLConnectionFactory connectionFactory = configuration.getHttpURLConnectionFactory();
        final HttpClientImpl client = new HttpClientImpl(PlatformClient.getService(Gson.class), connectionFactory, configuration);

        final ServiceLoader<RequestHandlerProvider> requestLoader = ServiceLoader.load(RequestHandlerProvider.class);
        final Iterator<RequestHandlerProvider> requestIterator = requestLoader.iterator();
        while (requestIterator.hasNext()) {
            client.addRequestHandler(requestIterator.next().getHandler(configuration));
        }

        final ServiceLoader<ResponseHandlerProvider> responseLoader = ServiceLoader.load(ResponseHandlerProvider.class);
        final Iterator<ResponseHandlerProvider> responseIterator = responseLoader.iterator();
        while (responseIterator.hasNext()) {
            client.addResponseHandler(responseIterator.next().getHandler(configuration));
        }
        return client;
    }
}
