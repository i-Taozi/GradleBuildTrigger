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
package com.canoo.dp.impl.platform.data.mapping;

import com.canoo.dp.impl.platform.data.EntityWithId;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public interface BeanMapper {

    <ID extends Serializable, B, E extends EntityWithId<ID>> E toEntity(B bean, Class<E> entityClass);

    default <ID extends Serializable, B, E extends EntityWithId<ID>> List<E> toEntityList(List<B> beanList, Class<E> entityClass) {
        return beanList.stream()
                .map(b -> toEntity(b, entityClass))
                .collect(Collectors.toList());
    }

    <ID extends Serializable, B, E extends EntityWithId<ID>> boolean hasBeanForEntity(E entity, Class<B> beanClass);

    <ID extends Serializable, B, E extends EntityWithId<ID>> B findBeanForEntity(E entity, Class<B> beanClass);

    <ID extends Serializable, B, E extends EntityWithId<ID>> B updateBean(E entity, B bean, Class<B> beanClass);

    <ID extends Serializable, B, E extends EntityWithId<ID>> B toBean(E entity, Class<B> beanClass);
}
