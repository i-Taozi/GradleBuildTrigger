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
package com.canoo.platform.server.spi;

import com.canoo.platform.core.PlatformConfiguration;
import org.apiguardian.api.API;

import java.util.List;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * This interface defines a server module that will automatically be started when the Dolphin Platform server component starts. Dolphin Platform will search for all implementations of this interface on the classpath that are annotated by {@link ModuleDefinition} and will automatically start them based on the order that is defined by the {@link ModuleDefinition} annotation.
 *
 * @author Hendrik Ebbers
 * @see ModuleDefinition
 *
 */
@API(since = "0.x", status = EXPERIMENTAL)
public interface ServerModule {

    /**
     * Some modules depend on other modules. This method returns a collection of the names of all modules that are needed to start this module. The name of a module is defined in the {@link ModuleDefinition} annotation.
     * @return a set of the names of all modules that are needed to start this module
     */
    List<String> getModuleDependencies();

    /**
     * Returns the unique name of the module.
     * @return the unique name of the module
     */
    String getName();

    /**
     * Returns true if the module will be booted at the Dolphin Platform bootstrap, otherwise false.
     * @param configuration the Dolphin Platform configuration
     * @return true if the module will be booted
     */
    boolean shouldBoot(PlatformConfiguration configuration);

    /**
     * This method will be called by the Dolphin Platform to initialize the module.
     * @param coreComponents the core components
     * @throws ModuleInitializationException if the module can not be initialized
     */
    void initialize(final ServerCoreComponents coreComponents) throws ModuleInitializationException;
}
