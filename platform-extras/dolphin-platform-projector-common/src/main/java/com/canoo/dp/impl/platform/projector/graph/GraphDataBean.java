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
package com.canoo.dp.impl.platform.projector.graph;


import com.canoo.dp.impl.platform.projector.base.AbstractProjectableBean;
import com.canoo.dp.impl.platform.projector.base.Projectable;
import com.canoo.dp.impl.platform.projector.base.WithLayoutMetadata;
import com.canoo.dp.impl.platform.projector.base.WithTitle;
import com.canoo.dp.impl.platform.projector.metadata.KeyValue;
import com.canoo.platform.remoting.ObservableList;
import com.canoo.platform.remoting.Property;
import com.canoo.platform.remoting.RemotingBean;

@RemotingBean
public class GraphDataBean extends AbstractProjectableBean implements Projectable, WithTitle, WithLayoutMetadata {

    private ObservableList<KeyValue> layoutMetadata;

    private ObservableList<GraphDataValueBean> values;

    private Property<String> title;

    private Property<String> keyLabel;

    private Property<String> dataLabel;

    public ObservableList<KeyValue> getLayoutMetadata() {
        return layoutMetadata;
    }

    public ObservableList<GraphDataValueBean> getValues() {
        return values;
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }
}
