/*
 * Copyright (c) 2001-2016 Caucho Technology, Inc.  All rights reserved.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.h3.ser;

import com.caucho.v5.h3.OutH3;
import com.caucho.v5.h3.io.ConstH3;
import com.caucho.v5.h3.io.InH3Amp;
import com.caucho.v5.h3.io.InRawH3;
import com.caucho.v5.h3.io.OutRawH3;

/**
 * byte[] serializer.
 */
public final class SerializerH3ArrayChar extends SerializerH3ValueBase<char[]>
{
  SerializerH3ArrayChar()
  {
    super(char[].class);
  }
  
  @Override
  public int typeSequence()
  {
    //return ConstH3.DEF_ARRAY_CHAR;
    return ConstH3.DEF_CHAR_ARRAY;
  }

  @Override
  public char []readObject(InRawH3 is, InH3Amp in)
  {
    // XXX:
    return is.readString().toCharArray();
  }

  @Override
  public void writeObject(OutRawH3 os, int defId, char []value, OutH3 out)
  {
    //os.writeString(value, 0, value.length);
    os.writeString(new String(value, 0, value.length));
  }
}
