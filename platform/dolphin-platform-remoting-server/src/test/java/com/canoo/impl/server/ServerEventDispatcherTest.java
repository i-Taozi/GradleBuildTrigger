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
package com.canoo.impl.server;

import com.canoo.dp.impl.remoting.legacy.RemotingConstants;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import com.canoo.dp.impl.server.model.ServerEventDispatcher;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ServerEventDispatcherTest {

    @Test
    public void testLocalSystemIdentifier() {
        //given:
        ServerModelStore serverModelStore = new ServerModelStore();
        ServerEventDispatcher dispatcher = new ServerEventDispatcher(serverModelStore);

        //then:
        assertEquals(dispatcher.getLocalSystemIdentifier(), RemotingConstants.SOURCE_SYSTEM_SERVER);
    }
}
