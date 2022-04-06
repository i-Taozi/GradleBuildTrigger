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

package com.caucho.v5.amp.vault;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.service.ServiceConfig;
import com.caucho.v5.amp.spi.HeadersAmp;
import com.caucho.v5.amp.stub.MethodAmpBase;
import com.caucho.v5.amp.stub.StubAmp;
import com.caucho.v5.amp.stub.StubClass;

import io.baratine.service.OnDelete;
import io.baratine.service.OnLoad;
import io.baratine.service.OnSave;
import io.baratine.service.Result;
import io.baratine.service.ResultChain;
import io.baratine.vault.Id;

/**
 * Stub class for a vault asset
 */
public class StubClassAsset extends StubClass
{
  private static final Logger log
    = Logger.getLogger(StubClassAsset.class.getName());
  
  private VaultConfig<?,?> _configResource;

  public StubClassAsset(ServicesAmp ampManager, 
                        Class<?> type,
                        ServiceConfig configService,
                        VaultConfig<?,?> configResource)
  {
    super(ampManager, type, type);
    
    _configResource = configResource;
  }
  
  @Override
  public void introspect()
  {
    super.introspect();
    
    introspectOnLoad();
  }
  
  private void introspectOnLoad()
  {
    VaultDriver<?,?> driver = _configResource.driver();
    
    if (driver == null || ! driver.isPersistent()) {
      return;
    }
    
    Field idField = findIdField(_configResource.entityType());
    if (idField == null) {
      return;
    }
    
    try {
      idField.setAccessible(true);
    
      MethodHandle idGetter = MethodHandles.lookup().unreflectGetter(idField);
      
      if (! isImplemented(OnLoad.class)) {
        onLoad(new MethodOnLoad(idGetter, driver));
      }
      
      if (! isImplemented(OnSave.class)) {
        onSave(new MethodOnSave(idGetter, driver));
      }
      
      if (! isImplemented(OnDelete.class)) {
        onDelete(new MethodOnDelete(idGetter, driver));
      }
    } catch (Exception e) {
      log.log(Level.WARNING, e.toString(), e);
    }
  }
  
  private Field findIdField(Class<?> entityClass)
  {
    if (entityClass == null) {
      return null;
    }
    
    Field idField = null;
    
    for (Field field : entityClass.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      
      if (field.isAnnotationPresent(Id.class)) {
        return field;
      }
      
      if (field.getName().equals("id") || field.getName().equals("_id")) {
        idField = field;
      }
    }
    
    Field idParent = findIdField(entityClass.getSuperclass());
    
    if (idParent != null && idParent.isAnnotationPresent(Id.class)) {
      return idParent;
    }
    else if (idField != null) {
      return idField;
    }
    else {
      return idParent;
    }
  }
  
  private static class MethodOnLoad extends MethodAmpBase
  {
    private MethodHandle _idGetter;
    private VaultDriver<?,?> _driver;
    
    MethodOnLoad(MethodHandle idGetter,
                 VaultDriver<?,?> driver)
    {
      _idGetter = idGetter;
      _driver = driver;
    }
    
    @Override
    public String name()
    {
      return "@onLoad";
    }
    
    @Override
    public void query(HeadersAmp headers,
                      ResultChain<?> result,
                      StubAmp actor)
    {
      Object bean = actor.bean();
    
      try {
        Serializable id = (Serializable) _idGetter.invoke(bean);
        
        ((VaultDriver) _driver).load(id, bean, result);
      } catch (Throwable e) {
        result.fail(e);
      }
    }
  }
  
  private static class MethodOnSave extends MethodAmpBase
  {
    private MethodHandle _idGetter;
    private VaultDriver<?,?> _driver;
    
    MethodOnSave(MethodHandle idGetter,
                 VaultDriver<?,?> driver)
    {
      _idGetter = idGetter;
      _driver = driver;
    }
    
    @Override
    public String name()
    {
      return "@OnSave";
    }
    
    @Override
    public void query(HeadersAmp headers,
                      ResultChain<?> result,
                      StubAmp stub)
    {
      Object bean = stub.bean();
    
      try {
        Serializable id = (Serializable) _idGetter.invoke(bean);
        
        if (stub.state().isDelete()) {
          ((VaultDriver) _driver).delete(id, bean, Result.ignore());
        }
        else {
          ((VaultDriver) _driver).save(id, bean, Result.ignore());
        }
        
        result.ok(null);
      } catch (Throwable e) {
        result.fail(e);
      }
    }
  }
  
  private static class MethodOnDelete extends MethodAmpBase
  {
    private MethodHandle _idGetter;
    private VaultDriver<?,?> _driver;
    
    MethodOnDelete(MethodHandle idGetter,
                   VaultDriver<?,?> driver)
    {
      _idGetter = idGetter;
      _driver = driver;
    }
    
    @Override
    public String name()
    {
      return "@OnDelete";
    }
    
    @Override
    public void query(HeadersAmp headers,
                      ResultChain<?> result,
                      StubAmp stub)
    {
      Object bean = stub.bean();
    
      try {
        Serializable id = (Serializable) _idGetter.invoke(bean);
        
        ((VaultDriver) _driver).delete(id, bean, Result.ignore());
        
        result.ok(null);
      } catch (Throwable e) {
        result.fail(e);
      }
    }
  }
}
