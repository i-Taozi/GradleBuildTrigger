/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.json.ser;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.caucho.v5.inject.type.TypeRef;

public class MapJavaSerializer<T extends Map<K,V>,K,V>
  extends MapSerializerJson<K,V>
{
  private Class<?> _rawClass;

  MapJavaSerializer(TypeRef typeRef, 
                    JsonFactory factory,
                    Class<?> rawClass)
  {
    //super(typeRef, factory);
    super(typeRef, factory, new MapSupplier<K,V>(rawClass));
    
    Objects.requireNonNull(typeRef);
    
    //TypeRef valueRef = typeRef.to(Map.class).param(1);
    
    //_keyDeser = keyDeser;
    //_valueDeser = factory.serializer(valueRef.type());
    
    _rawClass = rawClass;
    
    if (Modifier.isAbstract(rawClass.getModifiers())) {
      throw new IllegalArgumentException(rawClass.getName());
    }
  }
  /*
  HashMapSerializer(TypeRef typeRef, JsonFactory factory)
  {
  }
  */

  /*
  @Override
  public T read(JsonReader in)
  {
    Event event = in.next();
    
    if (event == null || event == Event.VALUE_NULL) {
      return null;
    }
    
    if (event != Event.START_OBJECT) {
      throw new JsonException(L.l("expected object at {0}", event));
    }
    
    Map<Object,Object> map = newInstance();
    
    while ((event = in.peek()) == Event.KEY_NAME) {
      in.next();
      
      String key = in.getString();

      Object value = _valueDeser.read(in);
      
      map.put(key, value);
    }
    
    in.next();
    
    return (T) map;
  }
  */

  private static class MapSupplier<K,V> implements Supplier<Map<K,V>>
  {
    private Class<Map<K,V>> _rawClass;
    
    MapSupplier(Class<?> rawClass)
    {
      _rawClass = (Class) rawClass;
    }
    
    @Override
    public Map<K,V> get()
    {
      try {
        return (Map) _rawClass.newInstance();
      } catch (Exception e) {
        throw new JsonException(_rawClass.getName() + ": " + e, e);
      }
    }
  }
}
