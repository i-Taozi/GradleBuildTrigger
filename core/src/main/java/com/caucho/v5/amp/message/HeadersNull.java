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

package com.caucho.v5.amp.message;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import com.caucho.v5.amp.spi.HeadersAmp;

/**
 * Null header implementation.
 */
public final class HeadersNull extends HeadersRampBase
{
  public static final HeadersNull NULL = new HeadersNull();
  
  private HeadersNull()
  {
  }
  
  @Override 
  public int getSize()
  {
    return 0;
  }
  
  @Override
  public Object get(String key)
  {
    return null;
  }

  @Override
  public Iterator<Entry<String, Object>> iterator()
  {
    return Collections.emptyIterator();
  }
  
  @Override
  public HeadersAmp add(String key, Object value)
  {
    return new HeadersCons(key, value, null);
  }
  
  @Override
  public String toString()
  {
    return "{}";
  }
}
