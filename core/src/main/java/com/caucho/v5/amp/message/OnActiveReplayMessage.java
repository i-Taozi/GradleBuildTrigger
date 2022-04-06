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

import io.baratine.service.Result;

import com.caucho.v5.amp.spi.InboxAmp;
import com.caucho.v5.amp.stub.StubAmp;

/**
 * Message after a replay to transition to normal operation.
 */
public class OnActiveReplayMessage extends MessageAmpBase
{
  private InboxAmp _inbox;
  private StubAmp _stubTop;

  public OnActiveReplayMessage(InboxAmp inbox, 
                               StubAmp stubTop,
                               boolean isSingle)
  {
    _inbox = inbox;
    _stubTop = stubTop;
  }
  
  @Override
  public InboxAmp inboxTarget()
  {
    return _inbox;
  }
  
  @Override
  public void invoke(InboxAmp inbox, StubAmp stubDeliver)
  {
    StubAmp stub = stubDeliver.worker(_stubTop);
    
      //actor.load(this).onActive(this);
    try {
      stub.state().onActive(stub, inbox);
      
      //System.out.println("AREP: " + actor + " " + actor.loadReplay(this));
    } catch (Exception e) {
      e.printStackTrace();
      
      throw e;
    }
      /*
      if (_isSingle) {
        actor.onActive();
      }
      */
    
    /*
    if (stubDeliver.isMain()) {
      OnSaveMessage onSave
        = new OnSaveMessage(_inbox, _stubTop, Result.ignore());
    
      long timeout = 1000;
    
      onSave.offer(timeout);
    }
    */
  }
  
  public void offer()
  {
    long timeout = InboxAmp.TIMEOUT_INFINITY;
    
    inboxTarget().offerAndWake(this, timeout);
  }
}
