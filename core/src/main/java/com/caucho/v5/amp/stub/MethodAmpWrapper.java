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

import java.lang.annotation.Annotation;

import com.caucho.v5.amp.spi.HeadersAmp;
import com.caucho.v5.amp.spi.StubContainerAmp;

import io.baratine.service.ResultChain;
import io.baratine.stream.ResultStream;

/**
 * Abstract stream for an actor.
 */
abstract public class MethodAmpWrapper extends MethodAmpBase 
{
  abstract protected MethodAmp delegate();
  
  @Override
  public String name()
  {
    MethodAmp delegate = delegate();
    
    if (delegate != null) {
      return delegate.name();
    }
    else {
      return "null:" + getClass().getSimpleName();
    }
  }
  
  @Override
  public boolean isClosed()
  {
    return delegate().isClosed();
  }
  
  @Override
  public boolean isDirect()
  {
    MethodAmp delegate = delegate();
    
    if (delegate != null) {
      return delegate.isDirect();
    }
    else {
      return false;
    }
  }
  
  @Override
  public boolean isModify()
  {
    return delegate().isModify();
  }
  
  @Override
  public Class<?> declaringClass()
  {
    return delegate().declaringClass();
  }
  
  @Override
  public Annotation [] getAnnotations()
  {
    return delegate().getAnnotations();
  }
  
  @Override
  public ParameterAmp[] parameters()
  {
    return delegate().parameters();
  }
  
  //
  // lifecycle methods
  //
  
  @Override
  public void onActive(StubContainerAmp container)
  {
    delegate().onActive(container);
  }
  
  //
  // send methods
  //
  
  @Override
  public void send(HeadersAmp headers,
                   StubAmp stub)
  {
    delegate().send(headers, stub);
  }

  @Override
  public void send(HeadersAmp headers,
                   StubAmp stub,
                   Object arg1)
  {
    delegate().send(headers, stub, arg1);
  }

  @Override
  public void send(HeadersAmp headers,
                   StubAmp stub,
                   Object arg1, 
                   Object arg2)
  {
    delegate().send(headers, stub, arg1, arg2);
  }

  @Override
  public void send(HeadersAmp headers,
                   StubAmp stub,
                   Object arg1,
                   Object arg2, 
                   Object arg3)
  {
    delegate().send(headers, stub, arg1, arg2, arg3);
  }

  @Override
  public void send(HeadersAmp headers,
                   StubAmp stub,
                   Object []args)
  {
    delegate().send(headers, stub, args);
  }
  
  //
  // query methods
  //
  
  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                    StubAmp stub)
  {
    delegate().query(headers, result, stub);
  }
  
  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                     StubAmp stub,
                     Object arg1)
  {
    delegate().query(headers, result, stub, arg1);
  }
  
  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                    StubAmp stub,
                    Object arg1,
                    Object arg2)
  {
    delegate().query(headers, result, stub, arg1, arg2);
  }
  
  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                    StubAmp stub,
                    Object arg1,
                    Object arg2,
                    Object arg3)
  {
    delegate().query(headers, result, stub, arg1, arg2, arg3);
  }
  
  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                    StubAmp stub,
                    Object []args)
  {
    delegate().query(headers, result, stub, args);
  }
  
  //
  // stream methods
  //

  @Override
  public <T> void stream(HeadersAmp headers,
                           ResultStream<T> result,
                           StubAmp actor,
                           Object []args)
  {
    delegate().stream(headers, result, actor, args);
  }
  
  @Override
  public int hashCode()
  {
    return delegate().hashCode();
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (! (obj instanceof MethodAmp)) {
      return false;
    }
    
    if (obj instanceof MethodAmpWrapper) {
      MethodAmpWrapper wrapper = (MethodAmpWrapper) obj;
      
      return delegate().equals(wrapper.delegate());
    }
    
    return delegate().equals(obj);
  }

  @Override
  public String toString()
  {
    return (getClass().getSimpleName()
            + "[" + delegate()
            + "]");
  }
}
