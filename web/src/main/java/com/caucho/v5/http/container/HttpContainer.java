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

package com.caucho.v5.http.container;

import java.io.IOException;

import com.caucho.v5.amp.spi.ShutdownModeAmp;
import com.caucho.v5.http.dispatch.InvocationManager;
import com.caucho.v5.http.protocol.ConnectionHttp;
import com.caucho.v5.http.protocol.HttpBufferStore;
import com.caucho.v5.network.port.ConnectionProtocol;
import com.caucho.v5.web.webapp.RequestBaratine;

import io.baratine.config.Config;

public interface HttpContainer
{
  /**
   * Returns the current http container
   */
  public static HttpContainer current()
  {
    HttpSystem httpSystem = HttpSystem.getCurrent();
    
    if (httpSystem != null) {
      return httpSystem.getHttpContainer();
    }
    else {
      return null;
    }
  }

  int getAccessLogBufferSize();

  boolean isSendfileEnabled();

  long getSendfileMinLength();

  void addSendfileCount();

  InvocationManager<?> getInvocationManager();

  ConnectionProtocol newRequest(ConnectionHttp conn);
  
  HttpBufferStore allocateHttpBuffer();

  void freeHttpBuffer(HttpBufferStore httpBuffer);
  

  boolean isDestroyed();

  String getServerDisplayName();

  String getServerId();

  void sendRequestError(Throwable e,
                        RequestBaratine requestFacade)
                        throws IOException;
  
  Config config();

  boolean isIgnoreClientDisconnect();

  String getURLCharacterEncoding();

  boolean start();

  void shutdown(ShutdownModeAmp mode);

  ClassLoader classLoader();

  String serverHeader();
}
