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
package com.canoo.dp.impl.platform.core;

import org.apiguardian.api.API;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class IdentitySet<E> implements Set<E> {

    private IdentityHashMap<E, E> internalMap;

    public IdentitySet() {
        internalMap = new IdentityHashMap<>();
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return internalMap.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return internalMap.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return internalMap.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internalMap.keySet().toArray(a);
    }

    @Override
    public boolean add(final E e) {
        final boolean ret = internalMap.keySet().contains(e);
        internalMap.put(e, e);
        return !ret;
    }

    @Override
    public boolean remove(final Object o) {
        final boolean ret = internalMap.keySet().contains(o);
        internalMap.remove(o);
        return ret;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return internalMap.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        boolean ret = false;
        for(final E elem : c) {
            boolean currentRet = add(elem);
            if(currentRet) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean ret = false;
        for(final Object elem : c) {
            boolean currentRet = remove(elem);
            if(currentRet) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public void clear() {
        internalMap.clear();
    }
}
