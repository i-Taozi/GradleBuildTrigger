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

package com.caucho.v5.kelp.query;

import java.io.IOException;
import java.io.InputStream;

import com.caucho.v5.kelp.Column;
import com.caucho.v5.kelp.RowCursor;

/**
 * Building path values for kelp.
 */
public class PathKelp
{
  private final Column _column;
  
  PathKelp(Column column)
  {
    _column = column;
  }
  
  protected Column getColumn()
  {
    return _column;
  }
  
  void scan(EnvKelp query, Object []values, RowCursor cursor)
  {
  }
  
  void scan(EnvKelp query, Object []values, InputStream is)
      throws IOException
  {
  }

  public PathMapHessian pathMap()
  {
    throw new UnsupportedOperationException(getClass().getName());
  }
            
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _column.name() + "]";
  }
}
