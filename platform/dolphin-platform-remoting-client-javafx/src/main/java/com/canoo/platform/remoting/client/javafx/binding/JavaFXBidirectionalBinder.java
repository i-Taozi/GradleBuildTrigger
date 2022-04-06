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
package com.canoo.platform.remoting.client.javafx.binding;

import com.canoo.platform.core.functional.Binding;
import com.canoo.platform.remoting.client.javafx.BidirectionalConverter;
import com.canoo.platform.remoting.client.javafx.FXBinder;
import com.canoo.platform.remoting.Property;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * This class can be used to create a unidirectional or bidirectional binding between a JavaFX property and a
 * Dolphin Platform property. Normally a developer don't need to create new instances of this class since it's part of a
 * fluent API. To create bindings see {@link FXBinder}
 * @param <S> value type for the properties
 */
@API(since = "0.x", status = MAINTAINED)
public interface JavaFXBidirectionalBinder<S> extends JavaFXBinder<S> {

    /**
     * Bind the given JavaFX property bidirectional to the Dolphin Platform property
     * @param dolphinProperty the Dolphin Platform property
     * @return the binding
     */
    default Binding bidirectionalTo(Property<S> dolphinProperty) {
        return bidirectionalTo(dolphinProperty, new BidirectionalConverter<S, S>() {
            @Override
            public S convertBack(S value) {
                return value;
            }

            @Override
            public S convert(S value) {
                return value;
            }
        });
    }

    /**
     * Bind the given JavaFX property bidirectional to the Dolphin Platform property
     * @param property the Dolphin Platform property
     * @param converter a converter.
     * @param <T> converted type
     * @return the binding.
     */
    <T> Binding bidirectionalTo(final Property<T> property, BidirectionalConverter<T, S> converter);

}
