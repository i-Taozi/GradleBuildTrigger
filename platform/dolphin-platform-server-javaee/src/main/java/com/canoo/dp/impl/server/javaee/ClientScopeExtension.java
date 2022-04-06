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
import com.canoo.platform.server.javaee.ClientScoped;
import org.apiguardian.api.API;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import java.io.Serializable;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ClientScopeExtension implements Extension, Serializable {

    public void addScope(@Observes final BeforeBeanDiscovery event) {
        Assert.requireNonNull(event, "event");
        event.addScope(ClientScoped.class, true, false);
    }

    public void registerContext(@Observes final AfterBeanDiscovery event, final BeanManager beanManager) {
        Assert.requireNonNull(event, "event");
        event.addContext(new ClientScopeContext(beanManager));
    }
}