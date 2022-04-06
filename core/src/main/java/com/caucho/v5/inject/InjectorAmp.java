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

package com.caucho.v5.inject;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;

import javax.inject.Provider;

import com.caucho.v5.inject.impl.InjectorImpl;

import io.baratine.config.Config;
import io.baratine.config.Config.ConfigBuilder;
import io.baratine.inject.Binding;
import io.baratine.inject.Injector;
import io.baratine.inject.Key;
import io.baratine.inject.Injector.InjectorBuilder;
import io.baratine.spi.ServiceManagerProvider;

/**
 * The injection manager for a given environment.
 */
public interface InjectorAmp extends Injector
{
  public static InjectorAmp current()
  {
    return current(Thread.currentThread().getContextClassLoader());
  }

  public static InjectorAmp current(ClassLoader classLoader)
  {
    //return ServiceManagerProvider.current().injectCurrent(classLoader);
    return InjectorImpl.current(classLoader);
  }

  public static InjectBuilderAmp manager(ClassLoader classLoader)
  {
    return (InjectBuilderAmp) ServiceManagerProvider.current().injectManager(classLoader);
  }

  public static InjectBuilderAmp manager()
  {
    return manager(Thread.currentThread().getContextClassLoader());
  }

  <T> Iterable<Binding<T>> bindings(Class<T> type);

  Config config();

  // <S,T> Convert<S, T> converter(Class<S> source, Class<T> target);

  public interface InjectBuilderAmp extends InjectorBuilder
  {
    InjectBuilderAmp context(boolean isContext);

    <U> void include(Key<U> keyParent, Method method);

    ConfigBuilder config();

    @Override
    InjectorAmp get();

    @Override
    InjectBuilderAmp property(String key, String value);
  }

  //
  // XXX: implementation
  //

  public static InjectorAmp create(ClassLoader classLoader)
  {
    return InjectorImpl.create(classLoader);
  }

  public static InjectorAmp create()
  {
    return create(Thread.currentThread().getContextClassLoader());
  }

  Provider<?> []program(Parameter[] parameters);

  <T> Consumer<T> injector(Class<T> type);

}

