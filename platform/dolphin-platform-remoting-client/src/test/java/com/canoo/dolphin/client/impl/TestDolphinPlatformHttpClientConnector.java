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
package com.canoo.dolphin.client.impl;

import com.canoo.dp.impl.client.DolphinPlatformHttpClientConnector;
import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.DefaultModelSynchronizer;
import com.canoo.dp.impl.client.legacy.communication.SimpleExceptionHandler;
import com.canoo.dp.impl.platform.client.http.HttpClientImpl;
import com.canoo.dp.impl.platform.core.http.HttpStatus;
import com.canoo.dp.impl.platform.core.PlatformConstants;
import com.canoo.dp.impl.remoting.commands.CreateContextCommand;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.remoting.legacy.communication.CreatePresentationModelCommand;
import com.canoo.dp.impl.remoting.legacy.communication.JsonCodec;
import com.canoo.platform.client.HeadlessToolkit;
import com.canoo.platform.client.PlatformClient;
import com.canoo.platform.core.http.HttpClient;
import com.canoo.platform.core.http.HttpURLConnectionFactory;
import com.canoo.platform.remoting.DolphinRemotingException;
import com.google.gson.Gson;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDolphinPlatformHttpClientConnector {

    @Test
    public void testSimpleCall() throws DolphinRemotingException, URISyntaxException {
        PlatformClient.init(new HeadlessToolkit());
        PlatformClient.getClientConfiguration().setHttpURLConnectionFactory(new HttpURLConnectionFactory() {
            @Override
            public HttpURLConnection create(URI url) throws IOException {
                return new HttpURLConnection(url.toURL()) {
                    @Override
                    public void disconnect() {

                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {

                    }

                    @Override
                    public int getResponseCode() throws IOException {
                        return HttpStatus.HTTP_OK;
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        return new ByteArrayOutputStream();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        String response = "[{\"pmId\":\"p1\",\"clientSideOnly\":false,\"id\":\"CreatePresentationModel\",\"attributes\":[],\"pmType\":null,\"className\":\"com.canoo.dp.impl.remoting.legacy.communication.CreatePresentationModelCommand\"}]";
                        return new ByteArrayInputStream(response.getBytes("UTF-8"));
                    }

                    @Override
                    public String getHeaderField(String name) {
                        if (PlatformConstants.CLIENT_ID_HTTP_HEADER_NAME.equals(name)) {
                            return "TEST-ID";
                        }
                        return super.getHeaderField(name);
                    }
                };
            }
        });

        final ClientModelStore clientModelStore = new ClientModelStore(new DefaultModelSynchronizer(() -> null));
        final DolphinPlatformHttpClientConnector connector = new DolphinPlatformHttpClientConnector(getDummyURL(), PlatformClient.getClientConfiguration(), clientModelStore, new JsonCodec(), new SimpleExceptionHandler(), PlatformClient.getService(HttpClient.class));

        final CreatePresentationModelCommand command = new CreatePresentationModelCommand();
        command.setPmId("p1");
        Command rawCommand = command;
        final List<Command> result = connector.transmit(Collections.singletonList(rawCommand));

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0) instanceof CreatePresentationModelCommand);
        Assert.assertEquals(((CreatePresentationModelCommand) result.get(0)).getPmId(), "p1");
    }

    @Test(expectedExceptions = DolphinRemotingException.class)
    public void testBadResponse() throws DolphinRemotingException, URISyntaxException {
        PlatformClient.init(new HeadlessToolkit());
        PlatformClient.getClientConfiguration().setHttpURLConnectionFactory(new HttpURLConnectionFactory() {
            @Override
            public HttpURLConnection create(URI url) throws IOException {
                return new HttpURLConnection(url.toURL()) {
                    @Override
                    public void disconnect() {

                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {

                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        return new ByteArrayOutputStream();
                    }

                };
            }
        });

        final ClientModelStore clientModelStore = new ClientModelStore(new DefaultModelSynchronizer(() -> null));

        final DolphinPlatformHttpClientConnector connector = new DolphinPlatformHttpClientConnector(getDummyURL(), PlatformClient.getClientConfiguration(), clientModelStore, new JsonCodec(), new SimpleExceptionHandler(), new HttpClientImpl(new Gson(), PlatformClient.getClientConfiguration()));

        final List<Command> commands = new ArrayList<>();
        commands.add(new CreateContextCommand());
        connector.transmit(commands);
    }

    private URI getDummyURL(){
        try {
            return new URI("http://dummyURL");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Exception occurred while creating URL", e);
        }
    }
}
