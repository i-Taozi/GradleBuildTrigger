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
package com.canoo.dp.impl.platform.projector.client.projection;

import com.canoo.dp.impl.platform.projector.client.lazy.LazyLoadingBehavior;
import com.canoo.dp.impl.platform.projector.client.media.SimpleMediaListCell;
import com.canoo.dp.impl.platform.projector.lazy.LazyMediaBean;
import com.canoo.dp.impl.platform.projector.lazy.concrete.MediaLazyListBean;
import com.canoo.platform.remoting.client.ClientContext;
import com.canoo.platform.remoting.client.ControllerProxy;
import javafx.scene.Parent;
import javafx.scene.control.ListView;

public class LazyListFactory implements ProjectionFactory<MediaLazyListBean> {

    @Override
    public Parent createProjection(Projector projector, ClientContext clientContext, ControllerProxy controllerProxy, MediaLazyListBean projectable) {
        ListView<LazyMediaBean> listView = new ListView<>();
        listView.setCellFactory(SimpleMediaListCell.createDefaultCallback());
        LazyLoadingBehavior<LazyMediaBean> lazyLoadingBehavior = new LazyLoadingBehavior<>(listView);
        lazyLoadingBehavior.setModel(projectable);
        return listView;
    }
}
