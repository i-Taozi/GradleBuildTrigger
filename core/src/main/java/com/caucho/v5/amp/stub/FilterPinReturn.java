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

import com.caucho.v5.amp.Amp;
import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.amp.proxy.ProxyHandleAmp;
import com.caucho.v5.amp.spi.InboxAmp;

/**
 * Filters a return value, pinning it to the called service.
 */
public class FilterPinReturn
{
  private final ServicesAmp _rampManager;
  private final Class<?> _api;

  FilterPinReturn(ServicesAmp rampManager,
                        Class<?> api)
  {
    _rampManager = rampManager;
    _api = api;
  }

  public Object filter(Object value)
  {
    if (value == null) {
      return null;
    }

    if (value instanceof ProxyHandleAmp) {
      return value;
    }
    
    InboxAmp caller = Amp.getCurrentInbox();
    
    InboxAmp inbox;
    
    if (caller != null) {
      inbox = caller;
    }
    else {
      inbox = _rampManager.inboxSystem();
    }
    
    ServiceRefAmp serviceRef = inbox.serviceRef().pin(value);
    
    return serviceRef.as(_api);
  }
}
