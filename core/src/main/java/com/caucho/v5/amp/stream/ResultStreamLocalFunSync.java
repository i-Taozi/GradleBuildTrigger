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

package com.caucho.v5.amp.stream;

import io.baratine.function.BiFunctionSync;
import io.baratine.stream.ResultStream;

import java.util.Objects;

@SuppressWarnings("serial")
public class ResultStreamLocalFunSync<T> extends ResultStreamLocalBase<T,T>
{
  private BiFunctionSync<T,T,T> _fun;

  private boolean _isFirst = true;
  private T _value;
  
  public ResultStreamLocalFunSync(ResultStream<? super T> result,
                                  BiFunctionSync<T,T,T> combiner)
  {
    super(result);
    
    Objects.requireNonNull(combiner);

    _fun = combiner;
  }

  @Override
  public void accept(T value)
  {
    if (_isFirst) {
      _isFirst = false;
      _value = value;
    }
    else {
      _value = _fun.apply(_value, value);
    }
  }

  @Override
  public void ok()
  {
    if (! _isFirst) {
      next().accept(_value);
    }
    
    next().ok();
  }
}
