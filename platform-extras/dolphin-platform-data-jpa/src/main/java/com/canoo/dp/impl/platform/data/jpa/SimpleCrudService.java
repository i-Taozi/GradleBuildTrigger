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
package com.canoo.dp.impl.platform.data.jpa;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.data.CrudService;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;

public class SimpleCrudService<E extends AbstractEntity> implements CrudService<Long, E> {

    private final Class<E> entityType;

    private final EntityManager entityManager;

    public SimpleCrudService(final Class<E> entityType, final EntityManager entityManager) {
        this.entityType = entityType;
        this.entityManager = entityManager;
    }

    @Override
    public E createNewInstance() {
        try {
            return entityType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("TODO");
        }
    }

    @Override
    public List<E> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityType);
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public E findById(Long id) {
        return entityManager.find(entityType, id);
    }

    @Override
    public Optional<E> byId(final Long id) {
        return Optional.ofNullable(findById(id));
    }

    @Override
    public Class<E> getDataClass() {
        return entityType;
    }

    @Override
    public E reset(final E entity) {
        Assert.requireNonNull(entity , "entity");
        final Long id = entity.getId();

        if(id != null) {
            entityManager.detach(entity);
            return findById(id);
        } else {
            return createNewInstance();
        }
    }

    @Override
    public E save(final E entity) {
        Assert.requireNonNull(entity , "entity");
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    @Override
    public void delete(final E toDelete) {
        entityManager.remove(toDelete);
    }

}
