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

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.remoting.client.javafx.BidirectionalConverter;
import com.canoo.platform.remoting.client.javafx.Converter;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.DEPRECATED;

@Deprecated
@API(since = "0.x", status = DEPRECATED)
public class DefaultBidirectionalConverter<T, U> implements BidirectionalConverter<T, U> {

    private final Converter<T, U> converter;

    private final Converter<U, T> backConverter;

    public DefaultBidirectionalConverter(final Converter<T, U> converter, final Converter<U, T> backConverter) {
        this.converter = Assert.requireNonNull(converter, "converter");
        this.backConverter = Assert.requireNonNull(backConverter, "backConverter");
    }

    @Override
    public T convertBack(final U value) {
        return backConverter.convert(value);
    }

    @Override
    public U convert(final T value) {
        return converter.convert(value);
    }
}
