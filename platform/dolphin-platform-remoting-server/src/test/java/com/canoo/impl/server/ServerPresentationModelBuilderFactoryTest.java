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

import com.canoo.dp.impl.remoting.PresentationModelBuilder;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import com.canoo.dp.impl.server.legacy.ServerPresentationModel;
import com.canoo.dp.impl.server.model.ServerPresentationModelBuilderFactory;
import com.canoo.impl.server.util.AbstractDolphinBasedTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class ServerPresentationModelBuilderFactoryTest extends AbstractDolphinBasedTest {

    @Test
    public void testSimpleCreation() {
        ServerModelStore serverModelStore = createServerModelStore();
        ServerPresentationModelBuilderFactory factory = new ServerPresentationModelBuilderFactory(serverModelStore);
        PresentationModelBuilder<ServerPresentationModel> builder = factory.createBuilder();
        assertNotNull(builder);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullArgument() {
        new ServerPresentationModelBuilderFactory(null);
    }
}
