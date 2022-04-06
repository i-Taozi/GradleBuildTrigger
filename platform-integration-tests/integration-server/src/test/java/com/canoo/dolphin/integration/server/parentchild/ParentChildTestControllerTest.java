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
package com.canoo.dolphin.integration.server.parentchild;

import com.canoo.dolphin.integration.parentchild.ChildTestBean;
import com.canoo.dolphin.integration.parentchild.DummyChildTestBean;
import com.canoo.dolphin.integration.parentchild.ParentTestBean;
import com.canoo.dolphin.integration.server.TestConfiguration;
import com.canoo.platform.remoting.Property;
import com.canoo.platform.spring.test.ControllerUnderTest;
import com.canoo.platform.spring.test.SpringTestNGControllerTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.canoo.dolphin.integration.parentchild.ParentChildTestConstants.CHILD_CONTROLLER_NAME;
import static com.canoo.dolphin.integration.parentchild.ParentChildTestConstants.DUMMY_CHILD_CONTROLLER_NAME;
import static com.canoo.dolphin.integration.parentchild.ParentChildTestConstants.PARENT_CONTROLLER_NAME;

@SpringBootTest(classes = TestConfiguration.class)
public class ParentChildTestControllerTest extends SpringTestNGControllerTest {

    private ControllerUnderTest<ParentTestBean> controller;

    @BeforeMethod
    public void init() {
        controller = createController(PARENT_CONTROLLER_NAME);
    }

    public void destroy() {
        controller.destroy();
    }

    @Test
    public void testPostChildCreatedCalledWhenChildControllerCreated() {
        Property<Boolean> postChildCreatedProperty = controller.getModel().postChildCreatedCalledProperty();
        Assert.assertNull(postChildCreatedProperty.get());
        controller.createController(CHILD_CONTROLLER_NAME);
        Assert.assertTrue(postChildCreatedProperty.get());
        destroy();
    }

    @Test
    public void testPostChildCreatedNotCalledWhenNonChildControllerCreated() {
        Property<Boolean> postChildCreatedProperty = controller.getModel().postChildCreatedCalledProperty();
        Assert.assertNull(postChildCreatedProperty.get());
        controller.createController(DUMMY_CHILD_CONTROLLER_NAME);
        Assert.assertNull(postChildCreatedProperty.get());
        destroy();
    }

    @Test
    public void testPreChildDestroyedCalledWhenChildControllerDestroyed() {
        controller.createController(CHILD_CONTROLLER_NAME);
        Property<Boolean> preDestroyCalledProperty = controller.getModel().preChildDestroyedCalledProperty();
        Assert.assertNull(preDestroyCalledProperty.get());
        destroy();
        Assert.assertTrue(preDestroyCalledProperty.get());
    }

    @Test
    public void testPreChildDestroyedNotCalledWhenNonChildControllerDestroyed() {
        ControllerUnderTest<DummyChildTestBean>  dummyController = controller.createController(DUMMY_CHILD_CONTROLLER_NAME);
        Property<Boolean> preDestroyCalledProperty = controller.getModel().preChildDestroyedCalledProperty();
        Assert.assertNull(preDestroyCalledProperty.get());
        dummyController.destroy();
        Assert.assertNull(preDestroyCalledProperty.get());
    }

    @Test
    public void testPreDestroyInChildIsCalledWhenParentIsDestroyed(){
        ControllerUnderTest<ChildTestBean>  childController = controller.createController(CHILD_CONTROLLER_NAME);
        Property<Boolean> preDestroyCalledProperty = childController.getModel().preDestroyedCalledProperty();
        Assert.assertNull(preDestroyCalledProperty.get());
        destroy();
        Assert.assertTrue(preDestroyCalledProperty.get());
    }

    @Test
    public void testPreDestroyInParentIsCalledFirstWhenParentIsDestroyed(){
        ControllerUnderTest<ChildTestBean>  childController = controller.createController(CHILD_CONTROLLER_NAME);
        Property<Boolean> childPreDestroyCalledProperty = childController.getModel().preDestroyedCalledProperty();
        Property<Boolean> parentPreDestroyCalledProperty = controller.getModel().preDestroyedCalledProperty();
        Assert.assertNull(parentPreDestroyCalledProperty.get());
        Assert.assertNull(childPreDestroyCalledProperty.get());
        destroy();
        Assert.assertTrue(parentPreDestroyCalledProperty.get());
        Assert.assertTrue(childPreDestroyCalledProperty.get());
    }
}
