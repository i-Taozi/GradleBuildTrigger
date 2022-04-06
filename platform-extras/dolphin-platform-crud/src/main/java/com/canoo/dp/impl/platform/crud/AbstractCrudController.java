package com.canoo.dp.impl.platform.crud;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.data.CrudService;
import com.canoo.dp.impl.platform.data.EntityWithId;
import com.canoo.dp.impl.platform.data.mapping.BeanConverter;
import com.canoo.dp.impl.platform.data.mapping.BeanMapperImpl;
import com.canoo.platform.remoting.BeanManager;
import com.canoo.platform.remoting.server.event.RemotingEventBus;

import javax.annotation.PostConstruct;
import java.io.Serializable;

public abstract class AbstractCrudController<ID extends Serializable, B, E extends EntityWithId<ID>> {

    private final CrudService<ID, E> crudService;

    private final BeanMapperImpl mapper;

    private final Class<B> modelClass;

    private final Class<E> entityClass;

    private final RemotingEventBus eventBus;

    private E entity;

    protected AbstractCrudController(final Class<B> modelClass, final Class<E> entityClass, final BeanManager manager, final CrudService<ID, E> crudService, final BeanConverter<ID, B, E> converter, final RemotingEventBus eventBus) {
        this.modelClass = Assert.requireNonNull(modelClass, "modelClass");
        this.entityClass = Assert.requireNonNull(entityClass, "entityClass");
        this.crudService = Assert.requireNonNull(crudService, "crudService");
        this.eventBus = Assert.requireNonNull(eventBus, "eventBus");
        Assert.requireNonNull(manager, "manager");
        Assert.requireNonNull(converter, "converter");
        this.mapper = new BeanMapperImpl(manager);
        mapper.addConverter(modelClass, entityClass, converter);
    }

    @PostConstruct
    protected void onDestroy() {
        if(entity != null && entity.getId() != null) {
            eventBus.publish(CrudConstants.CRUD_EVENT_TOPIC, new CrudEvent(entityClass, entity.getId(), CrudEventType.CLOSE));
        }
    }

    protected void showNew() {
        setEntity(crudService.createNewInstance());
        updateModel();
    }

    protected void save() {
        final E updatedEntity = mapper.toEntity(getModel(), entityClass);
        eventBus.publish(CrudConstants.CRUD_EVENT_TOPIC, new CrudEvent(entityClass, updatedEntity.getId(), CrudEventType.SAVE));
        setEntity(crudService.save(updatedEntity));
        updateModel();
    }

    protected void reset() {
        setEntity(crudService.reset(entity));
        updateModel();
    }

    protected void show(final ID id) {
        final E loadedEntity = crudService.byId(id)
                .orElseThrow(() -> new IllegalArgumentException("Can not find entity of type '" + getEntityType() + "' with id '" + id + "'"));
        setEntity(loadedEntity);
    }

    protected E getEntity() {
        return entity;
    }

    protected void setEntity(final E entity) {
        if(this.entity != null && this.entity.getId() != null) {
            eventBus.publish(CrudConstants.CRUD_EVENT_TOPIC, new CrudEvent(entityClass, this.entity.getId(), CrudEventType.CLOSE));
        }
        this.entity = entity;
        if(this.entity != null && this.entity.getId() != null) {
            eventBus.publish(CrudConstants.CRUD_EVENT_TOPIC, new CrudEvent(entityClass, this.entity.getId(), CrudEventType.SHOW));
        }
        updateModel();
    }

    protected abstract B getModel();

    protected CrudService<ID, E> getCrudService() {
        return crudService;
    }

    private void updateModel() {
        mapper.updateBean(entity, getModel(), modelClass);
    }

    private String getEntityType() {
        return entityClass.getSimpleName();
    }

}
