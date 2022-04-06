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
package com.canoo.dp.impl.platform.client.session;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.core.PlatformConstants;
import com.canoo.platform.core.DolphinRuntimeException;
import com.canoo.platform.core.http.HttpURLConnectionHandler;
import com.canoo.platform.client.session.ClientSessionStore;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ClientSessionRequestHandler implements HttpURLConnectionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClientSessionRequestHandler.class);

    private final ClientSessionStore clientSessionStore;

    public ClientSessionRequestHandler(final ClientSessionStore clientSessionStore) {
        this.clientSessionStore = Assert.requireNonNull(clientSessionStore, "clientSessionStore");
    }

    @Override
    public void handle(final HttpURLConnection request){
        Assert.requireNonNull(request, "request");
        final String clientId;
        try {
            clientId = clientSessionStore.getClientIdentifierForUrl(request.getURL().toURI());
            if (clientId != null) {
                LOG.debug("Adding client id {} to http request at {}", clientId, request.getURL());
                request.setRequestProperty(PlatformConstants.CLIENT_ID_HTTP_HEADER_NAME, clientId);
            } else {
                LOG.debug("Request to application at {} without client id. PlatformClient id not defined until now.", request.getURL());
            }
        } catch (URISyntaxException e) {
            LOG.error("Exception while converting to request URL {} to URI", request.getURL());
            throw new DolphinRuntimeException("Exception while converting URL "+request.getURL() +"to URI", e);
        }

    }
}
