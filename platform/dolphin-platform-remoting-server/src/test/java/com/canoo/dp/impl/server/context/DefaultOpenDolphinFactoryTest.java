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

import com.canoo.dp.impl.remoting.codec.OptimizedJsonCodec;
import com.canoo.dp.impl.remoting.legacy.communication.ChangeAttributeMetadataCommand;
import com.canoo.dp.impl.remoting.legacy.communication.CreatePresentationModelCommand;
import com.canoo.dp.impl.remoting.legacy.communication.PresentationModelDeletedCommand;
import com.canoo.dp.impl.remoting.legacy.communication.ValueChangedCommand;
import com.canoo.dp.impl.server.legacy.DefaultServerDolphin;
import com.canoo.dp.impl.server.legacy.ServerConnector;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DefaultOpenDolphinFactoryTest {

    @Test
    public void testDolphinCreation() {

        final ServerModelStore modelStore = new ServerModelStore();
        final ServerConnector serverConnector = new ServerConnector();
        serverConnector.setCodec(OptimizedJsonCodec.getInstance());
        serverConnector.setServerModelStore(modelStore);
        final DefaultServerDolphin serverDolphin = new DefaultServerDolphin(modelStore, serverConnector);
        serverDolphin.getServerConnector().registerDefaultActions();

        assertNotNull(serverDolphin);
        assertNotNull(serverDolphin.getModelStore());
        assertNotNull(serverDolphin.getServerConnector());
        assertNotNull(serverDolphin.getModelStore());
        assertNotNull(serverDolphin.getServerConnector().getCodec());
        assertEquals(OptimizedJsonCodec.class, serverDolphin.getServerConnector().getCodec().getClass());

        assertEquals(serverDolphin.getServerConnector().getRegistry().getActions().size(), 4);
        assertTrue(serverDolphin.getServerConnector().getRegistry().getActions().containsKey(ValueChangedCommand.class));
        assertTrue(serverDolphin.getServerConnector().getRegistry().getActions().containsKey(CreatePresentationModelCommand.class));
        assertTrue(serverDolphin.getServerConnector().getRegistry().getActions().containsKey(ChangeAttributeMetadataCommand.class));
        assertTrue(serverDolphin.getServerConnector().getRegistry().getActions().containsKey(PresentationModelDeletedCommand.class));

        assertEquals(serverDolphin.getModelStore().listPresentationModelIds().size(), 0);
    }
}
