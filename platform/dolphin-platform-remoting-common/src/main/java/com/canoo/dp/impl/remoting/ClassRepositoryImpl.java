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
package com.canoo.dp.impl.remoting;

import com.canoo.platform.remoting.ObservableList;
import com.canoo.platform.remoting.spi.converter.Converter;
import com.canoo.dp.impl.remoting.info.ClassInfo;
import com.canoo.dp.impl.remoting.info.PropertyInfo;
import com.canoo.platform.remoting.Property;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.core.ReflectionHelper;
import com.canoo.dp.impl.remoting.legacy.core.ModelStore;
import com.canoo.dp.impl.remoting.legacy.core.ModelStoreEvent;
import com.canoo.dp.impl.remoting.legacy.core.ModelStoreListener;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A {@code ClassRepository} manages {@link ClassInfo} objects for all registered Dolphin Beans. A {@code ClassInfo}
 * object keeps information on class level about the properties and ObservableLists of a RemotingBean.
 */
@API(since = "0.x", status = INTERNAL)
public class ClassRepositoryImpl implements ClassRepository {

 private static final Logger LOG = LoggerFactory.getLogger(ClassRepositoryImpl.class);

    private final PresentationModelBuilderFactory builderFactory;
    private final Converters converters;

    private final Map<Class<?>, ClassInfo> classToClassInfoMap = new HashMap<>();
    private final Map<String, ClassInfo> modelTypeToClassInfoMap = new HashMap<>();

    public ClassRepositoryImpl(final ModelStore modelStore, final Converters converters, final PresentationModelBuilderFactory builderFactory) {
        this.converters = Assert.requireNonNull(converters, "converters");
        this.builderFactory = Assert.requireNonNull(builderFactory, "builderFactory");

        Assert.requireNonNull(modelStore, "modelStore").addModelStoreListener(PlatformRemotingConstants.DOLPHIN_BEAN, new ModelStoreListener() {
            @Override
            public void modelStoreChanged(final ModelStoreEvent event) {
                Assert.requireNonNull(event, "event");
                try {
                    final String className = (String) event.getPresentationModel().getAttribute(PlatformRemotingConstants.JAVA_CLASS).getValue();
                    final Class<?> beanClass = Class.forName(className);
                    final ClassInfo classInfo = createClassInfoForClass(beanClass);
                    Assert.requireNonNull(classInfo, "classInfo");
                    classToClassInfoMap.put(beanClass, classInfo);
                    modelTypeToClassInfoMap.put(classInfo.getModelType(), classInfo);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Error in class info generation!", e);
                }
            }
        });
    }

    @Override
    public ClassInfo getClassInfo(final String modelType) {
        return modelTypeToClassInfoMap.get(modelType);
    }

    @Override
    public ClassInfo getOrCreateClassInfo(final Class<?> beanClass) {
        final ClassInfo existingClassInfo = classToClassInfoMap.get(beanClass);
        if (existingClassInfo != null) {
            return existingClassInfo;
        }

        createPresentationModelForClass(beanClass);

        return classToClassInfoMap.get(beanClass);
    }

    private void createPresentationModelForClass(final Class<?> beanClass) {
        Assert.requireNonNull(beanClass, "beanClass");
        final String id = DolphinUtils.getDolphinPresentationModelTypeForClass(beanClass);
        final PresentationModelBuilder builder = builderFactory.createBuilder()
                .withId(id)
                .withType(PlatformRemotingConstants.DOLPHIN_BEAN)
                .withAttribute(PlatformRemotingConstants.JAVA_CLASS, beanClass.getName());

        for (final Field field : ReflectionHelper.getInheritedDeclaredFields(beanClass)) {
            if (Property.class.isAssignableFrom(field.getType()) || ObservableList.class.isAssignableFrom(field.getType())) {
                final String attributeName = DolphinUtils.getDolphinAttributePropertyNameForField(field);
                final Class<?> clazz = ReflectionHelper.getTypeParameter(field);
                if(clazz == null) {
                    throw new MappingException("Can't define generic type for field " + attributeName + " in bean " + beanClass);
                }
                final int type = converters.getFieldType(clazz);
                builder.withAttribute(attributeName, type);
            }
        }

        builder.create();
    }

    private ClassInfo createClassInfoForClass(final Class<?> beanClass) {
        final List<PropertyInfo> propertyInfos = new ArrayList<>();
        final List<PropertyInfo> observableListInfos = new ArrayList<>();

        for (Field field : ReflectionHelper.getInheritedDeclaredFields(beanClass)) {
            PropertyType type = null;
            if (Property.class.isAssignableFrom(field.getType())) {
                type = PropertyType.PROPERTY;
            } else if (ObservableList.class.isAssignableFrom(field.getType())) {
                type = PropertyType.OBSERVABLE_LIST;
            }
            final Class<?> parameterType = ReflectionHelper.getTypeParameter(field);

            if (type != null && parameterType != null) {
                final String attributeName = DolphinUtils.getDolphinAttributePropertyNameForField(field);
                final Converter converter = converters.getConverter(parameterType);
                final PropertyInfo propertyInfo = new ClassPropertyInfo(attributeName, converter, field);
                if (type == PropertyType.PROPERTY) {
                    propertyInfos.add(propertyInfo);
                } else {
                    observableListInfos.add(propertyInfo);
                }
            }
        }

        return new ClassInfo(beanClass, propertyInfos, observableListInfos);
    }

    private enum PropertyType {PROPERTY, OBSERVABLE_LIST}
}
