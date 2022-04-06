/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gregwhitaker.catnap.core.util;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities that deal with reflection junk.
 */
public class ClassUtil {

    private ClassUtil() {
        //Hiding default constructor
    }

    /**
     * @param startClass
     * @param exclusiveParent
     * @return
     */
    public static Iterable<Field> getFieldsUpTo(Class<?> startClass, Class<?> exclusiveParent) {
        List<Field> currentClassFields = new ArrayList<Field>(Arrays.asList(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    /**
     * @param clazz
     * @return
     */
    public static boolean isPrimitiveType(Class<?> clazz) {
        if (clazz != null) {
            return clazz.isPrimitive() ||
                    clazz.isEnum() ||
                    Number.class.isAssignableFrom(clazz) ||
                    String.class.isAssignableFrom(clazz) ||
                    Boolean.class.isAssignableFrom(clazz) ||
                    Character.class.isAssignableFrom(clazz) ||
                    Date.class.isAssignableFrom(clazz);
        }

        return false;
    }

    /**
     * @param clazz
     * @return
     */
    public static boolean isArraysArrayList(Class<?> clazz) {
        try {
            Class<?> arraysClazz = Class.forName("java.util.Arrays$ArrayList");
            return arraysClazz.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * @param instanceType
     * @return
     */
    public static List<PropertyDescriptor> getReadableProperties(Class<?> instanceType) {
        List<PropertyDescriptor> result = new ArrayList<PropertyDescriptor>();
        for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(instanceType)) {
            if (descriptor.getReadMethod() != null && !descriptor.getName().equals("class")) {
                result.add(descriptor);
            }
        }

        return result;
    }

    /**
     * @param instanceType
     * @return
     */
    public static Map<String, PropertyDescriptor> getReadablePropertiesAsMap(Class<?> instanceType) {
        Map<String, PropertyDescriptor> result = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(instanceType)) {
            if (descriptor.getReadMethod() != null && !descriptor.getName().equals("class")) {
                result.put(descriptor.getName(), descriptor);
            }
        }

        return result;
    }

    /**
     * @param name
     * @param instanceType
     * @return
     */
    public static PropertyDescriptor getReadableProperty(String name, Class<?> instanceType) {
        for (PropertyDescriptor descriptor : getReadableProperties(instanceType)) {
            if (descriptor.getName().equals(name) && descriptor.getReadMethod() != null && !descriptor.getName().equals("class")) {
                return descriptor;
            }
        }

        return null;
    }

    /**
     * Gets the class of a generic instance.
     *
     * @param instance instance to return the class for
     * @param <T>      type of class
     * @return class of the generic instance
     */
    public static <T> Class<T> loadClass(T instance) {
        if (instance != null) {
            return (Class<T>) instance.getClass();
        }

        return null;
    }
}
