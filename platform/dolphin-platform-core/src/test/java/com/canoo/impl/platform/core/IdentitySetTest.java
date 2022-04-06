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
package com.canoo.impl.platform.core;

import com.canoo.dp.impl.platform.core.IdentitySet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class IdentitySetTest {

    @Test
    public void testWithDate() {
        //given:
        final IdentitySet<Date> set = new IdentitySet<>();

        final long time = System.currentTimeMillis();
        final Date date1 = new Date(time);
        final Date date2 = new Date(time);
        final Date date3 = new Date(time + 1);

        //when:
        set.add(date1);
        set.add(date2);
        set.add(date3);

        //then:
        Assert.assertEquals(set.size(), 3);
    }

}
