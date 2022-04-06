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

package com.caucho.v5.ramp.compute;

import io.baratine.service.Result;
import io.baratine.service.Services;

import java.util.function.Supplier;

import com.caucho.v5.amp.ServicesAmp;

/**
 * The task manager has an embedded service manager to keep it isolated
 * from the rest of baratine.
 */
public class TaskManager
{
  private Services _ampManager;
  private TaskService _taskService;
  private int _workers;
  
  public TaskManager()
  {
    int workers = Runtime.getRuntime().availableProcessors();
    
    if (workers <= 0) {
      workers = 1;
    }
    
    _workers = workers;
    
    Thread thread = Thread.currentThread();
    ClassLoader oldLoader = thread.getContextClassLoader();
    
    try {
      thread.setContextClassLoader(TaskManager.class.getClassLoader());
      
      _ampManager = ServicesAmp.newManager().start();
      
      _taskService = _ampManager.newService(TaskService.class,
                                            TaskServiceImpl::new)
                                .workers(workers)
                                .as(TaskService.class);
    } finally {
      thread.setContextClassLoader(oldLoader);
    }
  }
  
  public int getWorkers()
  {
    return _workers;
  }
  
  public void run(Runnable task, Result<Void> result)
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    _taskService.run(task, loader, result);
  }
  
  public <T> void execute(Supplier<T> task, Result<T> result)
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    _taskService.execute(task, loader, result);
  }
  
  public <T> void execute(Supplier<T> task, ClassLoader loader, Result<T> result)
  {
    _taskService.execute(task, loader, result);
  }
  
  public void run(Runnable task)
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    _taskService.run(task, loader, Result.ignore());
  }

  /*
  public void run(Runnable task, Result<Void> result)
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    _taskService.run(task, loader, result);
  }
  */
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[]";
  }
}
