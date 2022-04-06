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
package com.canoo.dolphin.legacy;

import com.canoo.dp.impl.remoting.legacy.core.ModelStoreEvent;
import com.canoo.dp.impl.remoting.legacy.core.ModelStoreListener;
import com.canoo.dp.impl.remoting.legacy.core.ModelStoreListenerWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ModelStoreListenerWrapperTest {
    @Test
    public void testEquals() {

        //given:
        ModelStoreListener listener = new ModelStoreListener() {
            @Override
            public void modelStoreChanged(ModelStoreEvent event) {

            }

        };
        ModelStoreListenerWrapper wrapper = new ModelStoreListenerWrapper("no-type", listener);

        //then:
        Assert.assertEquals(new ModelStoreListenerWrapper("no-type", listener), wrapper);
        Assert.assertNotEquals(new ModelStoreListenerWrapper("other-type", listener), wrapper);
    }

}
