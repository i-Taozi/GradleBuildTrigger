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
package com.canoo.impl.dp.spring.test;

import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.client.legacy.communication.AbstractClientConnector;
import com.canoo.dp.impl.platform.client.session.ClientSessionStoreImpl;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.dp.impl.server.client.HttpClientSessionImpl;
import com.canoo.dp.impl.server.config.ConfigurationFileLoader;
import com.canoo.dp.impl.server.config.ServerConfiguration;
import com.canoo.dp.impl.server.config.RemotingConfiguration;
import com.canoo.dp.impl.server.context.DolphinContext;
import com.canoo.dp.impl.server.controller.ControllerRepository;
import com.canoo.dp.impl.server.scanner.DefaultClasspathScanner;
import com.canoo.platform.client.PlatformClient;
import com.canoo.platform.server.client.ClientSession;
import org.apiguardian.api.API;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.canoo.dp.impl.server.bootstrap.BasicConfigurationProvider.ROOT_PACKAGE_FOR_CLASSPATH_SCAN;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class TestConfiguration {

    private final DolphinContext dolphinTestContext;

    private final TestClientContext clientContext;

    public TestConfiguration(final WebApplicationContext context, final HttpSession httpSession) throws Exception {
        Assert.requireNonNull(context, "context");
        Assert.requireNonNull(httpSession, "httpSession");

        final ServerConfiguration defaultPlatformConfiguration = ConfigurationFileLoader.loadConfiguration();
        final RemotingConfiguration remotingConfiguration = new RemotingConfiguration(defaultPlatformConfiguration);

        //PlatformClient
        final ExecutorService clientExecutor = Executors.newSingleThreadExecutor();

        final ClientSessionStoreImpl clientSessionStore = new ClientSessionStoreImpl();
        final Function<ClientModelStore, AbstractClientConnector> connectorProvider = s -> new DolphinTestClientConnector(s, clientExecutor, c -> sendToServer(c));
        clientContext = new TestClientContextImpl(PlatformClient.getClientConfiguration(), new URI("http://dummy"), connectorProvider, clientSessionStore);

        //Server
        final ControllerRepository controllerRepository = new ControllerRepository(new DefaultClasspathScanner(defaultPlatformConfiguration.getListProperty(ROOT_PACKAGE_FOR_CLASSPATH_SCAN)));
        final TestSpringManagedBeanFactory containerManager = new TestSpringManagedBeanFactory(context);
        containerManager.init(context.getServletContext());
        final DolphinContextProviderMock dolphinContextProviderMock = new DolphinContextProviderMock();


        dolphinTestContext = new TestDolphinContext(remotingConfiguration, new HttpClientSessionImpl(httpSession), dolphinContextProviderMock, containerManager, controllerRepository, createEmptyCallback());

        dolphinContextProviderMock.setCurrentContext(dolphinTestContext);
    }

    private Consumer<DolphinContext> createEmptyCallback() {
        return (c) -> {
        };
    }

    private List<Command> sendToServer(final List<Command> commandList) {
        return dolphinTestContext.handle(commandList);
    }

    public DolphinContext getDolphinTestContext() {
        return dolphinTestContext;
    }

    private class DolphinContextProviderMock implements ClientSessionProvider {

        private DolphinContext currentContext;

        public void setCurrentContext(final DolphinContext currentContext) {
            this.currentContext = currentContext;
        }

        @Override
        public ClientSession getCurrentClientSession() {
            return currentContext.getClientSession();
        }
    }

    public TestClientContext getClientContext() {
        return clientContext;
    }
}
