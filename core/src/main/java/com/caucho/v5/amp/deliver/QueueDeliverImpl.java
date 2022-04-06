/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)(TM)
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

package com.caucho.v5.amp.deliver;

import com.caucho.v5.amp.spi.ShutdownModeAmp;


/**
 * queue with attached workers to process messages.
 */
public final class QueueDeliverImpl<M> // extends MessageOutbox<M>>
  extends QueueDeliverBase<M>
{
  private final WorkerDeliver<M> _worker;
  
  QueueDeliverImpl(QueueRing<M> queue,
                   WorkerDeliver<M> worker)
  {
    super(queue);

    _worker = worker;
  }
  
  /**
   * Returns the head worker in the queue for late queuing.
   */
  /*
  @Override
  public WorkerOutbox<M,C> getWorker()
  {
    return _worker;
  }
  */
  
  @Override
  public boolean isSingleWorker()
  {
    return getQueue().counterGroupSize() == 2;
  } 
  
  @Override
  public boolean wake()
  {
    return _worker.wake();
  }
  
  @Override
  public WorkerDeliver<M> worker()
  {
    return _worker;
  }
  
  @Override
  public void wakeAll()
  {
    _worker.wakeAll();
  }
  
  @Override
  public void wakeAllAndWait()
  {
    _worker.wakeAllAndWait();
  }
  
  @Override
  public void shutdown(ShutdownModeAmp mode)
  {
    super.shutdown(mode);
    
    _worker.shutdown(mode);
  }
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _worker + "]";
  }
}
