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
package com.canoo.platform.remoting.spi.converter;

import com.canoo.platform.remoting.RemotingBean;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * A converter interface that convert custom data types to the internally supported data types of Dolphin Platform. The Dolphin Platform supports all types that are supported by JSON:
 * - String
 * - Number
 * - Boolean
 *
 * @param <B> type of the custom data
 * @param <D> type of the internal Dolphin Platform supported data types that represents the custom data. Converter will be provided by custom implementations of the {@link ConverterFactory} interface.
 * @author Hendrik Ebbers
 * @see com.canoo.platform.remoting.Property
 * @see RemotingBean
 * @see ConverterFactory
 */
@API(since = "0.x", status = MAINTAINED)
public interface Converter<B, D> {

    /**
     * Converts the given data in a Dolphin Platform supported data type to the custom data type
     *
     * @param value the data in a Dolphin Platform supported data type
     * @return data in the custom data type
     * @throws ValueConverterException if the data can not be converted
     */
    B convertFromDolphin(D value) throws ValueConverterException;

    /**
     * Converts the given data to a Dolphin Platform supported data type
     * @param value the data in the custom data type
     * @return the data in a Dolphin Platform supported data type
     * @throws ValueConverterException if the data can not be converted
     */
    D convertToDolphin(B value) throws ValueConverterException;

}
