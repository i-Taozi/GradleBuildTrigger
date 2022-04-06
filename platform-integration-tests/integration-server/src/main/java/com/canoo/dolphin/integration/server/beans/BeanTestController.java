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
package com.canoo.dolphin.integration.server.beans;

import com.canoo.dolphin.integration.bean.BeanTestBean;
import com.canoo.dolphin.integration.bean.BeanTestConstants;
import com.canoo.platform.remoting.BeanManager;
import com.canoo.platform.remoting.server.RemotingController;
import com.canoo.platform.remoting.server.RemotingModel;
import com.canoo.platform.remoting.server.RemotingContext;
import com.canoo.platform.remoting.server.binding.PropertyBinder;
import com.canoo.platform.remoting.server.event.RemotingEventBus;
import com.canoo.platform.server.client.ClientSession;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@RemotingController(BeanTestConstants.BEAN_CONTROLLER_NAME)
public class BeanTestController {

    @RemotingModel
    private BeanTestBean model;

    @Inject
    private ClientSession clientSession;

    @Inject
    private RemotingContext remotingContext;

    @Inject
    private BeanManager beanManager;

    @Inject
    private RemotingEventBus dolphinEventBus;

    @Inject
    private PropertyBinder propertyBinder;

    @PostConstruct
    public void init() {
        model.setClientSessionInjected(clientSession != null);
        model.setRemotingContextInjected(remotingContext != null);
        model.setBeanManagerInjected(beanManager != null);
        model.setDolphinEventBusInjected(dolphinEventBus != null);
        model.setPropertyBinderInjected(propertyBinder != null);

    }
}
