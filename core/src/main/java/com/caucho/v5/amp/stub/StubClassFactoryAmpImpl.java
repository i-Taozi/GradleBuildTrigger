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

package com.caucho.v5.amp.stub;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.service.ServiceConfig;
import com.caucho.v5.amp.spi.StubContainerAmp;

import io.baratine.service.OnLookup;
import io.baratine.service.OnSave;

/**
 * Creates service stubs for service implementation beans..
 */
public class StubClassFactoryAmpImpl implements StubClassFactoryAmp
{
  /*
  private static WeakHashMap<ClassLoader,SoftReference<AmpProxyCache>> _cacheMap
    = new WeakHashMap<>();
    */
    
  private final ServicesAmp _ampManager;
  
  //private AmpProxyCache _proxyCache;
  
  private StubClassFactory _stubClassFactory;

  /*
  private ConcurrentHashMap<Class<?>,StubClass> _stubMap
    = new ConcurrentHashMap<>();
    */
  
  private ConcurrentHashMap<Class<?>,ClassStubSession> _skeletonChannelMap
    = new ConcurrentHashMap<>();
      
  public StubClassFactoryAmpImpl(ServicesAmp services)
  {
    Objects.requireNonNull(services);
    
    _ampManager = services;
    
    _stubClassFactory = new StubClassFactory(services);
  }
  
  @Override
  public StubClass stubClass(Class<?> type,
                        Class<?> api)
  {
    Objects.requireNonNull(type);
    
    if (api == null) {
      api = type;
    }
    
    return _stubClassFactory.stubClass(type, api);
  }

  @Override
  public StubAmp stub(Object bean,
                      String path,
                      String childPath,
                      StubContainerAmp container,
                      ServiceConfig config)
  {
    StubClass stubClass;
    
    /*
    if (path != null && (path.startsWith("pod://") || path.startsWith("public://"))) {
      stubClass = createPodSkeleton(bean.getClass(), path, config);
    }
    else {
      stubClass = stubClass(bean.getClass(), config);
    }
    */
    
    Class<?> api = null;
    
    if (config != null) {
      config.api();
    }
    
    if (api == null) {
      api = bean.getClass();
    }
    
    stubClass = _stubClassFactory.stubClass(bean.getClass(), api);
    
    if (container != null) {
      return new StubAmpBeanChild(stubClass, bean, path, childPath, container);
    }
    else {
      if (path == null && config != null) {
        path = config.name(); 
      }
      
      String name = path;
      
      return new StubAmpBean(stubClass, bean, name, null, config);
    }
  }

  /*
  protected StubClass createPodSkeleton(Class<?> beanClass, 
                                            String path,
                                            ServiceConfig config)
  {
    return stubClass(beanClass, config);
  }
  */
  
  private String getLocalPath(String path)
  {
    int p = path.indexOf("://");
    
    if (p < 0) {
      return null;
    }
    
    int q = path.indexOf("/", p + 3);
    
    if (q > 0) {
      return path.substring(q);
    }
    else {
      return null;
    }
  }
  
  /*
  private StubClass stubClass(Class<?> type,
                                ServiceConfig config)
  {
    StubClass skel = _stubMap.get(type);
    
    if (skel == null) {
      skel = new StubClass(_ampManager, type, type);
      skel.introspect();
      _stubMap.putIfAbsent(type, skel);
      skel = _stubMap.get(type);
    }
    
    return skel;
    
  }
  */

  /*
  @Override
  public StubAmp createSkeletonSession(Object bean,
                                        String key,
                                        ContextSession context,
                                        ServiceConfig config)
  {
    Class<?> beanClass = bean.getClass();
    
    ClassStubSession skel = _skeletonChannelMap.get(beanClass);
    
    if (skel == null) {
      skel = new ClassStubSession(_ampManager, beanClass);
      skel.introspect();
      _skeletonChannelMap.putIfAbsent(beanClass, skel);
      skel = _skeletonChannelMap.get(beanClass);
    }
    
    return new ActorSkeletonSession(skel, bean, key, context); 
  }
  */

  /*
  @Override
  public StubAmp createSkeletonMain(Class<?> api,
                                     String path,
                                     ServiceConfig config)
  {
    ClassStub skel = new ClassStub(_ampManager, api, config);
    skel.introspect();
    
    // XXX: need different actor
    return new StubAmpBeanBase(skel, path, null);
  }
  */
  
  /*
  @Override
  public <T> T createProxy(ServiceRefAmp serviceRef,
                           Class<T> api)

  {
    Objects.requireNonNull(api);
    
    if (ServiceRef.class.isAssignableFrom(api)) {
      throw new IllegalArgumentException(api.toString());
    }
    
    Thread thread = Thread.currentThread();
    // baratine/8098
    // ClassLoader loader = thread.getContextClassLoader();
    ClassLoader loader = serviceRef.manager().classLoader();
    
    try {
      //thread.setContextClassLoader(_ampManager.getClassLoader());
      
      AmpProxyCache cache = getCache(loader);

      Constructor<?> proxyCtor = null;
      
      synchronized (cache) {
        proxyCtor = cache.getProxy(api.getName());
      }
      
      if (proxyCtor == null) {
        proxyCtor = ProxyGeneratorAmp.create(api, loader);
        
        proxyCtor.setAccessible(true);
        
        synchronized (cache) {
          cache.putProxy(api.getName(), proxyCtor);
        }
      }
      
      InboxAmp systemInbox = serviceRef.manager().inboxSystem();
      MessageFactoryAmp messageFactory = _messageFactory;
      
      return (T) proxyCtor.newInstance(serviceRef, systemInbox, messageFactory);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      }
      
      throw ServiceException.createAndRethrow(e.getCause());
    } catch (Throwable e) {
      throw ServiceException.createAndRethrow(e);
    } finally {
      // thread.setContextClassLoader(oldLoader);
    }
  }
  */
  
  /*
  private AmpProxyCache getCache(ClassLoader loader)
  {
    synchronized (_cacheMap) {
      SoftReference<AmpProxyCache> cacheRef = _cacheMap.get(loader);
      
      AmpProxyCache cache = null;
      
      if (cacheRef != null) {
        cache = cacheRef.get();
      }
      
      if (cache == null) {
        cache = new AmpProxyCache();
        _cacheMap.put(loader, new SoftReference<AmpProxyCache>(cache));
      }
      
      return cache;
    }
  }
  */

  /*
  static class AmpProxyCache {
    private HashMap<String,Constructor<?>> _proxyMap = new HashMap<>();
    private HashMap<String,Constructor<?>> _reproxyMap = new HashMap<>();
    
    Constructor<?> getProxy(String name)
    {
      return _proxyMap.get(name);
    }
    
    void putProxy(String name, Constructor<?> ctor)
    {
      _proxyMap.put(name, ctor);
    }

    Constructor<?> getReproxy(String name)
    {
      return _reproxyMap.get(name);
    }
    
    void putReproxy(String name, Constructor<?> ctor)
    {
      _reproxyMap.put(name, ctor);
    }
  }
  */
}
