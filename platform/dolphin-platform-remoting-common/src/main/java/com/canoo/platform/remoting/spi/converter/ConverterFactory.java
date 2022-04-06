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

import java.util.List;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * Entry point of the data converter SPI. Custom implementations can be provided by the default Java SPI
 * (see {@link java.util.ServiceLoader}) and will be used at runtime to convert custom data types that
 * are used in Dolphin Platform beans (see {@link RemotingBean}) to
 * internally supported data types.
 *
 * @author Hendrik Ebbers
 */
@API(since = "0.x", status = MAINTAINED)
public interface ConverterFactory {

    /**
     * Init method that will be automatically called after the converter factory instance has been created.
     * This method may be changed in future since the {@link DolphinBeanRepo} param is not needed for
     * custom extensions and should not be part of the public API.
     * not
     * @param beanRepository the internally used bean repository.
     */
    void init(DolphinBeanRepo beanRepository);

    /**
     * This method will be called to check if the converter supports the given custom data type
     * @param cls class of the custom data type that should be converted
     * @return true if this factory supports to convert the custom data type
     */
    boolean supportsType(Class<?> cls);

    /**
     * This method will be called to get all supported types for conversion
     * @return List of supported converter class types
     */
    List<Class> getSupportedTypes();

    /**
     * Returns a unique identifier.
     * @return a unique identifier
     */
    int getTypeIdentifier();

    /**
     * Returns a converter that can be used to convert a dolphin data type to a custom data type.
     * @param cls the dolphin data type
     * @return the converter
     */
    Converter getConverterForType(Class<?> cls);

}
