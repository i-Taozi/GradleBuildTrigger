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

package com.caucho.v5.amp.service;

import java.io.Serializable;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.amp.message.OnSaveMessage;
import com.caucho.v5.amp.spi.InboxAmp;
import com.caucho.v5.amp.spi.MessageAmp;
import com.caucho.v5.amp.spi.MethodRefAmp;
import com.caucho.v5.amp.spi.QueryRefAmp;
import com.caucho.v5.amp.spi.ShutdownModeAmp;
import com.caucho.v5.amp.stub.StubAmp;
import com.caucho.v5.inject.type.AnnotatedTypeClass;

import io.baratine.service.Cancel;
import io.baratine.service.Result;
import io.baratine.service.ServiceRef;

/**
 * Abstract implementation for a service ref.
 */
abstract public class ServiceRefBase implements ServiceRefAmp, Serializable
{
  private static final Logger log
    = Logger.getLogger(ServiceRefBase.class.getName());
  
  @Override
  abstract public String address();
  
  @Override
  public boolean isUp()
  {
    return ! isClosed();
  }
  
  @Override
  public boolean isClosed()
  {
    return false;
  }
  
  @Override
  public boolean isPublic()
  {
    return false;
  }
  
  /*
  @Override
  public String []getRemoteRoles()
  {
    return null;
  }
  
  @Override
  public boolean isRemoteSecure()
  {
    return false;
  }
  */
  
  @Override
  public InboxAmp inbox()
  {
    throw new UnsupportedOperationException(getClass().getName());
  }
  
  @Override
  public ServicesAmp services()
  {
    return inbox().manager();
  }
  
  @Override
  public StubAmp stub()
  {
    return null;
  }
  
  @Override
  public AnnotatedType api()
  {
    StubAmp stub = stub();
    
    if (stub != null) {
      return stub.api();
    }
    else {
      return AnnotatedTypeClass.ofObject();
    }
  }
  
  /*
  @Override
  public Annotation[] getApiAnnotations()
  {
    ActorAmp actor = getActor();
    
    if (actor != null) {
      return actor.getApiAnnotations();
    }
    else {
      return new Annotation[0];
    }
  }
  */

  @Override
  public MethodRefAmp methodByName(String methodName)
  {
    return new MethodRefNull(this, methodName);
  }

  @Override
  public MethodRefAmp method(String methodName, 
                             Type returnType, 
                             Class<?> ...param)
  {
    return new MethodRefNull(this, methodName);
  }
  
  @Override
  public MethodRefAmp methodByName(String methodName, Type returnType)
  {
    return methodByName(methodName);
  }
  
  @Override
  public Iterable<? extends MethodRefAmp> getMethods()
  {
    return new ArrayList<MethodRefAmp>();
  }

  @Override
  public void offer(MessageAmp msg)
  {
    throw new UnsupportedOperationException(this + " " + getClass().getName());
  }
  
  @Override
  public QueryRefAmp removeQueryRef(long id)
  {
    return inbox().removeQueryRef(id);
  }
  
  @Override
  public QueryRefAmp getQueryRef(long id)
  {
    return inbox().getQueryRef(id);
  }
  
  @Override
  public ServiceRefAmp lookup()
  {
    return null;
  }
  
  @Override
  public ServiceRefAmp onLookup(String path)
  {
    return null;
  }
  
  @Override
  public ServiceRefAmp service(String path)
  {
    return services().service(address() + path);
  }
  
  /*
  public ServiceRefAmp partition(int hash)
  {
    return this;
  }
  
  public int partitionSize()
  {
    return 1;
  }
  */

  @Override
  public ServiceRefAmp start()
  {
    return this;
  }

  @Override
  public ServiceRef save(Result<Void> result)
  {
    result.ok(null);
    
    return this;
  }

  /*
  @Override
  public ServiceRef service(Supplier<?> supplier, ServiceConfig config)
  {
    throw new UnsupportedOperationException(getClass().getName());
  }
  */

  /*
  @Override
  public ServiceRef unbind(String address)
  {
    throw new UnsupportedOperationException(getClass().getName());
  }
  */

  @Override
  public <T> T as(Class<T> api)
  {
    return services().newProxy(this, api);
  }

  @Override
  public ServiceRefAmp pin(Object serviceImpl)
  {
    Objects.requireNonNull(serviceImpl);
    
    // start();
    
    return services().pin(this, serviceImpl);
  }

  @Override
  public ServiceRefAmp pin(Object serviceImpl, String path)
  {
    Objects.requireNonNull(serviceImpl);
    
    // start();
    
    return services().pin(this, serviceImpl, path);
  }

  @Override
  public ServiceRefAmp bind(String address)
  {
    services().bind(this, address);
    
    return this;
  }
  
  @Override
  public void close(Result<Void> result)
  {
    shutdown(ShutdownModeAmp.GRACEFUL);
    result.ok(null);
  }

  @Override
  public void shutdown(ShutdownModeAmp mode)
  {
  }
  
  protected void shutdownCheckpoint(ShutdownModeAmp mode)
  {
    try {
      long timeout = 0;
    
      if (mode == ShutdownModeAmp.GRACEFUL) {
        OnSaveMessage checkpointMsg
          = new OnSaveMessage(inbox(), stub(), Result.ignore());
    
        inbox().offerAndWake(checkpointMsg, timeout);
      }
    
      //getInbox().offerAndWake(new MessageOnShutdown(getInbox(), mode), timeout);
      inbox().shutdown(mode);
    } catch (Throwable e) {
      log.log(Level.FINER, e.toString(), e);
    }
  }
  
  private Object writeReplace()
  {
    return new ServiceRefHandle(address(), services());
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + address() + "]";
  }
}
