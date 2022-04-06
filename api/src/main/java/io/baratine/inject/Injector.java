/*
 * Copyright (c) 1998-2016 Caucho Technology -- all rights reserved
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

package io.baratine.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Provider;

import io.baratine.config.Config;
import io.baratine.config.IncludeGenerator;
import io.baratine.convert.ConvertManager;
import io.baratine.spi.ServiceManagerProvider;

/**
 * The injection manager interface
 */
public interface Injector
{
  /**
   * Returns an instance provider for the given Key, which combines
   * a Type with &#64;Qualifier annotations.
   */
  <T> Provider<T> provider(Key<T> key);

  /**
   * Returns an instance provider for the given InjectionPoint.
   */
  <T> Provider<T> provider(InjectionPoint<T> atPoint);

  /**
   * Returns an injected instance for the given type. The instance returned
   * depends on the bindings of the inject manager.
   */
  <T> T instance(Class<T> type);

  /**
   * Returns an injected instance for the given Key, which combines
   * a type with annotations.
   */
  <T> T instance(Key<T> key);

  /**
   * Returns an injected instance for the given InjectionPoint.
   */
  <T> T instance(InjectionPoint<T> ip);

  /**
   * Consumer for injecting dependencies.
   */
  <T> Consumer<T> injector(Class<T> type);

  /**
   * Returns the bindings associated with a key.
   */
  <T> List<Binding<T>> bindings(Key<T> key);

  /**
   * Returns the configuration.
   */
  Config config();

  /**
   * Returns the config property.
   */
  String property(String key);

  /**
   * Returns the type converter from a source class to a target class.
   */
  ConvertManager converter();
  //<S,T> Convert<S, T> converter(Class<S> source, Class<T> target);


  /**
   * Creates a new manager.
   */
  public static InjectorBuilder newManager(ClassLoader classLoader)
  {
    return ServiceManagerProvider.current().injectManager(classLoader);
  }

  public static InjectorBuilder newManager()
  {
    return newManager(Thread.currentThread().getContextClassLoader());
  }

  public interface InjectorBuilder
  {
    default InjectorBuilder include(Class<?> includeClass)
    {
      throw new UnsupportedOperationException(getClass().getName());
    }

    <T> BindingBuilder<T> bean(Class<T> impl);
    <T> BindingBuilder<T> bean(T instance);

    <T> BindingBuilder<T> provider(Provider<T> provider);

    //<T,X> BindingBuilder<T> function(Function<InjectionPoint<X>,T> function);

    <T,U> BindingBuilder<T> provider(Key<U> parent, Method m);

    InjectorBuilder autoBind(InjectAutoBind autoBind);

    InjectorBuilder property(String key, String value);

    Injector get();
  }

  public interface BindingBuilder<T>
  {
    BindingBuilder<T> to(Class<? super T> api);
    BindingBuilder<T> to(Key<? super T> key);

    BindingBuilder<T> priority(int priority);
    BindingBuilder<T> scope(Class<? extends Annotation> scopeType);
  }

  public interface IncludeInject extends IncludeGenerator<InjectorBuilder>
  {
  }

  public interface InjectAutoBind
  {
    <T> Provider<T> provider(Injector manager, Key<T> key);

    default <T> Provider<T> provider(Injector injector,
                                     InjectionPoint<T> ip)
    {
      return provider(injector, ip.key());
    }
  }
}

