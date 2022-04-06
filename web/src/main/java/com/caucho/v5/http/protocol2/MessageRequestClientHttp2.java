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

package com.caucho.v5.http.protocol2;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import com.caucho.v5.io.WriteStream;

/**
 * InputStreamHttp reads a single HTTP frame.
 */
public class MessageRequestClientHttp2 extends MessageHttp
{
  private static final Logger log
    = Logger.getLogger(MessageRequestClientHttp2.class.getName());
  
  private String _method;
  private String _path;
  private String _host;
  private Map<String,String> _headers;
  private InRequestClient _request;
  private FlagsHttp _flags;

  public MessageRequestClientHttp2(String method,
                                   String host,
                                   String path,
                                   Map<String,String> headers,
                                   InRequestClient request,
                                   FlagsHttp flags)
  {
    _method = method;
    _path = path;
    _host = host;
    _headers = headers;
    _request = request;
    _flags = flags;
  }
  
  protected FlagsHttp getFlagsHttp()
  {
    return _flags;
  }
  
  /**
   * Deliver the message
   * 
   * @param os the physical output stream
   * @param writerHttp the writer context
   */
  @Override
  public void deliver(WriteStream os, OutHttp2 outHttp)
    throws IOException
  {
    // OutChannelHttp2 stream = _request.getStreamOut();
    
    int streamId = outHttp.nextStream(_request.channel());
    
    // http/1220
    _request.init(streamId);

    OutHeader outHeader = outHttp.getOutHeader();
    
    outHeader.openHeaders(streamId, getFlagsHttp());
    
    outHeader.header(":method", _method);
    outHeader.header(":scheme", "http");
    outHeader.header(":authority", _host);
    outHeader.header(":path", _path);
    
    if (_headers != null) {
      for (Map.Entry<String,String> entry : _headers.entrySet()) {
        outHeader.header(entry.getKey(), entry.getValue());
      }
    }
    
    outHeader.closeHeaders();
    
    if (getFlagsHttp() == FlagsHttp.END_STREAM) {
      // stream.closeWrite();
      _request.channel().closeWrite();
      // outHttp.closeWrite(streamId);
    }
  }
}
