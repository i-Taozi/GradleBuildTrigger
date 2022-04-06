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
package com.canoo.dolphin.integration;

import com.canoo.dolphin.integration.bean.BeanTestBean;
import com.canoo.platform.remoting.client.ClientContext;
import com.canoo.platform.remoting.client.ControllerProxy;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.canoo.dolphin.integration.bean.BeanTestConstants.BEAN_CONTROLLER_NAME;

public class BeansControllerTest extends AbstractIntegrationTest {

    @Test(dataProvider = ENDPOINTS_DATAPROVIDER, description = "Test if all bean types of Dolphin Platform can be injected in a controller")
    public void testCreateController(String containerType, String endpoint) {
        try {
            ClientContext context = connect(endpoint);
            ControllerProxy<BeanTestBean> controller = createController(context, BEAN_CONTROLLER_NAME);
            Assert.assertTrue(controller.getModel().getBeanManagerInjected());
            Assert.assertTrue(controller.getModel().getClientSessionInjected());
            Assert.assertTrue(controller.getModel().getDolphinEventBusInjected());
            Assert.assertTrue(controller.getModel().getPropertyBinderInjected());
            Assert.assertTrue(controller.getModel().getRemotingContextInjected());
            destroy(controller, endpoint);
            disconnect(context, endpoint);
        } catch (Exception e) {
            Assert.fail("Error in test for " + containerType, e);
        }
    }
}
