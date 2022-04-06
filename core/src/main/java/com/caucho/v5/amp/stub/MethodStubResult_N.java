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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.spi.HeadersAmp;
import com.caucho.v5.inject.type.TypeRef;

import io.baratine.convert.Convert;
import io.baratine.service.Result;
import io.baratine.service.ResultChain;
import io.baratine.service.ServiceException;
import io.baratine.service.ServiceExceptionIllegalArgument;
import io.baratine.stream.ResultStream;

/**
 * Stub for a method returning a Result.
 */
class MethodStubResult_N extends MethodStubBase
{
  private static final Logger log
  = Logger.getLogger(MethodStubResult_N.class.getName());

  private final String _name;
  private final Method _method;
  private final MethodHandle _methodHandle;
  
  private ParameterAmp []_paramTypes;
  private Class<?> _returnType;

  private ClassValue<Convert<?, ?>> _shimMap;

  private ServicesAmp _services;

  //private Class<?>[] _paramTypesCl;

  MethodStubResult_N(ServicesAmp services,
                     Method method)
    throws IllegalAccessException
  {
    super(method);
    
    _services = services;
    
    _method = method;
    _name = method.getName();
    
    _methodHandle = initMethodHandle(services, method);
  }
  
  protected Class<?> getResultClass()
  {
    return ResultChain.class;
  }
  
  protected Method method()
  {
    return _method;
  }
  
  protected MethodHandle methodHandle()
  {
    return _methodHandle;
  }
  
  protected MethodHandle initMethodHandle(ServicesAmp ampManager,
                                          Method method)
    throws IllegalAccessException
  {
    Class<?> []paramTypes = method.getParameterTypes();
    int paramLen = paramTypes.length;
    int resultOffset = findResultOffset(paramTypes);

    method.setAccessible(true);
    MethodHandle mh = MethodHandles.lookup().unreflect(method);
    
    int []permute = new int[paramLen + 1];
    
    permute[0] = 0;
    for (int i = 0; i < resultOffset; i++) {
      permute[i + 1] = i + 2;
    }
    for (int i = resultOffset + 1; i < paramLen; i++) {
      permute[i + 1] = i + 1;
    }
    permute[resultOffset + 1] = 1;
    
    MethodType type = MethodType.genericMethodType(paramLen + 1);
    type = type.changeReturnType(void.class);
    
    mh = mh.asType(type);
    
    mh = filterMethod(ampManager,
                      mh,
                      method);
     
    mh = MethodHandles.permuteArguments(mh, type, permute);
    
    /*
    if (paramLen > 0 && ! Object[].class.equals(paramTypes[paramLen - 1])) {
    }
    */
    mh = mh.asSpreader(Object[].class, paramLen - 1);
    
    type = MethodType.methodType(void.class, 
                                 Object.class,
                                 getResultClass(),
                                 Object[].class);

    return mh.asType(type);
  }
  
  private int findResultOffset(Class<?> []paramTypes)
  {
    Class<?> resultClass = getResultClass();
    
    for (int i = 0; i < paramTypes.length; i++) {
      if (resultClass.isAssignableFrom(paramTypes[i])) {
        return i;
      }
    }
    
    throw new IllegalStateException(String.valueOf(resultClass));
  }
  
  @Override
  public String name()
  {
    return _name;
  }
  
  @Override
  public Annotation []getAnnotations()
  {
    return _method.getAnnotations();
  }
  
  @Override
  public boolean isVarArgs()
  {
    return _method.isVarArgs();
  }
  
  @Override
  public Class<?> getReturnType()
  {
    if (_returnType == null) {
      for (Parameter param : _method.getParameters()) {
        if (ResultChain.class.isAssignableFrom(param.getType())) {
          TypeRef typeRef = TypeRef.of(param.getParameterizedType());
          TypeRef resultRef = typeRef.to(ResultChain.class);
          TypeRef returnRef = resultRef.param(0); 
          
          if (returnRef != null) {
            _returnType = returnRef.rawClass();
          }
          else {
            _returnType = Object.class;
          }
        }
      }
      
      if (_returnType == null) {
        _returnType = Object.class;
      }
    }
    
    return _returnType;
  }
  
