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
package com.canoo.dp.impl.server.model;

import com.canoo.dp.impl.remoting.BeanRepository;
import com.canoo.dp.impl.server.gc.GarbageCollector;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Interface that defines the {@link BeanRepository} for the server.
 */
@API(since = "0.x", status = INTERNAL)
public interface ServerBeanRepository extends BeanRepository {

    /**
     * Method should be called if a bean get rejected by the Dolphin Platform garbage collection. This means
     * that the bean isn't referenced anymore by another bean and can be removed. Implementations shoudl removePresentationModel
     * the bean from the model / remoting layer. For more information see {@link GarbageCollector}
     * @param rejectedBean the rejected bean
     * @param <T> type of the bean.
     */
    <T> void onGarbageCollectionRejection(T rejectedBean);
}
