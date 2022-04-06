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
package com.canoo.dolphin.integration.parentchild;

import com.canoo.platform.remoting.Property;
import com.canoo.platform.remoting.RemotingBean;

@RemotingBean
public class DummyChildTestBean {

    private Property<Boolean> postChildCreatedCalled;

    private Property<Boolean> preChildDestroyedCalled;

    public Property<Boolean> postChildCreatedCalledProperty() {
        return postChildCreatedCalled;
    }

    public Property<Boolean> preChildDestroyedCalledProperty() {
        return preChildDestroyedCalled;
    }

    public void setPostChildCreatedCalled(final Boolean postChildCreatedCalled) {
        this.postChildCreatedCalled.set(postChildCreatedCalled);
    }

    public void setPreChildDestroyedCalled(final Boolean preChildDestroyedCalled) {
        this.preChildDestroyedCalled.set(preChildDestroyedCalled);
    }

    public Boolean getPostChildCreatedCalled() {
        return postChildCreatedCalled.get();
    }

    public Boolean getPreChildDestroyedCalled() {
        return preChildDestroyedCalled.get();
    }

}
