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

package com.caucho.v5.http.protocol;

import java.io.IOException;
import java.util.Objects;

import com.caucho.v5.io.OutputStreamWithBuffer;
import com.caucho.v5.io.TempBuffer;
import com.caucho.v5.util.L10N;

import io.baratine.io.Buffer;

/**
 * API for handling the output stream.
 */
public abstract class OutHttpApp 
  extends OutputStreamWithBuffer
{
  private static final L10N L = new L10N(OutHttpApp.class);
  
  private static final int SIZE = TempBuffer.SIZE;
  private static final int DEFAULT_SIZE = SIZE;

  private State _state = State.START;
  
  //private final byte []_singleByteBuffer = new byte[1];

  // head of the expandable buffer
  private TempBuffer _tBuf;

  private byte []_buffer;
  private int _offset;
  private int _startOffset;

  // total buffer length
  private int _bufferCapacity;
  // extended buffer length
  private int _bufferSize;

  private long _contentLength;

  //private RequestHttpBase _request;

  //
  // abstract methods
  //
  
  abstract protected void flush(Buffer data, boolean isEnd);

  //
  // state predicates
  //

  /**
   * Set true for HEAD requests.
   */
  public final boolean isHead()
  {
    return _state.isHead();
  }

  /**
   * Test if data has been flushed to the client.
   */
  public boolean isCommitted()
  {
    return _state.isCommitted();
  }
  
  /**
   * Test if the request is closing.
   */
  public boolean isClosing()
  {
    return _state.isClosing();
  }
  
  @Override
  public boolean isClosed()
  {
    return _state.isClosed();
  }
  
  /**
   * Test if the request is closing.
   */
  public boolean isCloseComplete()
  {
    return _state.isClosing();
  }

  public boolean isChunked()
  {
    return false;
  }
  
  /**
   * Initializes the Buffered Response stream at the beginning of a request.
   */
  
  /**
   * Starts the response stream.
   */
  public void start()
  {
    _state = _state.toStart();
    
    _bufferCapacity = DEFAULT_SIZE;
    
    _tBuf = TempBuffer.create();
    
    _buffer = _tBuf.buffer();
    _startOffset = bufferStart();
    _offset = _startOffset;

    _contentLength = 0;
  }

  //
  // implementations
  //
  
  //
  // byte buffer
  //

  /**
   * Returns the byte buffer.
   */
  @Override
  public byte []buffer()
    throws IOException
  {
    return _buffer;
  }

  /**
   * Returns the byte offset.
   */
  @Override
  public int offset()
    throws IOException
  {
    return _offset;
  }

  /**
   * Sets the byte offset.
   */
  @Override
  public void offset(int offset)
    throws IOException
  {
    _offset = offset;
  }

  public long contentLength()
  {
    return _contentLength + _offset - _startOffset;
  }

  /**
   * Writes a byte to the output.
   */
  @Override
  public void write(int ch)
    throws IOException
  {
    if (isClosed() || isHead()) {
      return;
    }
    
    int offset = _offset;
    
    if (SIZE <= offset) {
      flushByteBuffer();
      offset = _offset;
    }

    _buffer[offset++] = (byte) ch;
    _offset = offset;
  }

  /**
   * Writes a chunk of bytes to the stream.
   */
  @Override
  public void write(byte []buffer, int offset, int length)
  {
    if (isClosed() || isHead()) {
      return;
    }

    int byteLength = _offset;
    
    while (true) {
      int sublen = Math.min(length, SIZE - byteLength);

      System.arraycopy(buffer, offset, _buffer, byteLength, sublen);
      offset += sublen;
      length -= sublen;
      byteLength += sublen;
      
      if (length <= 0) {
        break;
      }
      
      _offset = byteLength;
      flushByteBuffer();
      byteLength = _offset;
    }

    _offset = byteLength;
  }

  public void write(Buffer data)
  {
    Objects.requireNonNull(data);
    
    int length = data.length();
    
    TempBuffer tBuf = TempBuffer.create();
    byte []buffer = tBuf.buffer();

    int pos = 0;
    while (pos < length) {
      int sublen = Math.min(length - pos, buffer.length);
      
      data.get(pos, buffer, 0, sublen);
      
      write(buffer, 0, sublen);
      
      pos += sublen;
    }
    
    tBuf.free();
  }

  /**
   * Returns the next byte buffer.
   */
  @Override
  public byte []nextBuffer(int offset)
    throws IOException
  {
    if (offset < 0 || SIZE < offset) {
      throw new IllegalStateException(L.l("Invalid offset: " + offset));
    }
    
    if (_bufferCapacity <= SIZE
        || _bufferCapacity <= offset + _bufferSize) {
      _offset = offset;
      flushByteBuffer();

      return buffer();
    }
    else {
      _tBuf.length(offset);
      _bufferSize += offset;

      TempBuffer tempBuf = TempBuffer.create();
      _tBuf.next(tempBuf);
      _tBuf = tempBuf;

      _buffer = _tBuf.buffer();
      _offset = _startOffset;

      return _buffer;
    }
  }

  protected final void flushByteBuffer()
  {
    flush(false);
  }
  
  protected int bufferStart()
  {
    return 0;
  }

  /**
   * Flushes the buffer.
   */
  @Override
  public void flush()
    throws IOException
  {
    flush(false);
  }
  
  //
  // lifecycle
  //

  /**
   * Set true for HEAD requests.
   */
  public final void toHead()
  {
    _state = _state.toHead();
  }

  /**
   * Sets the committed state
   */
  public void toCommitted()
  {
    _state = _state.toCommitted();
  }

  public void upgrade()
  {
    //_state = _state.toUpgrade();
  }
  
  /**
   * Closes the response stream
   */
  @Override
  public final void close()
    throws IOException
  {
    State state = _state;
    
    if (state.isClosing()) {
      return;
    }
    
    _state = state.toClosing();
    
    try {
      flush(true);
    } finally {
      try {
        _state = _state.toClose();
      } catch (RuntimeException e) {
        throw new RuntimeException(state + ": " + e, e);
      }
    }
  }
  
  /**
   * Flushes the buffered response to the output stream.
   */
  private void flush(boolean isEnd)
  {
    if (_startOffset == _offset && _bufferSize == 0) {
      if (! isCommitted() || isEnd) {
        flush(null, isEnd);
        _startOffset = bufferStart();
        _offset = _startOffset;
      }
      return;
    }

    int sublen = _offset - _startOffset;
    _tBuf.length(_offset);
    _contentLength += _offset - _startOffset;
    _bufferSize = 0;
    
    if (_startOffset > 0) {
      fillChunkHeader(_tBuf, sublen);
    }
    
    flush(_tBuf, isEnd);

    if (! isEnd) {
      _tBuf = TempBuffer.create();
    
      _startOffset = bufferStart();
      _offset = _startOffset;

      _tBuf.length(_offset);
      
      _buffer = _tBuf.buffer();
    }
    else {
      _tBuf = null;
    }
  }
  
  protected void fillChunkHeader(TempBuffer buf, int sublen)
  {
    throw new UnsupportedOperationException();
  }
                                 
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _state + "]";
  }
  
  enum State {
    START {
      State toHead() { return HEAD; }
      State toCommitted() { return COMMITTED; }
      State toClosing() { return CLOSING; }
    },
    HEAD {
      boolean isHead() { return true; }
      
      State toHead() { return this; }
      State toCommitted() { return COMMITTED_HEAD; }
      State toClosing() { return CLOSING_HEAD; }
    },
    COMMITTED {
      boolean isCommitted() { return true; }
      
      State toHead() { return COMMITTED_HEAD; }
      State toCommitted() { return this; }
      State toClosing() { return CLOSING_COMMITTED; }
    },
    COMMITTED_HEAD {
      boolean isCommitted() { return true; }
      boolean isHead() { return true; }
      
      State toHead() { return this; }
      State toCommitted() { return this; }
      State toClosing() { return CLOSING_HEAD_COMMITTED; }
    },
    CLOSING {
      boolean isClosing() { return true; }
      
      State toHead() { return CLOSING_HEAD; }
      State toCommitted() { return CLOSING_COMMITTED; }
      State toClose() { return CLOSED; }
    },
    CLOSING_HEAD {
      boolean isHead() { return true; }
      boolean isClosing() { return true; }
      
      State toHead() { return this; }
      State toCommitted() { return CLOSING_HEAD_COMMITTED; }
      State toClose() { return CLOSED; }
    },
    CLOSING_COMMITTED {
      boolean isCommitted() { return true; }
      boolean isClosing() { return true; }
      
      State toHead() { return CLOSING_HEAD_COMMITTED; }
      State toCommitted() { return this; }
      // State toClosing() { Thread.dumpStack(); return CLOSED; }
      State toClose() { return CLOSED; }
    },
    CLOSING_HEAD_COMMITTED {
      boolean isHead() { return true; }
      boolean isCommitted() { return true; }
      boolean isClosing() { return true; }
      
      State toHead() { return this; }
      State toCommitted() { return this; }
      State toClose() { return CLOSED; }
    },
    CLOSED {
      boolean isCommitted() { return true; }
      boolean isClosed() { return true; }
      boolean isClosing() { return true; }
    };
    
    boolean isHead() { return false; }
    boolean isCommitted() { return false; }
    boolean isClosing() { return false; }
    boolean isClosed() { return false; }
   
    State toStart() { return START; }
    
    State toHead()
    { 
      throw new IllegalStateException(toString());
    }
    
    State toCommitted()
    {
      throw new IllegalStateException(toString());
    }
    
    State toClosing()
    {
      throw new IllegalStateException(toString());
    }
    
    State toClose()
    { 
      throw new IllegalStateException(toString());
    }
  }
}
