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
package com.canoo.dp.impl.platform.projector.lazy.concrete;

import com.canoo.dp.impl.platform.projector.lazy.AbstractLazyListBean;
import com.canoo.dp.impl.platform.projector.lazy.LazyMediaBean;
import com.canoo.platform.remoting.ObservableList;
import com.canoo.platform.remoting.Property;
import com.canoo.platform.remoting.RemotingBean;

@RemotingBean
public class MediaLazyListBean extends AbstractLazyListBean<LazyMediaBean> {

    private ObservableList<LazyMediaBean> loadedContent;

    private ObservableList<LazyMediaBean> selectedValues;

    private Property<LazyMediaBean> selectedValue;

    @Override
    public ObservableList<LazyMediaBean> getLoadedContent() {
        return loadedContent;
    }

    @Override
    public ObservableList<LazyMediaBean> getSelectedValues() {
        return selectedValues;
    }

    @Override
    public Property<LazyMediaBean> selectedValueProperty() {
        return selectedValue;
    }
}
