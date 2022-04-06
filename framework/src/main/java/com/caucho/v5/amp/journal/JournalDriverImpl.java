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

import io.baratine.service.ServiceException;

import com.caucho.v5.bartender.journal.JournalSystem;
import com.caucho.v5.db.journal.JournalStream;
import com.caucho.v5.util.L10N;

/**
 * Factory for opening and restoring journals.
 */
public class JournalDriverImpl implements JournalDriverAmp
{
  private static final L10N L = new L10N(JournalDriverImpl.class);
  
  //private int _maxCount = -1;
  //private long _timeout = -1;
  
  /*
  @Override
  public void setMaxCount(int maxCount)
  {
    _maxCount = maxCount;
  }
  
  @Override
  public void setDelay(long timeout)
  {
    _timeout = timeout;
  }
  
  @Override
  public long getDelay()
  {
    return _timeout;
  }
  */
  
  @Override
  public JournalAmp open(String name) // , int maxCount, long timeout)
  {
    JournalSystem system = JournalSystem.getCurrent();
    
    /*
    if (maxCount < 0) {
      maxCount = _maxCount;
    }
    
    if (timeout < 0) {
      timeout = _timeout;
    }
    */
    
    if (system != null) {
      return new JournalImpl(system.openJournal(name)); // , maxCount, timeout);
    }
    else {
      throw new ServiceException(L.l("Journals are not supported on this system."));
    }
  }
  
  @Override
  public JournalAmp openPeer(String name, String peerName)
  {
    JournalSystem system = JournalSystem.getCurrent();
    JournalStream peerJournal = null;

    if (system != null) {
      peerJournal = system.openPeerJournal(name, peerName);
    }
    
    if (peerJournal != null) {  
      return new JournalImpl(peerJournal); // , -1, -1);
    }
    else {
      throw new ServiceException(L.l("Peer journals are not supported on this system."));
    }
  }
}
