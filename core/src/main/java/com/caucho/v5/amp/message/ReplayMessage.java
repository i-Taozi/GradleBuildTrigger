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

import java.util.logging.Logger;

import com.caucho.v5.amp.deliver.QueueDeliver;
import com.caucho.v5.amp.spi.InboxAmp;
import com.caucho.v5.amp.spi.MessageAmp;
import com.caucho.v5.amp.stub.StubAmp;

/**
 * Message to shut down an instance.
 */
public class ReplayMessage extends MessageAmpBase
{
  private static final Logger log
    = Logger.getLogger(ReplayMessage.class.getName());
  
  private InboxAmp _inbox;
  private QueueDeliver<MessageAmp> _queue;
  private Result<Boolean> _result;

  public ReplayMessage(InboxAmp mailbox,
                       QueueDeliver<MessageAmp> queue,
                       Result<Boolean> result)
  {
    _inbox = mailbox;
    _queue = queue;
    _result = result;
  }
  
  @Override
  public InboxAmp inboxTarget()
  {
    return _inbox;
  }
  
  @Override
  public void invoke(InboxAmp inbox, StubAmp actor)
  {
    try {
      actor.replay(inbox, _queue, _result);
      
      /*
      if (actor instanceof RampJournalActor) {
      _journal.replayStart(this, _queue);
      }
      */
    } catch (Throwable e) {
      e.printStackTrace();
      _result.fail(e);
    }
  }
  
  public void offer()
  {
    long timeout = InboxAmp.TIMEOUT_INFINITY;
    
    inboxTarget().offerAndWake(this, timeout);
  }
}
