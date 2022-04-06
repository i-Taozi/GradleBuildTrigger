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
package com.canoo.dp.impl.server.mbean.beans;

import org.apiguardian.api.API;

import java.util.Set;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Interface for a MBean that defines a Dolphin Platform session (see {@link com.canoo.dolphin.server.DolphinSession})
 */
@API(since = "0.x", status = INTERNAL)
public interface DolphinSessionInfoMBean {

    String getDolphinSessionId();

    Set<String> getAttributesNames();

    Object getAttribute(String name);

    long getGarbageCollectionRuns();

    long getGarbageCollectionRemovedBeansTotal();

    int getGarbageCollectionCurrentManagedBeansCount();
}
