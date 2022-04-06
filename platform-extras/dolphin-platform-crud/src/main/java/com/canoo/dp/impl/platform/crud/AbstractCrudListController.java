package com.canoo.dp.impl.platform.crud;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.data.CrudService;
import com.canoo.dp.impl.platform.data.EntityWithId;
import com.canoo.dp.impl.platform.data.mapping.BeanConverter;
import com.canoo.dp.impl.platform.data.mapping.BeanMapperImpl;
import com.canoo.platform.core.functional.Subscription;
import com.canoo.platform.remoting.BeanManager;
import com.canoo.platform.remoting.ObservableList;
import com.canoo.platform.remoting.server.event.RemotingEventBus;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCrudListController<ID extends Serializable, B, E extends EntityWithId<ID>> {

    private final CrudService<ID, E> crudService;

    private final BeanMapperImpl mapper;

    private final Class<B> modelClass;

    private final Class<E> entityClass;

    private final RemotingEventBus eventBus;

    private List<E> entities;

    private final Subscription eventBusSubscription;
    
    protected AbstractCrudListController(final Class<B> modelClass, final Class<E> entityClass, final BeanManager manager, final CrudService<ID, E> crudService, final BeanConverter<ID, B, E> converter, final RemotingEventBus eventBus) {
        this.modelClass = Assert.requireNonNull(modelClass, "modelClass");
        this.entityClass = Assert.requireNonNull(entityClass, "entityClass");
        this.crudService = Assert.requireNonNull(crudService, "crudService");
        this.eventBus = Assert.requireNonNull(eventBus, "eventBus");
        this.entities = Collections.emptyList();
        Assert.requireNonNull(manager, "manager");
        Assert.requireNonNull(converter, "converter");
        this.mapper = new BeanMapperImpl(manager);
        mapper.addConverter(modelClass, entityClass, converter);
        eventBusSubscription = eventBus.subscribe(CrudConstants.CRUD_EVENT_TOPIC, e -> onCrudEvent(e.getData()));
    }

    @PostConstruct
    protected void onDestroy() {
        eventBusSubscription.unsubscribe();
    }

    private void onCrudEvent(final CrudEvent event) {
        //TODO: Would be perfect to get meta information for example the user.
        Assert.requireNonNull(event, "event");
        if(event.getEntityClass().equals(entityClass)) {
            final ID id = (ID) event.getId();
            entities.stream()
                    .filter(e -> id.equals(e.getId()))
                    .findFirst()
                    .ifPresent(e -> onEntityDirty(e, event.getEventType()));
        }
    }

    protected void onEntityDirty(final E entity, final CrudEventType type) {
        
    }

    protected void showAll() {
        setEntities(crudService.findAll());
    }

    protected void addNew() {
        final List<E> newList = new ArrayList<>(getEntities());
        newList.add(crudService.createNewInstance());
        setEntities(newList);
    }

    protected void deleteSelected() {
        delete(getSelectedEntities());
    }

    protected void resetSelected() {
        reset(getSelectedEntities());
    }

    protected void delete(final List<E> entities) {
        entities.stream()
                .filter(e -> e.getId() != null)
                .forEach(e -> crudService.delete(e));
        final List<E> newList = new ArrayList<>(getEntities());
        newList.removeAll(entities);
        setEntities(newList);
    }

    protected void reset(final List<E> entities) {
        List<E> resetedEntities = entities.stream()
                .map(e -> crudService.reset(e))
                .collect(Collectors.toList());
        final List<E> newList = new ArrayList<>(getEntities());
        newList.replaceAll(e -> {
            final int index = entities.indexOf(e);
            if(index >= 0) {
                return resetedEntities.get(index);
            }
            return e;
        });
        setEntities(newList);
    }

    protected void resetAll() {
        List<E> newEntities = entities.stream().map(e -> crudService.reset(e)).collect(Collectors.toList());
        setEntities(newEntities);
    }

    protected void deleteAll() {
        entities.forEach(e -> crudService.delete(e));
        setEntities(Collections.emptyList());
    }

    public List<E> getEntities() {
        return entities;
    }

    public void setEntities(final List<E> entities) {
        this.entities = Collections.unmodifiableList(entities);
        updateModel();
    }

    protected void updateModel() {
        final List<B> updated = entities.stream()
                .filter(e -> !mapper.hasBeanForEntity(e, modelClass))
                .map(e -> mapper.toBean(e, modelClass))
                .collect(Collectors.toList());

        final List<B> toDelete = getModel().stream()
                .filter(b -> !updated.contains(b))
                .collect(Collectors.toList());
        getModel().removeAll(toDelete);

        final List<B> toAdd = entities.stream()
                .filter(e -> !mapper.hasBeanForEntity(e, modelClass))
                .map(e -> mapper.toBean(e, modelClass))
                .collect(Collectors.toList());
        getModel().addAll(toAdd);
    }

    protected abstract ObservableList<B> getModel();

    protected abstract List<B> getSelected();

    protected CrudService<ID, E> getCrudService() {
        return crudService;
    }

    private List<E> getSelectedEntities() {
        return getSelected().stream()
                .map(b -> mapper.toEntity(b, entityClass))
                .collect(Collectors.toList());
    }

    private String getEntityType() {
        return entityClass.getSimpleName();
    }
}
