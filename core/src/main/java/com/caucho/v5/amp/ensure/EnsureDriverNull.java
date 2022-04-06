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

package com.caucho.v5.amp.ensure;

import java.util.logging.Logger;

import com.caucho.v5.amp.stub.MethodAmp;
import com.caucho.v5.util.L10N;

/**
 * Driver for the @Ensure reliable message annotation.
 */
public class EnsureDriverNull implements EnsureDriverAmp
{
  private static final L10N L = new L10N(EnsureDriverNull.class);
  private static final Logger log
  = Logger.getLogger(EnsureDriverNull.class.getName());
  
  @Override
  public MethodEnsureAmp ensure(MethodAmp method)
  {
    log.warning(L.l("@Ensure method '{0}' with null driver",
                    method));
    
    return new MethodEnsureNull(method);
  }
}
