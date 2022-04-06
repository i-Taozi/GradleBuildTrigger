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
 * @author Nam Nguyen
 */

package com.caucho.v5.bartender.files.provider;

import io.baratine.files.BfsFileSync;
import io.baratine.files.Status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import com.caucho.v5.util.L10N;

public class JInputByteChannel implements SeekableByteChannel
{
  private static final L10N L = new L10N(JInputByteChannel.class);

  private final BfsFileSync _file;
  private InputStream _is;
  private long _position;

  public JInputByteChannel(BfsFileSync file, InputStream is)
  {
    _file = file;
    _is = is;
  }

  @Override
  public boolean isOpen()
  {
    return _is != null;
  }

  @Override
  public void close() throws IOException
  {
    InputStream is = _is;
    _is = null;

    if (is != null) {
      is.close();
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException
  {
    if (_is == null) {
      return -1;
    }

    byte []buffer = new byte[1024];

    int remaining = dst.remaining();

    int total = 0;

    while (remaining > 0) {
      int toRead = Math.min(remaining, buffer.length);

      int len = _is.read(buffer, 0, toRead);

      if (len < 0) {
        break;
      }

      dst.put(buffer, 0, len);

      total += len;
    }

    return total;
  }

  @Override
  public int write(ByteBuffer src) throws IOException
  {
    throw new IOException(L.l("cannot write to a read channel"));
  }

  @Override
  public long position() throws IOException
  {
    return _position;
  }

  @Override
  public SeekableByteChannel position(long newPosition) throws IOException
  {
    if (_is == null) {
      throw new IOException(L.l("channel is closed"));
    }

    if (newPosition < _position) {
      throw new IOException(L.l("cannot seek backwards, current position={0}, requested position={1}",
                                _position, newPosition));
    }

    byte[] buffer = new byte[1024];

    while (_position != newPosition) {
      long diff = newPosition - _position;

      int toRead = (int) Math.min(buffer.length, diff);

      int len = _is.read(buffer, 0, toRead);

      if (len < 0) {
        break;
      }

      _position += len;
    }

    return this;
  }

  @Override
  public long size() throws IOException
  {
    Status status = _file.getStatus();

    return status.getLength();
  }

  @Override
  public SeekableByteChannel truncate(long size) throws IOException
  {
    throw new IOException(L.l("cannot truncate a read channel"));
  }

}
