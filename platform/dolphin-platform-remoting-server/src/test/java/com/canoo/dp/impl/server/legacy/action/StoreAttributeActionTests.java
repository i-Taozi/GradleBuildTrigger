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
package com.canoo.dp.impl.server.legacy.action;

import com.canoo.dp.impl.remoting.legacy.communication.ChangeAttributeMetadataCommand;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.legacy.ServerAttribute;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import com.canoo.dp.impl.server.legacy.ServerPresentationModel;
import com.canoo.dp.impl.server.legacy.communication.ActionRegistry;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

public class StoreAttributeActionTests {

    @BeforeMethod
    public void setUp() throws Exception {
        serverModelStore = new ServerModelStore();
        serverModelStore.setCurrentResponse(new ArrayList<Command>());
        registry = new ActionRegistry();
    }

    @Test
    public void testChangeAttributeMetadata() {
        StoreAttributeAction action = new StoreAttributeAction();
        action.setServerModelStore(serverModelStore);
        action.registerIn(registry);
        ServerAttribute attribute = new ServerAttribute("newAttribute", "");
        serverModelStore.add(new ServerPresentationModel("model", Collections.singletonList(attribute), serverModelStore));
        registry.getActionsFor(ChangeAttributeMetadataCommand.class).get(0).handleCommand(new ChangeAttributeMetadataCommand(attribute.getId(), "value", "newValue"), Collections.emptyList());
        Assert.assertEquals("newValue", attribute.getValue());
    }

    private ServerModelStore serverModelStore;
    private ActionRegistry registry;
}
