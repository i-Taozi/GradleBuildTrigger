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

import java.lang.reflect.Method;
import java.util.Objects;

import com.caucho.v5.json.io.InJson;
import com.caucho.v5.json.io.JsonReaderImpl;
import com.caucho.v5.json.io.JsonWriterImpl;

class StringValueOfSerializer<T> extends JsonSerializerBase<T>
{
  private Class<T> _type;
  private Method _valueOf;
  
  StringValueOfSerializer(Class<T> type, Method valueOf)
  {
    Objects.requireNonNull(type);
    Objects.requireNonNull(valueOf);
    
    _type = type;
    _valueOf = valueOf;
    
    if (! type.equals(_valueOf.getReturnType())) {
      throw new IllegalArgumentException(_valueOf.toString());
    }
  }
  
  @Override
  public T read(JsonReaderImpl in)
  {
    if (in.peek() == InJson.Event.VALUE_NULL) {
      in.next();
      
      return null;
    }
    else {
      String value = in.readString();
      
      try {
        return (T) _valueOf.invoke(null, value);
      } catch (Exception e) {
        throw new JsonException(e);
      }
    }
  }
  
  @Override
  public void write(JsonWriterImpl out, T value)
  {
    if (value != null) {
      out.write(value.toString());
    }
    else {
      out.writeNull();
    }
  }
}
