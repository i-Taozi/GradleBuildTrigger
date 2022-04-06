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
package com.canoo.dp.impl.client.javafx;

import com.canoo.platform.core.functional.Binding;
import com.canoo.platform.remoting.client.javafx.Converter;
import com.canoo.platform.remoting.client.javafx.binding.JavaFXBinder;
import com.canoo.platform.core.functional.Subscription;
import com.canoo.platform.remoting.Property;
import com.canoo.dp.impl.platform.core.Assert;
import javafx.beans.value.WritableValue;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class DefaultJavaFXBinder<S> implements JavaFXBinder<S> {

    private final WritableValue<S> javaFxValue;

    public DefaultJavaFXBinder(final WritableValue<S> javaFxValue) {
        this.javaFxValue = Assert.requireNonNull(javaFxValue, "javaFxValue");
    }

    @Override
    public <T> Binding to(final Property<T> dolphinProperty, final Converter<? super T, ? extends S> converter) {
        Assert.requireNonNull(dolphinProperty, "dolphinProperty");
        Assert.requireNonNull(converter, "converter");
        final Subscription subscription = dolphinProperty.onChanged(event -> javaFxValue.setValue(converter.convert(dolphinProperty.get())));
        javaFxValue.setValue(converter.convert(dolphinProperty.get()));
        return () -> subscription.unsubscribe();
    }
}
