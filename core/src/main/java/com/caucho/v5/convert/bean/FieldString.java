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

package com.caucho.v5.convert.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * getter/setter for bean fields.
 */
public class FieldString<T> extends FieldBase<T>
{
  private MethodHandle _getter;
  private MethodHandle _setter;

  public FieldString(Field field)
  {
    super(field);

    try {
      MethodHandle getter = MethodHandles.lookup().unreflectGetter(field);
      _getter = getter.asType(MethodType.methodType(String.class, Object.class));

      MethodHandle setter = MethodHandles.lookup().unreflectSetter(field);
      _setter = setter.asType(MethodType.methodType(void.class, Object.class, String.class));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public final String getString(T bean)
  {
    try {
      return (String) _getter.invokeExact((Object) bean);
    } catch (Throwable e) {
      throw error(e);
    }
  }

  @Override
  public final void setString(T bean, String value)
  {
    try {
      _setter.invokeExact((Object) bean, value);
    } catch (Throwable e) {
      throw error(e);
    }
  }

  @Override
  public final Object getObject(T bean)
  {
    return getString(bean);
  }

  @Override
  public final void setObject(T bean, Object value)
  {
    setString(bean, value != null ? String.valueOf(value) : null);
  }
}
