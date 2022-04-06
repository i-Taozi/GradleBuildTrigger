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

import com.canoo.platform.remoting.BeanManager;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.server.binding.PropertyBinderImpl;
import com.canoo.platform.remoting.server.ClientSessionExecutor;
import com.canoo.platform.remoting.server.RemotingContext;
import com.canoo.platform.remoting.server.binding.PropertyBinder;
import com.canoo.platform.remoting.server.event.RemotingEventBus;
import com.canoo.platform.server.client.ClientSession;
import org.apiguardian.api.API;

import java.util.concurrent.Executor;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class RemotingContextImpl implements RemotingContext {

    private final DolphinContext dolphinContext;

    private final RemotingEventBus eventBus;

    private final PropertyBinder propertyBinder = new PropertyBinderImpl();

    private final ClientSessionExecutor clientSessionExecutor;

    public RemotingContextImpl(final DolphinContext dolphinContext, RemotingEventBus eventBus) {
        this.dolphinContext = Assert.requireNonNull(dolphinContext, "dolphinContext");
        this.eventBus = Assert.requireNonNull(eventBus, "eventBus");
        clientSessionExecutor = new ClientSessionExecutorImpl(new Executor() {
            @Override
            public void execute(Runnable command) {
                dolphinContext.runLater(command);
            }
        });
    }

    @Override
    public String getId() {
        return dolphinContext.getId();
    }

    @Override
    public ClientSessionExecutor createSessionExecutor() {
        return clientSessionExecutor;
    }

    @Override
    public PropertyBinder getBinder() {
        return propertyBinder;
    }

    @Override
    public BeanManager getBeanManager() {
        return dolphinContext.getBeanManager();
    }

    @Override
    public RemotingEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public ClientSession getClientSession() {
        return dolphinContext.getClientSession();
    }

    @Override
    public boolean isActive() {
        return dolphinContext.isActive();
    }
}
