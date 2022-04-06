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
package com.canoo.dp.impl.server.context;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.dp.impl.server.config.RemotingConfiguration;
import com.canoo.dp.impl.server.controller.ControllerRepository;
import com.canoo.dp.impl.server.controller.ControllerValidationException;
import com.canoo.platform.server.client.ClientSession;
import com.canoo.platform.server.spi.components.ClasspathScanner;
import com.canoo.platform.server.spi.components.ManagedBeanFactory;
import org.apiguardian.api.API;

import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class DefaultDolphinContextFactory implements DolphinContextFactory {

    private final RemotingConfiguration configuration;

    private final ControllerRepository controllerRepository;

    private final ManagedBeanFactory beanFactory;

    private final ClientSessionProvider sessionProvider;

    public DefaultDolphinContextFactory(final RemotingConfiguration configuration, ClientSessionProvider sessionProvider, final ManagedBeanFactory beanFactory, final ClasspathScanner scanner)
    throws ControllerValidationException {
        this.configuration = Assert.requireNonNull(configuration, "configuration");
        this.sessionProvider = Assert.requireNonNull(sessionProvider, "sessionProvider");
        this.beanFactory = Assert.requireNonNull(beanFactory, "beanFactory");
        this.controllerRepository = new ControllerRepository(scanner);
    }

    @Override
    public DolphinContext create(final ClientSession clientSession, final Consumer<DolphinContext> onDestroyCallback) {
        Assert.requireNonNull(clientSession, "clientSession");
        return new DolphinContext(configuration, clientSession, sessionProvider, beanFactory, controllerRepository, onDestroyCallback);
    }
}
