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

package com.caucho.v5.amp.journal;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.deliver.Deliver;
import com.caucho.v5.amp.deliver.Outbox;
import com.caucho.v5.amp.inbox.InboxQueue;
import com.caucho.v5.amp.spi.MessageAmp;
import com.caucho.v5.amp.stub.StubAmp;

import io.baratine.service.ServiceException;

/**
 * Worker for a queue inbox. 
 */
public class DeliverJournal implements Deliver<MessageAmp>
{
  private static final Logger log
    = Logger.getLogger(DeliverJournal.class.getName());
  
  private final InboxQueue _inbox;
  private final StubAmp _actorDeliver;

  public DeliverJournal(StubAmp actor,
                        InboxQueue inbox)
  {
    Objects.requireNonNull(actor);
    Objects.requireNonNull(inbox);
    
    _inbox = inbox;
    _actorDeliver = actor;
  }

  @Override
  public final void deliver(final MessageAmp msg, 
                            Outbox outbox)
      throws Exception
  {
    try {
      msg.invoke(_inbox, _actorDeliver);
    } catch (ServiceException e) {
      log.fine(e.toString());
    } catch (Throwable e) {
      log.log(Level.WARNING, this + " " + e.toString(), e);
    }
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _actorDeliver + "]";
  }
}
