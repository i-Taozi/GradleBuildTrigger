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

package com.caucho.v5.amp.message;

import com.caucho.v5.amp.spi.InboxAmp;

import io.baratine.stream.ResultStream;

/**
 * Handles the context for an actor, primarily including its
 * query map.
 */
public class ResultStreamJoin<T> extends ResultStream.Wrapper<Object,Object>
{
  private ResultStream<T> _next;
  private final InboxAmp _inbox;
  
  private int _count;
  private int _completeCount;

  private boolean _isStart;

  
  public ResultStreamJoin(ResultStream<T> next, InboxAmp inbox)
  {
    super((ResultStream) next.createJoin());
    
    _next = next;
    //_inbox = next.getInbox();
    _inbox = inbox;
  }
  
  public ResultStream<T> fork()
  {
    _count++;
    _completeCount++;
    
    ResultStream<Object> fork = new ResultStreamFork(this, _inbox);
    
    return _next.createFork(fork);
  }
  
  @Override
  public void start()
  {
    if (! _isStart) {
      _isStart = true;
      next().start();
    }
  }
  
  @Override
  public void accept(Object obj)
  {
    next().accept(obj);
  }
  
  @Override
  public void ok()
  {
    if (--_completeCount <= 0) {
      next().ok();
    }
  }
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + next() + "]";
  }
}
