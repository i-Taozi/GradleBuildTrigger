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
package com.canoo.dolphin.impl;

import com.canoo.dp.impl.remoting.collections.ObservableArrayList;
import com.canoo.dp.impl.remoting.DolphinUtils;
import com.canoo.dp.impl.remoting.MockedProperty;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class DolphinUtilsTest {

    @Test
    public void testIsAllowedForUnmanaged() {
        //Basics
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Double.class));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Double.TYPE));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Long.class));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Long.TYPE));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Float.class));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Float.TYPE));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Integer.class));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Integer.TYPE));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Boolean.class));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(Boolean.TYPE));
        assertTrue(DolphinUtils.isAllowedForUnmanaged(String.class));

        //Enum
        assertTrue(DolphinUtils.isAllowedForUnmanaged(RetentionPolicy.class));

        //Property
        assertTrue(DolphinUtils.isAllowedForUnmanaged(MockedProperty.class));

        //Other
        assertFalse(DolphinUtils.isAllowedForUnmanaged(Date.class));
        assertFalse(DolphinUtils.isAllowedForUnmanaged(LocalDateTime.class));
        assertFalse(DolphinUtils.isAllowedForUnmanaged(Locale.class));

        try {
            DolphinUtils.isAllowedForUnmanaged(null);
            Assert.fail("Null check not working");
        } catch (Exception e) {

        }
    }

    @Test
    public void testIsEnumType() throws Exception {
        assertTrue(DolphinUtils.isEnumType(DataType.class));

        try {
            DolphinUtils.isEnumType(null);
            Assert.fail("Null check not working");
        } catch (Exception e) {

        }
    }

    @Test
    public void testIsProperty() throws Exception {
        assertTrue(DolphinUtils.isProperty(MockedProperty.class));

        try {
            DolphinUtils.isProperty((Class<?>) null);
            Assert.fail("Null check not working");
        } catch (Exception e) {

        }
    }

    @Test
    public void testBasicType() throws Exception {
        assertTrue(DolphinUtils.isBasicType(String.class));
        assertTrue(DolphinUtils.isBasicType(Number.class));
        assertTrue(DolphinUtils.isBasicType(Long.class));
        assertTrue(DolphinUtils.isBasicType(Integer.class));
        assertTrue(DolphinUtils.isBasicType(Double.class));
        assertTrue(DolphinUtils.isBasicType(Boolean.class));
        assertTrue(DolphinUtils.isBasicType(Byte.class));
        assertTrue(DolphinUtils.isBasicType(Short.class));
        assertTrue(DolphinUtils.isBasicType(BigDecimal.class));
        assertTrue(DolphinUtils.isBasicType(BigInteger.class));
        assertTrue(DolphinUtils.isBasicType(Long.TYPE));
        assertTrue(DolphinUtils.isBasicType(Integer.TYPE));
        assertTrue(DolphinUtils.isBasicType(Double.TYPE));
        assertTrue(DolphinUtils.isBasicType(Boolean.TYPE));
        assertTrue(DolphinUtils.isBasicType(Byte.TYPE));
        assertTrue(DolphinUtils.isBasicType(Short.TYPE));

        assertFalse(DolphinUtils.isBasicType(DolphinUtilsTest.class));
        assertFalse(DolphinUtils.isBasicType(DataType.class));
        assertFalse(DolphinUtils.isBasicType(UUID.class));

        try {
            DolphinUtils.isBasicType(null);
            Assert.fail("Null check not working");
        } catch (Exception e) {

        }
    }

    @Test
    public void testIsObservableList() {
        assertTrue(DolphinUtils.isObservableList(ObservableArrayList.class));
        assertFalse(DolphinUtils.isObservableList(LinkedList.class));

        try {
            DolphinUtils.isObservableList(null);
            Assert.fail("Null check not working");
        } catch (Exception e) {

        }
    }
}
