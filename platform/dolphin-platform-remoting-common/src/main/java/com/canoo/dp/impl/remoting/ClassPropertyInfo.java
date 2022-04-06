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

import com.canoo.platform.remoting.spi.converter.Converter;
import com.canoo.dp.impl.platform.core.ReflectionHelper;
import com.canoo.dp.impl.remoting.info.PropertyInfo;
import org.apiguardian.api.API;

import java.lang.reflect.Field;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ClassPropertyInfo extends PropertyInfo {

    private final Field field;

    public ClassPropertyInfo(final String attributeName, final Converter converter, final Field field) {
        super(attributeName, converter);
        this.field = field;
    }

    @Override
    public Object getPrivileged(final Object bean) {
        return ReflectionHelper.getPrivileged(field, bean);
    }

    @Override
    public void setPriviliged(final Object bean, final Object value) {
        ReflectionHelper.setPrivileged(field, bean, value);
    }

}
