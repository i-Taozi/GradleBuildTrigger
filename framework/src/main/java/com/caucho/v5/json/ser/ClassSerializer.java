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

public class ClassSerializer extends JsonSerializerBase<Class<?>>
{
  static final ClassSerializer SER = new ClassSerializer();

  private ClassSerializer() {}

  @Override
  public void write(JsonWriterImpl out, Class<?> value)
  {
    //out.write(QDate.formatISO8601(value.getTime()));
    out.write(value.getName());
  }
  
  @Override
  public Class<?> read(JsonReaderImpl in)
  {
    try {
      Event event = in.peek();
      
      switch (in.peek()) {
      case VALUE_NULL:
        return null;
      
      case VALUE_STRING:
        //return new Date(new QDate().parseDate(in.readString()));
        return null;

      default:
        throw error("Unexpected JSON {0} while parsing Date", event);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new JsonException(e);
    }
  }
}
