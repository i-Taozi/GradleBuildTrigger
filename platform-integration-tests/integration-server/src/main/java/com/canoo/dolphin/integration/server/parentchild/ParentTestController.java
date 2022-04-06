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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.canoo.dolphin.integration.parentchild.ParentTestBean;
import com.canoo.platform.remoting.server.PostChildCreated;
import com.canoo.platform.remoting.server.PreChildDestroyed;
import com.canoo.platform.remoting.server.RemotingController;
import com.canoo.platform.remoting.server.RemotingModel;

import static com.canoo.dolphin.integration.parentchild.ParentChildTestConstants.PARENT_CONTROLLER_NAME;

@RemotingController(PARENT_CONTROLLER_NAME)
public class ParentTestController {

    @RemotingModel
    private ParentTestBean model;


    @PostConstruct
    private void init() {
        model.setPostCreatedCalled(true);
    }

    @PostChildCreated
    private void onChildControllerCreated(final ChildTestController childController) {
        model.setPostChildCreatedCalled(true);
    }

    @PreChildDestroyed
    private void onChildControllerDestroyed(final ChildTestController childController) {
        model.setPreChildDestroyedCalled(true);
    }

    @PreDestroy
    private void destroy() {
        model.setPreDestroyedCalled(true);
    }
}