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

package com.caucho.v5.amp.marshal;

import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.amp.service.MethodRefBase;
import com.caucho.v5.amp.spi.InboxAmp;
import com.caucho.v5.amp.spi.MethodRefAmp;
import com.caucho.v5.amp.stub.MethodAmp;
import com.caucho.v5.amp.stub.StubAmp;

/**
 * Handles the context for an actor, primarily including its
 * query map.
 */
public class MethodRefImport extends MethodRefBase
{
  private final ServiceRefImport _serviceRef;
  private final String _methodName;
  
  private final MethodRefAmp _delegate;
  
  private final MethodImport _method;
  
  private PodImport _moduleImport;
  
  private Class<?> _retType;

  public MethodRefImport(ServiceRefImport serviceRef,
                         MethodRefAmp delegate,
                         String methodName,
                         Class<?> retType)
  {
    _serviceRef = serviceRef;
    _methodName = methodName;
    
    _delegate = delegate;
    
    _moduleImport = serviceRef.getModuleImport();
    
    _method = new MethodImport(_serviceRef, 
                               delegate.method(), 
                               _retType);

    _retType = retType;
  }
  
  public MethodRefAmp getDelegate()
  {
    MethodRefAmp delegate = _delegate;
    
    if (delegate != null) {
      return delegate;
    }
    else {
      return null;
    }
  }

  @Override
  public ServiceRefAmp serviceRef()
  {
    return _serviceRef;
  }

  @Override
  public String getName()
  {
    return _methodName;
  }
  
  @Override
  public MethodAmp method()
  {
    return _method;
  }

  @Override
  public StubAmp stubActive(StubAmp actorDeliver)
  {
    return getDelegate().stubActive(actorDeliver);
  }

  @Override
  public InboxAmp inbox()
  {
    return getDelegate().inbox();
  }
}
