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

import com.canoo.platform.remoting.ListChangeEvent;
import com.canoo.platform.remoting.ListChangeListener;
import com.canoo.platform.remoting.ObservableList;
import com.canoo.platform.remoting.ValueChangeEvent;
import com.canoo.dp.impl.remoting.AbstractBeanBuilder;
import com.canoo.dp.impl.remoting.PresentationModelBuilderFactory;
import com.canoo.dp.impl.remoting.PropertyImpl;
import com.canoo.dp.impl.remoting.collections.ObservableArrayList;
import com.canoo.dp.impl.remoting.BeanRepository;
import com.canoo.dp.impl.remoting.ClassRepository;
import com.canoo.dp.impl.remoting.EventDispatcher;
import com.canoo.dp.impl.remoting.ListMapper;
import com.canoo.dp.impl.remoting.info.PropertyInfo;
import com.canoo.platform.remoting.Property;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.server.gc.GarbageCollector;
import com.canoo.dp.impl.remoting.legacy.core.Attribute;
import com.canoo.dp.impl.remoting.legacy.core.PresentationModel;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ServerBeanBuilderImpl extends AbstractBeanBuilder implements ServerBeanBuilder {

    private final GarbageCollector garbageCollector;

    public ServerBeanBuilderImpl(final ClassRepository classRepository, final BeanRepository beanRepository, final ListMapper listMapper, final PresentationModelBuilderFactory builderFactory, final EventDispatcher dispatcher, final GarbageCollector garbageCollector) {
        super(classRepository, beanRepository, listMapper, builderFactory, dispatcher);
        this.garbageCollector = Assert.requireNonNull(garbageCollector, "garbageCollector");
    }

    public <T> T createRootModel(Class<T> beanClass) {
        T bean = super.create(beanClass);
        garbageCollector.onBeanCreated(bean, true);
        return bean;
    }

    @Override
    public <T> T create(Class<T> beanClass) {
        T bean = super.create(beanClass);
        garbageCollector.onBeanCreated(bean, false);
        return bean;
    }

    protected <T> ObservableList<T> create(final PropertyInfo observableListInfo, final PresentationModel model, final ListMapper listMapper) {
        Assert.requireNonNull(model, "model");
        Assert.requireNonNull(listMapper, "listMapper");
        final ObservableList<T> list = new ObservableArrayList<T>() {
            @Override
            protected void notifyInternalListeners(ListChangeEvent<T> event) {
                listMapper.processEvent(observableListInfo, model.getId(), event);
            }
        };

        list.onChanged(new ListChangeListener<T>() {
            @Override
            public void listChanged(ListChangeEvent<? extends T> event) {
                for(ListChangeEvent.Change<? extends T> c : event.getChanges()) {
                    if(c.isAdded()) {
                        for(Object added : list.subList(c.getFrom(), c.getTo())) {
                            garbageCollector.onAddedToList(list, added);
                        }
                    }
                    if(c.isRemoved()) {
                        for(Object removed : c.getRemovedElements()) {
                            garbageCollector.onRemovedFromList(list, removed);
                        }
                    }
                    if(c.isReplaced()) {
                        //??? TODO
                    }
                }
            }
        });

        return list;
    }

    protected <T> Property<T> create(final Attribute attribute, final PropertyInfo propertyInfo) {
        return new PropertyImpl<T>(attribute, propertyInfo) {

            @Override
            protected void notifyInternalListeners(ValueChangeEvent event) {
                super.notifyInternalListeners(event);
                garbageCollector.onPropertyValueChanged(event.getSource(), event.getOldValue(), event.getNewValue());
            }
        };
    }
}

