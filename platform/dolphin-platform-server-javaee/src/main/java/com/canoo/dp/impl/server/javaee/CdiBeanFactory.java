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
package com.canoo.dp.impl.server.javaee;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.core.context.ContextManagerImpl;
import com.canoo.dp.impl.server.bootstrap.PlatformBootstrap;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.dp.impl.server.servlet.ServerTimingFilter;
import com.canoo.platform.core.context.ContextManager;
import com.canoo.platform.server.client.ClientSession;
import com.canoo.platform.server.javaee.ClientScoped;
import com.canoo.platform.server.timing.ServerTiming;
import org.apiguardian.api.API;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Factory that provides all needed Dolphin Platform extensions as CDI beans.
 *
 * @author Hendrik Ebbers
 */
@ApplicationScoped
@API(since = "0.x", status = INTERNAL)
public class CdiBeanFactory {

    @Produces
    @ClientScoped
    public ClientSession createDolphinSession() {
        final ClientSessionProvider provider = PlatformBootstrap.getServerCoreComponents().getInstance(ClientSessionProvider.class);
        Assert.requireNonNull(provider, "provider");
        return provider.getCurrentClientSession();
    }

    @Produces
    @RequestScoped
    public ServerTiming createServerTiming() {
        return ServerTimingFilter.getCurrentTiming();
    }

    @Produces
    @ApplicationScoped
    public ContextManager createContextManager() {
        return ContextManagerImpl.getInstance();
    }
}
