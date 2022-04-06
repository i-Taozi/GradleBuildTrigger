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
package com.canoo.dolphin.integration.server.listener;

import com.canoo.platform.server.ServerListener;
import com.canoo.platform.server.client.ClientSession;
import com.canoo.platform.server.client.ClientSessionListener;
import com.canoo.platform.remoting.server.event.RemotingEventBus;

import javax.inject.Inject;

@ServerListener
public class TestClientSessionListener implements ClientSessionListener {

    @Inject
    private RemotingEventBus eventBus;

    public static boolean eventBusInjected = false;

    @Override
    public void sessionCreated(ClientSession clientSession) {
        eventBusInjected = (eventBus != null);
    }

    @Override
    public void sessionDestroyed(ClientSession clientSession) {

    }
}
