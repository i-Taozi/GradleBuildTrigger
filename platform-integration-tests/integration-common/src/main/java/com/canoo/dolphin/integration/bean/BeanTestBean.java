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
package com.canoo.dolphin.integration.bean;

import com.canoo.platform.remoting.RemotingBean;
import com.canoo.platform.remoting.Property;

@RemotingBean
public class BeanTestBean {

    private Property<Boolean> propertyBinderInjected;

    private Property<Boolean> dolphinEventBusInjected;

    private Property<Boolean> beanManagerInjected;

    private Property<Boolean> remotingContextInjected;

    private Property<Boolean> clientSessionInjected;

    public boolean getPropertyBinderInjected() {
        return propertyBinderInjected.get();
    }

    public void setPropertyBinderInjected(boolean propertyBinderInjected) {
        this.propertyBinderInjected.set(propertyBinderInjected);
    }

    public boolean getDolphinEventBusInjected() {
        return dolphinEventBusInjected.get();
    }

    public void setDolphinEventBusInjected(boolean dolphinEventBusInjected) {
        this.dolphinEventBusInjected.set(dolphinEventBusInjected);
    }

    public boolean getBeanManagerInjected() {
        return beanManagerInjected.get();
    }

    public void setBeanManagerInjected(boolean beanManagerInjected) {
        this.beanManagerInjected.set(beanManagerInjected);
    }

    public boolean getRemotingContextInjected() {
        return remotingContextInjected.get();
    }

    public void setRemotingContextInjected(boolean remotingContextInjected) {
        this.remotingContextInjected.set(remotingContextInjected);
    }

    public boolean getClientSessionInjected() {
        return clientSessionInjected.get();
    }

    public void setClientSessionInjected(boolean clientSessionInjected) {
        this.clientSessionInjected.set(clientSessionInjected);
    }
}
