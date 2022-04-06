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
package com.canoo.dp.impl.platform.projector.lazy;

import com.canoo.dp.impl.platform.projector.media.MediaBean;
import com.canoo.platform.remoting.Property;
import com.canoo.platform.remoting.RemotingBean;

@RemotingBean
public class LazyMediaBean extends MediaBean implements LazyListElement {

    private Property<Integer> index;

    @Override
    public Property<Integer> indexProperty() {
        return index;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
