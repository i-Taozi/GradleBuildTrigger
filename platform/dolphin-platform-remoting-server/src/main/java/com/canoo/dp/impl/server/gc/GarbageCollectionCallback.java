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
package com.canoo.dp.impl.server.gc;

import org.apiguardian.api.API;

import java.util.Set;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A callback that is called by the {@link GarbageCollector} after each garbage collection.
 */
@API(since = "0.x", status = INTERNAL)
public interface GarbageCollectionCallback {

    /**
     * The method is called by the GC to withoutResult all instanced of the models that can be removed
     * @param rejectedInstances set of all instances that can be removed
     */
    void onReject(Set<Instance> rejectedInstances);

}
