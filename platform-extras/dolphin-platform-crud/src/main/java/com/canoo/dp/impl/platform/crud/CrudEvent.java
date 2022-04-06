package com.canoo.dp.impl.platform.crud;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.data.EntityWithId;

import java.io.Serializable;

public class CrudEvent<ID extends Serializable, E extends EntityWithId<ID>> implements Serializable {

    private Class<E> entityClass;

    private ID id;

    private CrudEventType eventType;

    public CrudEvent() {
    }

    public CrudEvent(final Class<E> entityClass, final ID id, final CrudEventType eventType) {
        this();
        setEntityClass(entityClass);
        setId(id);
        setEventType(eventType);
    }

    public CrudEventType getEventType() {
        return eventType;
    }

    public void setEventType(final CrudEventType eventType) {
        this.eventType = Assert.requireNonNull(eventType, "eventType");
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(final Class<E> entityClass) {
        this.entityClass = Assert.requireNonNull(entityClass, "entityClass");
    }

    public ID getId() {
        return id;
    }

    public void setId(final ID id) {
        this.id = Assert.requireNonNull(id, "id");
    }

    public boolean matches(final EntityWithId entity) {
        Assert.requireNonNull(entity, "entity");
        if(!entity.getClass().equals(entityClass)) {
            return false;
        }
        final Serializable entityId = entity.getId();
        if(entityId == null || !id.equals(entityId)) {
            return false;
        }
        return true;
    }
}
