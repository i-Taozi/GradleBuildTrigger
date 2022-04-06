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
package com.canoo.dolphin.test.eventbus;

import com.canoo.platform.remoting.server.RemotingAction;
import com.canoo.platform.remoting.server.RemotingController;
import com.canoo.platform.remoting.server.RemotingModel;
import com.canoo.platform.remoting.server.event.RemotingEventBus;
import org.springframework.beans.factory.annotation.Autowired;

@RemotingController(EventBusTestConstants.EVENT_BUS_PUBLISHER_CONTROLLER_NAME)
public class EventBusPublisherController {

    @RemotingModel
    private EventBusTestModel model;

    @Autowired
    private RemotingEventBus eventBus;

    @RemotingAction(EventBusTestConstants.CALL_ACTION)
    public void call() {
        eventBus.publish(EventBusTestConstants.TEST_TOPIC, model.valueProperty().get());
    }
}