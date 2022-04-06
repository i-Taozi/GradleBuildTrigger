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
package com.canoo.dp.impl.platform.data.jpa.event;

import com.canoo.dp.impl.platform.data.jpa.AbstractEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Listener for all JPA events. Defined as a singleton. {@link PersistenceListener} instances can be registered as listener.
 */
public class PersistenceListenerManager {

    private final static PersistenceListenerManager instance = new PersistenceListenerManager();

    private final List<PersistenceListener> listeners;

    private PersistenceListenerManager() {
        listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the singleton
     * @return the singleton
     */
    public static final PersistenceListenerManager getInstance() {
        return instance;
    }

    /**
     * Adds a listener that will be notified for all JPA events
     * @param listener the listener
     */
    public final void addListener(PersistenceListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener
     * @param listener the listener
     */
    public final void removeListener(PersistenceListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all listeners
     */
    public final void clear() {
        listeners.clear();
    }

    public final void firePersisted(AbstractEntity entity) {
        for(PersistenceListener listener : listeners) {
            listener.onEntityPersisted(entity);
        }
    }

    public final void fireRemoved(AbstractEntity entity) {
        for(PersistenceListener listener : listeners) {
            listener.onEntityRemoved(entity);
        }
    }

    public final void fireUpdated(AbstractEntity entity) {
        for(PersistenceListener listener : listeners) {
            listener.onEntityUpdated(entity);
        }
    }
}
