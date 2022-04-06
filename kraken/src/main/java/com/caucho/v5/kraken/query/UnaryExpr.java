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
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.kraken.query;

import com.caucho.v5.kelp.RowCursor;
import com.caucho.v5.kelp.query.ExprBuilderKelp;
import com.caucho.v5.kelp.query.QueryBuilderKelp;
import com.caucho.v5.kelp.query.UnaryOpKelp;

public class UnaryExpr extends ExprKraken
{
  private UnaryOp _op;
  private ExprKraken _expr;

  public UnaryExpr(UnaryOp op,
                   ExprKraken expr)
  {
    _op = op;
    _expr = expr;
  }
  
  //
  // bind
  //
  
  @Override
  public ExprKraken bind(QueryBuilderKraken builder)
  {
    ExprKraken expr = _expr.bind(builder);
    
    if (expr == _expr) {
      return this;
    }
    
    return new UnaryExpr(_op, expr);
  }

  @Override
  public ExprBuilderKelp buildKelp(QueryBuilderKraken builder)
  {
    ExprBuilderKelp expr = _expr.buildKelp(builder);
    
    switch (_op) {
    case MINUS:
      return expr.op(UnaryOpKelp.MINUS);
      
    default:
      throw new IllegalStateException(String.valueOf(_op));
    }
  }

  @Override
  public Object evalObject(RowCursor cursor, Object []args)
  {
    return _op.eval(_expr.evalObject(cursor, args));
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _op + "," + _expr + "]";
  }
}
