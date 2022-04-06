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

package com.caucho.v5.kelp;

import com.caucho.v5.util.BitsUtil;
import com.caucho.v5.util.CurrentTime;

/**
 * The state column for a kelp row, recording the sequence and timeout.
 * 
 * state : 2
 * timeout : 30 (timeout in s, 1 billion sec = )
 * time : 48  (time in ms)
 * v_seq : 16
 */
public class ColumnState extends Column
{
  public static final int STATE_MASK = Page.CODE_MASK << 24;
  public static final int STATE_DATA = Page.INSERT << 24;
  public static final int STATE_REMOVED = 0;
  
  public static final int TIME_MASK = ~STATE_MASK;
  public static final long VERSION_MASK = ~0L; // (1L << 48) - 1;
  public static final long VERSION_TIME_SHIFT = 24;
  
  public static final int LENGTH = 12;
  
  public ColumnState(int index,
                     String name,
                     int offset)
  {
    super(index, name, ColumnType.STATE, offset);
  }

  @Override
  public final int length()
  {
    return LENGTH;
  }
  
  public static long state(long value)
  {
    return (value & STATE_MASK);
  }
  
  public static boolean isRemoved(long value)
  {
    return state(value) == STATE_REMOVED;
  }
  
  public static boolean isData(long value)
  {
    return state(value) == STATE_DATA;
  }
  
  public int timeout(byte []rowBuffer, int rowOffset)
  {
    int value = BitsUtil.readInt(rowBuffer, rowOffset + offset());
    
    return value & TIME_MASK;
  }
  
  public void timeout(byte []rowBuffer, int rowOffset, int value)
  {
    value = value & TIME_MASK;
    
    BitsUtil.writeInt(rowBuffer, rowOffset + offset(), value);
  }
  
  public long version(byte []rowBuffer, int rowOffset)
  {
    long value = BitsUtil.readLong(rowBuffer, rowOffset + offset() + 4);
    
    return value & VERSION_MASK;
  }
  
  public void version(byte []rowBuffer, int rowOffset, long value)
  {
    // value = value & VERSION_MASK;
    
    int offset = rowOffset + offset() + 4;
    
    BitsUtil.writeLong(rowBuffer, offset, value);
  }
  
  public long time(byte []rowBuffer, int rowOffset)
  {
    long version = BitsUtil.readLong(rowBuffer, rowOffset + offset() + 4);
    
    // XXX: theoretical rollover issues
    
    return (version >> 24) * 1024;
  }
}
