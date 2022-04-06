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
package com.canoo.dolphin.client.impl;

import com.canoo.dolphin.client.util.AbstractDolphinBasedTest;
import com.canoo.dp.impl.client.DolphinCommandHandler;
import com.canoo.dp.impl.client.legacy.ClientAttribute;
import com.canoo.dp.impl.client.legacy.ClientModelStore;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.legacy.ServerConnector;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import com.canoo.dp.impl.server.legacy.action.DolphinServerAction;
import com.canoo.dp.impl.server.legacy.communication.ActionRegistry;
import com.canoo.dp.impl.server.legacy.communication.CommandHandler;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class TestDolphinCommandHandler extends AbstractDolphinBasedTest {

    private final class TestChangeCommand extends Command {
        public TestChangeCommand() {
            super(TestChangeCommand.class.getSimpleName());
        }
    }

    @Test
    public void testInvocation() throws Exception {
        //Given:
        final DolphinTestConfiguration configuration = createDolphinTestConfiguration();
        final ServerModelStore serverModelStore = configuration.getServerModelStore();
        final ClientModelStore clientModelStore = configuration.getClientModelStore();
        final DolphinCommandHandler dolphinCommandHandler = new DolphinCommandHandler(configuration.getClientConnector());
        final String modelId = UUID.randomUUID().toString();
        clientModelStore.createModel(modelId, null, new ClientAttribute("myAttribute", "UNKNOWN"));
        configuration.getServerConnector().register(new DolphinServerAction() {
            @Override
            public void registerIn(ActionRegistry registry) {
                registry.register(TestChangeCommand.class, new CommandHandler() {
                    @Override
                    public void handleCommand(Command command, List response) {
                        serverModelStore.findPresentationModelById(modelId).getAttribute("myAttribute").setValue("Hello World");
                    }
                });
            }
        });

        //When:
        dolphinCommandHandler.invokeDolphinCommand(new TestChangeCommand()).get();

        //Then:
        assertEquals(clientModelStore.findPresentationModelById(modelId).getAttribute("myAttribute").getValue(), "Hello World");
    }

}
