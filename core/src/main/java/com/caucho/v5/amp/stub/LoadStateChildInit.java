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

import io.baratine.service.Result;

import com.caucho.v5.amp.spi.HeadersAmp;
import com.caucho.v5.amp.spi.InboxAmp;
import com.caucho.v5.amp.spi.StubStateAmp;
import com.caucho.v5.amp.spi.MessageAmp;


/**
 * child actor init state, before the bean is loaded.
 */
public class LoadStateChildInit implements StubStateAmp
{
  private final StubAmpBeanChild _actor;
  
  public LoadStateChildInit(StubAmpBeanChild actor)
  {
    _actor = actor;
  }

  @Override
  public StubStateAmp load(StubAmp actor, 
                        InboxAmp inbox, 
                        MessageAmp msg)
  {
    actor.loadBean();
    
    return this;
  }
  
  @Override
  public void query(StubAmp actorDeliver,
                    StubAmp actorMessage,
                    MethodAmp method, 
                    HeadersAmp headers, 
                    Result<?> result,
                    Object[] args)
  {
    method.query(headers, result, actorMessage, args);
  }

  @Override
  public void onModify(StubAmp actorAmpBase)
  {
    // TODO Auto-generated method stub
    
  }
}
