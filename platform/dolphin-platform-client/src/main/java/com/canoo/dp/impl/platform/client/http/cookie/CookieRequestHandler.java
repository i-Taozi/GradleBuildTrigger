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
package com.canoo.dp.impl.platform.client.http.cookie;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.core.http.HttpURLConnectionHandler;
import com.canoo.platform.core.DolphinRuntimeException;
import org.apiguardian.api.API;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class CookieRequestHandler implements HttpURLConnectionHandler {

    private final HttpClientCookieHandler clientCookieHandler;

    public CookieRequestHandler(final HttpClientCookieHandler clientCookieHandler) {
        this.clientCookieHandler = Assert.requireNonNull(clientCookieHandler, "clientCookieHandler");
    }

    @Override
    public void handle(final HttpURLConnection connection) {
        Assert.requireNonNull(connection, "connection");
        try {
            clientCookieHandler.setRequestCookies(connection);
        } catch (URISyntaxException e) {
            throw new DolphinRuntimeException("Can not set cookies", e);
        }
    }
}