  @Override
  public ParameterAmp []parameters()
  {
    if (_paramTypes == null) {
      ParameterAmp []paramClasses = ParameterAmp.of(_method.getParameters()); 
      
      ArrayList<ParameterAmp> paramTypeList = new ArrayList<>();

      for (int i = 0; i < paramClasses.length; i++) {
        if (ResultChain.class.isAssignableFrom(paramClasses[i].rawClass())) {
          continue;
        }
        else if (ResultStream.class.isAssignableFrom(paramClasses[i].rawClass())) {
          continue;
        }
        
        paramTypeList.add(paramClasses[i]);
      }
      
      ParameterAmp[] paramTypes = new ParameterAmp[paramTypeList.size()];
      paramTypeList.toArray(paramTypes);
      
      _paramTypes = paramTypes;
    }
    
    return _paramTypes;
  }

  @Override
  public void send(HeadersAmp headers,
                   StubAmp actor,
                   Object []args)
  {
    Object bean = actor.bean();
    
    if (log.isLoggable(Level.FINEST)) {
      log.finest("amp-send " + _name + "[" + bean + "] " + toList(args));
    }

    try {
      Result<?> result = Result.ignore();
      
      //_methodHandle.invokeExact(bean, cmpl, args);
      _methodHandle.invoke(bean, result, args);
    } catch (Throwable e) {
      log.log(Level.FINER, bean + ": " + e.toString(), e);
    }
  }

  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                    StubAmp actor,
                    Object []args)
  {
    Object bean = actor.bean();

    if (log.isLoggable(Level.FINEST)) {
      log.finest("amp-query " + _name + "[" + bean + "] " + toList(args)
          + "\n  " + result);
    }
    
    try {
      //_methodHandle.invokeExact(bean, result, args);
      _methodHandle.invoke(bean, result, args);
    } catch (ServiceException e) {
      result.fail(e);
    } catch (IllegalArgumentException e) {
      RuntimeException exn = new ServiceExceptionIllegalArgument(bean + "." + _name + ": " + e.getMessage(), e);
      exn.fillInStackTrace();
      
      result.fail(exn);
    } catch (ClassCastException e) {
      RuntimeException exn = new ServiceExceptionIllegalArgument(bean.getClass().getSimpleName() + "." + _name + ": " + e.getMessage(), e);
      exn.fillInStackTrace();
      
      result.fail(exn);
    } catch (ArrayIndexOutOfBoundsException e) {
      if (args.length + 1 != _method.getParameterTypes().length) {
        String msg = bean + "." + _method.getName() + ": " + e.getMessage() + " " + Arrays.asList(args);

        log.log(Level.FINE, msg, e);
        
        RuntimeException exn = new ServiceExceptionIllegalArgument(msg, e);
        
        result.fail(exn);
      }
      else {
        log.log(Level.FINEST, bean + ": " + e.toString(), e);
        
        result.fail(e);
      }
    } catch (Throwable e) {
      log.log(Level.FINEST, bean + ": " + e.toString(), e);

      result.fail(e);
    }
  }
  
  @Override
  public Object shim(Object value)
  {
    if (value == null) {
      return null;
    }
    
    ClassValue<Convert<?,?>> shimMap = shimMap();
    
    Convert convert = shimMap.get(value.getClass());
    
    return convert.convert(value);
  }
  
  private ClassValue<Convert<?,?>> shimMap()
  {
    ClassValue<Convert<?,?>> shimMap = _shimMap;
    
    if (shimMap == null) {
      Class<?> resultType = getReturnType();
      
      shimMap = _shimMap = (ClassValue) _services.shims(resultType);
    }
    
    return shimMap;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(getClass().getSimpleName())
      .append("[").append(_name);
    
    if (_paramTypes != null) {
      for (ParameterAmp param : _paramTypes) {
        sb.append(",").append(param.rawClass().getSimpleName());
      }
    }
    
    sb.append("]");
    
    return sb.toString();
  }
}
