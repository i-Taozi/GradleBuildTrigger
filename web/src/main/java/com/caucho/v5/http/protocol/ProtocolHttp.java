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
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.http.protocol;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.caucho.v5.http.container.HttpContainer;
import com.caucho.v5.http.container.HttpSystem;
import com.caucho.v5.network.port.ConnectionProtocol;
import com.caucho.v5.network.port.ConnectionTcp;
import com.caucho.v5.network.port.Protocol;
import com.caucho.v5.subsystem.SystemManager;
import com.caucho.v5.util.CurrentTime;
import com.caucho.v5.util.FreeList;
import com.caucho.v5.util.L10N;
import com.caucho.v5.util.Version;
import com.caucho.v5.web.webapp.RequestBaratineImpl;

/**
 * ProtocolHttp manages the creation of http protocol connections.
 */
public class ProtocolHttp implements Protocol
{
  private final static L10N L = new L10N(ProtocolHttp.class);
  private final static Logger logger
    = Logger.getLogger(ProtocolHttp.class.getName());

  private HttpContainer _http;
  private Protocol _extensionProtocol;
  private SystemManager _systemManager;
  
  private String _serverHeader;
  
  private FreeList<RequestHttp1> _freeRequest = new FreeList<>(512);
  
  private AtomicLong _sequence = new AtomicLong();

  public ProtocolHttp()
  {
    _systemManager = SystemManager.getCurrent();
    
    Objects.requireNonNull(_systemManager);
    
    if (CurrentTime.isTest()) {
      _serverHeader = "Baratine/1.1";
    }
    else {
      _serverHeader = "Baratine/" + Version.getVersion();
    }
  }

  public ProtocolHttp(HttpContainer http)
  {
    Objects.requireNonNull(http);
    //setProtocolName("http");
    
    _http = http;
    
    _serverHeader = http.serverHeader();
  }
  
  @Override
  public String name()
  {
    return "http";
  }
  
  @Override
  public String []nextProtocols()
  {
    return new String[] { "h2", "http/1.1", "http/1.0" };
  }

 /**
   * Create a HttpRequest object for the new thread.
   */
  @Override
  public ConnectionProtocol newConnection(ConnectionTcp connTcp)
  {
    return new ConnectionHttp(this, connTcp, _sequence.incrementAndGet());
  }
  
  public String serverHeader()
  {
    return _serverHeader;
  }

  public void extensionProtocol(Protocol protocol)
  {
    _extensionProtocol = protocol;
  }

  public Protocol extensionProtocol()
  {
    return _extensionProtocol;
  }
  
  public HttpContainer http()
  {
    if (_http != null) {
      return _http;
    }

    HttpSystem httpSystem = _systemManager.getSystem(HttpSystem.class);
    
    if (httpSystem != null) {
      _http = httpSystem.getHttpContainer();
    }
      
    if (_http != null) {
      return _http;
    }
    else {
      return null;
    }
  }

  public ConnectionProtocol newRequest(ConnectionHttp conn)
  {
    HttpContainer http = http();
    
    Objects.requireNonNull(http);
    
    RequestHttp1 request = _freeRequest.allocate();
    
    if (request == null) {
      request = new RequestHttp1(this);
    }

    //request.init(conn);
    
    return new RequestBaratineImpl(conn, request);
  }
  
  protected void requestFree(RequestHttp1 request)
  {
    if (request != null) {
      if (! _freeRequest.free(request)) {
        logger.warning(L.l("Free-Failed: {0}", _freeRequest.size()));
      }
    }
  }
}
