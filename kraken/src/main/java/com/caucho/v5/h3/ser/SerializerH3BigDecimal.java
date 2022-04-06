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
 * @author Alex Rojkov
 */

package com.caucho.v5.h3.ser;

import com.caucho.v5.h3.OutH3;
import com.caucho.v5.h3.io.ConstH3;
import com.caucho.v5.h3.io.InH3Amp;
import com.caucho.v5.h3.io.InRawH3;
import com.caucho.v5.h3.io.OutRawH3;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public class SerializerH3BigDecimal extends SerializerH3Base<BigDecimal>
{
  @Override
  public int typeSequence()
  {
    return ConstH3.DEF_BIGDECIMAL;
  }

  @Override
  public Type type()
  {
    return BigDecimal.class;
  }

  @Override
  public BigDecimal readObject(InRawH3 is, InH3Amp in)
  {
    byte[] bytes = is.readBinary();

    BigInteger bigInteger = new BigInteger(bytes);

    int scale = (int) is.readLong();

    BigDecimal result = new BigDecimal(bigInteger, scale);

    return result;
  }

  @Override
  public void skip(InRawH3 is, InH3Amp in)
  {
    is.skip(in);
    is.skip(in);
  }

  @Override
  public void writeObject(OutRawH3 os,
                          int defId,
                          BigDecimal subject,
                          OutH3 out)
  {
    os.writeObject(typeSequence());

    BigInteger bigInt = subject.unscaledValue();

    byte[] bytes = bigInt.toByteArray();

    int scale = subject.scale();

    os.writeBinary(bytes, 0, bytes.length);
    os.writeLong(scale);
  }
}
