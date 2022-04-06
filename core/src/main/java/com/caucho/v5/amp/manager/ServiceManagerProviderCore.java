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

package com.caucho.v5.amp.manager;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.Amp;
import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.proxy.ProxyHandleAmp;
import com.caucho.v5.amp.spi.MessageAmp;
import com.caucho.v5.amp.spi.OutboxAmp;
import com.caucho.v5.inject.InjectorAmp;
import com.caucho.v5.inject.InjectorAmp.InjectBuilderAmp;
import com.caucho.v5.inject.impl.InjectorImpl;
import com.caucho.v5.loader.EnvLoader;

import io.baratine.service.ServiceRef;
import io.baratine.service.Services;
import io.baratine.spi.ServiceManagerProvider;

/**
 * Provider for the Services.
 */
public class ServiceManagerProviderCore
  extends ServiceManagerProvider
{
  private static final Logger log = Logger.getLogger(ServiceManagerProviderCore.class.getName());
  
  public ServiceManagerProviderCore()
  {
    EnvLoader.addCloseListener(this);
  }
  
  @Override
  public ServicesAmp currentManager()
  {
    return Amp.getCurrentManager();
  }
  
  @Override
  public Services.ServicesBuilder newManager()
  {
    return new ServicesBuilderImpl();
  }
  
  @Override
  public ServiceRef toRef(Object serviceProxy)
  {
    Objects.requireNonNull(serviceProxy);
      
    ProxyHandleAmp proxyHandle = (ProxyHandleAmp) serviceProxy;
    
    return proxyHandle.__caucho_getServiceRef();
  }

  @Override
  public ServiceRef currentServiceRef()
  {
    OutboxAmp outbox = OutboxAmp.current();
    
    if (outbox != null) {
      return outbox.inbox().serviceRef();
    }

    try {
      ServicesAmp manager = currentManager();
    
      if (manager != null) {
        return manager.inboxSystem().serviceRef();
      }
      else {
        return null;
      }
    } catch (Exception e) {
      log.log(Level.FINEST, e.toString(), e);
      
      return null;
    }
  }

  //@Override
  public MessageAmp getCurrentMessage()
  {
    OutboxAmp outbox = OutboxAmp.current();
    
    if (outbox != null) {
      return outbox.message();
    }
    else {
      return null;
    }
  }
  
  @Override
  public void flushOutbox()
  {
    OutboxAmp outbox = OutboxAmp.current();

    if (outbox != null) {
      outbox.flush();
    }
  }
  
  @Override
  public boolean flushOutboxAndExecuteLast()
  {
    OutboxAmp outbox = OutboxAmp.current();

    if (outbox != null) {
      return outbox.flushAndExecuteLast();
    }
    else {
      return false;
    }
  }

  //@Override
  public InjectorAmp injectCurrent(ClassLoader classLoader)
  {
    return InjectorImpl.current(classLoader);
  }

  //@Override
  public InjectorAmp injectCreate(ClassLoader classLoader)
  {
    return InjectorImpl.create(classLoader);
  }

  @Override
  public InjectBuilderAmp injectManager(ClassLoader classLoader)
  {
    return InjectorImpl.manager(classLoader);
  }
}
