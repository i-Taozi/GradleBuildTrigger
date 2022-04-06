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

package com.caucho.v5.tcp;

import java.io.IOException;
import java.net.Socket;

import com.caucho.v5.io.ReadStream;
import com.caucho.v5.io.SocketBar;
import com.caucho.v5.io.SocketSystem;
import com.caucho.v5.io.SocketWrapperBar;
import com.caucho.v5.io.StreamImpl;
import com.caucho.v5.io.WriteStream;

/**
 * Simplified tcp connection for testing.
 */
public class TcpConnection
{
  private SocketBar _socket;

  private ReadStream _is;
  private WriteStream _os;

  private TcpConnection(SocketBar socket)
    throws IOException
  {
    _socket = socket;
    StreamImpl stream = _socket.stream();

    _is = new ReadStream(stream);
    _os = new WriteStream(stream);
  }

  public static TcpConnection open(String address,
                                   int port)
    throws IOException
  {
    SocketSystem socketSystem = SocketSystem.current();

    if (socketSystem != null) {
      SocketBar socket = socketSystem.connect(address, port);

      return new TcpConnection(socket);
    }
    else {
      Socket socket = new Socket(address, port);

      SocketBar qSocket = new SocketWrapperBar(socket);

      return new TcpConnection(qSocket);
    }
  }

  public void timeout(long timeout)
    throws IOException
  {
    _socket.setSoTimeout(timeout);
  }

  public ReadStream inputStream()
  {
    return _is;
  }

  public int readBytes(byte[] buffer)
    throws IOException
  {
    int i = 0;

    int ch = _is.read();

    if (ch < 0) {
      return 0;
    }

    buffer[i++] = (byte) ch;

    while (i < buffer.length
           && _is.available() > 0
           && (ch = _is.read()) >= 0) {
      buffer[i++] = (byte) ch;
    }

    return i;
  }

  public String read(int length)
    throws IOException
  {
    // XXX: encoding
    StringBuilder sb = new StringBuilder();

    int ch = _is.read();
    if (ch < 0) {
      return null;
    }

    sb.append((char) ch);
    length--;

    while (length-- > 0
           && _is.available() > 0
           && ((ch = _is.read()) >= 0)) {
      sb.append((char) ch);
    }

    return sb.toString();
  }

  public WriteStream outputStream()
  {
    return _os;
  }

  public void write(int ch)
    throws IOException
  {
    _os.write(ch);
  }

  public void write(byte []buffer, int offset, int length)
    throws IOException
  {
    _os.write(buffer, offset, length);
  }

  public void print(String data)
    throws IOException
  {
    _os.print(data);
  }

  public void flush()
    throws IOException
  {
    _os.flush();
  }

  public void close()
    throws IOException
  {
    try {
      _is.close();
      _os.close();
    } finally {
      _socket.close();
    }
  }
}
