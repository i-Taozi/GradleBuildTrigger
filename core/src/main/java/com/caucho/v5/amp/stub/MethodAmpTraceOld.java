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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.spi.HeadersAmp;

import io.baratine.service.ResultChain;

/**
 * debugging messages
 */
public class MethodAmpTraceOld extends MethodAmpBase
{
  private static final Logger log
    = Logger.getLogger(MethodAmpTraceOld.class.getName());
  
  private String _methodName;
  
  public MethodAmpTraceOld(String methodName)
  {
    _methodName = methodName;
  }
  
  @Override
  public void send(HeadersAmp headers,
                   StubAmp actor,
                   Object []args)
  {
    if (headers != null
        && headers.getSize() > 0
        && log.isLoggable(Level.FINE)) {
      Object traceId = headers.get("trace.id");
      
      log.fine("send {id=" + traceId + "} " + actor.name() + " " + _methodName + Arrays.asList(args));
    }
  }
  
  @Override
  public void query(HeadersAmp headers,
                    ResultChain<?> result,
                    StubAmp actor,
                    Object []args)
  {
    if (headers != null
        && headers.getSize() > 0
        && log.isLoggable(Level.FINE)) {
      Object traceId = headers.get("trace.id");
      
      log.fine("query {id=" + traceId + "} " + actor.name() + " " + _methodName + Arrays.asList(args));
    }
  }
}
