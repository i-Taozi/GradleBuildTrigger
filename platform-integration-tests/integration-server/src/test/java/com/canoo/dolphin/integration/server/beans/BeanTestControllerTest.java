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
import com.canoo.dolphin.integration.server.TestConfiguration;
import com.canoo.platform.spring.test.ControllerUnderTest;
import com.canoo.platform.spring.test.SpringTestNGControllerTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.canoo.dolphin.integration.bean.BeanTestConstants.BEAN_CONTROLLER_NAME;

@SpringBootTest(classes = TestConfiguration.class)
public class BeanTestControllerTest extends SpringTestNGControllerTest {

    private ControllerUnderTest<BeanTestBean> controller;

    @BeforeMethod
    public void init() {
        controller = createController(BEAN_CONTROLLER_NAME);
    }

    public void destroy() {
        controller.destroy();
    }

    @Test
    public void testInjection() {
        Assert.assertTrue(controller.getModel().getBeanManagerInjected());
        Assert.assertTrue(controller.getModel().getClientSessionInjected());
        Assert.assertTrue(controller.getModel().getDolphinEventBusInjected());
        Assert.assertTrue(controller.getModel().getPropertyBinderInjected());
        Assert.assertTrue(controller.getModel().getRemotingContextInjected());
        destroy();
    }
}
