/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)(TM)
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

package com.caucho.v5.inject.impl;

import javax.inject.Provider;

import com.caucho.v5.inject.BindingAmp;
import com.caucho.v5.inject.InjectorAmp;

import io.baratine.inject.Key;

/**
 * A method producer that returns a new injection bean for the given type and
 * qualifiers.
 */
public class ProviderDelegate<T> implements BindingAmp<T>, Provider<T>
{
  private Key<T> _key;
  private int _priority;
  
  private Provider<? extends T> _supplier;

  private InjectorAmp _manager;

  public ProviderDelegate(InjectorAmp manager,
                          Key<T> key,
                          int priority,
                          Provider<? extends T> supplier)
  {
    _manager = manager;
    _key = key;
    _priority = priority;
    
    _supplier = supplier;
  }
  
  @Override  
  public Key<T> key()
  {
    return _key;
  }
  
  @Override
  public int priority()
  {
    return _priority;
  }
  
  /*
  @Override
  public boolean isMatch(Key key)
  {
    return _key.isAssignableFrom(key);
  }
  */

  @Override
  public Provider<T> provider()
  {
    return this;
  }
  
  /*
  @Override
  public T create(Type type, Annotation ...qualifiers)
  {
    return _supplier.get();
  }
  */
  
  @Override
  public T get()
  {
    return _supplier.get();
  }
  
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _key + "]";
  }
}

