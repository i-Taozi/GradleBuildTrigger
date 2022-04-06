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
package com.canoo.platform.logger.server;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.logger.model.LogHistoryBean;
import com.canoo.platform.remoting.server.PostChildCreated;
import com.canoo.platform.remoting.server.RemotingController;
import com.canoo.platform.remoting.server.RemotingModel;

@RemotingController
public class LogHistoryParentController {

    private LogFilterController filterController;

    private LogListController listController;

    @RemotingModel
    private LogHistoryBean model;

    @PostChildCreated
    public void onFilterControllerAdded(final LogFilterController controller) {
        Assert.requireNonNull(controller, "controller");
        if(filterController != null) {
            throw new IllegalStateException("Only one filter controller can be added as child!");
        }
        this.filterController = controller;
        if(listController != null) {
            filterController.addSearchListener(r -> listController.update(r));
        }
    }

    @PostChildCreated
    public void onListControllerAdded(final LogListController controller) {
        Assert.requireNonNull(controller, "controller");
        if(listController != null) {
            throw new IllegalStateException("Only one list controller can be added as child!");
        }
        this.listController = controller;
        if(filterController != null) {
            filterController.addSearchListener(r -> listController.update(r));
        }
    }

}
