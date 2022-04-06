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
package com.canoo.dp.impl.remoting;

import com.canoo.platform.core.functional.Subscription;
import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.legacy.core.ModelStore;
import com.canoo.dp.impl.remoting.legacy.core.PresentationModel;
import org.apiguardian.api.API;

import java.util.*;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A {@code BeanRepository} keeps a list of all registered Dolphin Beans and the mapping between OpenDolphin IDs and
 * the associated Dolphin Bean.
 * <p>
 * A new bean needs to be registered with the {@link #registerBean(Object, PresentationModel, UpdateSource)} method and can be deleted
 * with the {@link #delete(Object)} method.
 */
// TODO mapDolphinToObject() does not really fit here, we should probably move it to Converters, but first we need to fix scopes
@API(since = "0.x", status = INTERNAL)
public class BeanRepositoryImpl implements BeanRepository {

    private final Map<Object, PresentationModel> objectPmToDolphinPm = new IdentityHashMap<>();
    private final Map<String, Object> dolphinIdToObjectPm = new HashMap<>();
    private final ModelStore modelStore;
    private final Map<Class<?>, List<BeanAddedListener<?>>> beanAddedListenerMap = new HashMap<>();
    private final List<BeanAddedListener<Object>> anyBeanAddedListeners = new ArrayList<>();
    private final Map<Class<?>, List<BeanRemovedListener<?>>> beanRemovedListenerMap = new HashMap<>();

    private List<BeanRemovedListener<Object>> anyBeanRemovedListeners = new ArrayList<>();

    public BeanRepositoryImpl(final ModelStore modelStore, final EventDispatcher dispatcher) {
        this.modelStore = Assert.requireNonNull(modelStore, "modelStore");

        dispatcher.addRemovedHandler((PresentationModel model) -> {
            final Object bean = dolphinIdToObjectPm.remove(model.getId());
            if (bean != null) {
                objectPmToDolphinPm.remove(bean);
                if(beanRemovedListenerMap.containsKey(bean.getClass())){
                    for (final BeanRemovedListener beanRemovedListener : beanRemovedListenerMap.get(bean.getClass())) {
                        beanRemovedListener.beanDestructed(bean);
                    }
                }
                for (final BeanRemovedListener beanRemovedListener : anyBeanRemovedListeners) {
                    beanRemovedListener.beanDestructed(bean);
                }
            }
        });
    }

    @Override
    public <T> Subscription addOnAddedListener(final Class<T> beanClass, final BeanAddedListener<? super T> listener) {
        DolphinUtils.assertIsDolphinBean(beanClass);
        beanAddedListenerMap.computeIfAbsent(beanClass, s -> new ArrayList()).add(listener);
        return () -> {
            if (beanAddedListenerMap.containsKey(beanClass)) {
                beanAddedListenerMap.get(beanClass).remove(listener);
            }
        };
    }

    @Override
    public Subscription addOnAddedListener(final BeanAddedListener<Object> listener) {
        anyBeanAddedListeners.add(listener);
        return () -> anyBeanAddedListeners.remove(listener);
    }

    @Override
    public <T> Subscription addOnRemovedListener(final Class<T> beanClass, final BeanRemovedListener<? super T> listener) {
        DolphinUtils.assertIsDolphinBean(beanClass);
        beanRemovedListenerMap.computeIfAbsent(beanClass, s -> new ArrayList()).add(listener);
        return () -> {
            if (beanRemovedListenerMap.containsKey(beanClass)) {
                beanRemovedListenerMap.get(beanClass).remove(listener);
            }
        };
    }

    @Override
    public Subscription addOnRemovedListener(final BeanRemovedListener<Object> listener) {
        anyBeanRemovedListeners.add(listener);
        return () -> anyBeanRemovedListeners.remove(listener);
    }

    @Override
    public boolean isManaged(Object bean) {
        DolphinUtils.assertIsDolphinBean(bean);
        return objectPmToDolphinPm.containsKey(bean);
    }

    @Override
    public <T> void delete(T bean) {
        DolphinUtils.assertIsDolphinBean(bean);
        final PresentationModel model = objectPmToDolphinPm.remove(bean);
        if (model != null) {
            dolphinIdToObjectPm.remove(model.getId());
            modelStore.remove(model);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Class<T> beanClass) {
        DolphinUtils.assertIsDolphinBean(beanClass);
        final List<T> result = new ArrayList<>();
        final List<PresentationModel> presentationModels = modelStore.findAllPresentationModelsByType(DolphinUtils.getDolphinPresentationModelTypeForClass(beanClass));
        for (PresentationModel model : presentationModels) {
            result.add((T) dolphinIdToObjectPm.get(model.getId()));
        }
        return result;
    }

    @Override
    public Object getBean(String sourceId) {
        if(sourceId == null) {
            return null;
        }
        if(!dolphinIdToObjectPm.containsKey(sourceId)) {
            throw new IllegalArgumentException("No bean instance found with id " + sourceId);
        }
        return dolphinIdToObjectPm.get(sourceId);
    }

    @Override
    public String getDolphinId(Object bean) {
        if (bean == null) {
            return null;
        }
        DolphinUtils.assertIsDolphinBean(bean);
        try {
            return objectPmToDolphinPm.get(bean).getId();
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("Only managed Dolphin Beans can be used.", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerBean(Object bean, PresentationModel model, UpdateSource source) {
        DolphinUtils.assertIsDolphinBean(bean);
        objectPmToDolphinPm.put(bean, model);
        dolphinIdToObjectPm.put(model.getId(), bean);

        if (source == UpdateSource.OTHER) {
            if(beanAddedListenerMap.containsKey(bean.getClass())){
                for (final BeanAddedListener beanAddedListener : beanAddedListenerMap.get(bean.getClass())) {
                    beanAddedListener.beanCreated(bean);
                }
            }
            for (final BeanAddedListener beanAddedListener : anyBeanAddedListeners) {
                beanAddedListener.beanCreated(bean);
            }
        }
    }


}
