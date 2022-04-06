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

package com.caucho.v5.json.ser;

import com.caucho.v5.json.io.InJson.Event;
import com.caucho.v5.json.io.JsonReaderImpl;
import com.caucho.v5.json.io.JsonWriterImpl;
import com.caucho.v5.util.L10N;

public class BooleanArraySerializer 
  extends AbstractJsonArraySerializer<boolean[]>
{
  private static final L10N L = new L10N(BooleanArraySerializer.class);
  
  static final BooleanArraySerializer SER = new BooleanArraySerializer();

  private BooleanArraySerializer() {}
  
  @Override
  public void write(JsonWriterImpl out, boolean []value)
  {
    out.writeStartArray();
    
    for (boolean child : value) {
      out.write(child);
    }
    
    out.writeEndArray();
  }

  @Override
  public boolean []read(JsonReaderImpl in)
  {
    Event event = in.next();
    
    if (event == null || event == Event.VALUE_NULL) {
      return null;
    }
    
    if (event != Event.START_ARRAY) {
      throw new JsonException(L.l("expected array at {0}", event));
    }
    
    boolean []values = new boolean[8];
    int i = 0;
    
    while ((event = in.peek()) != Event.END_ARRAY && event != null) {
      values = addValue(values, i++, in.readBoolean());
    }
    
    in.next();

    boolean []result = new boolean[i];
    
    System.arraycopy(values, 0, result, 0, i);
    
    return result;
  }
  
  private boolean []addValue(boolean []values, int i, boolean value)
  {
    if (values.length <= i) {
      boolean []newValues = new boolean[values.length * 2];
      
      System.arraycopy(values, 0, newValues, 0, i);
      
      values = newValues;
    }
    
    values[i] = value;
    
    return values;
  }
}
