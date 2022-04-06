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

package com.caucho.v5.amp.spi;

import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.amp.stub.MethodAmp;
import com.caucho.v5.amp.stub.StubAmp;
import com.caucho.v5.amp.stub.StubAmpBean;

import io.baratine.service.Result;
import io.baratine.service.ResultChain;
import io.baratine.service.ServiceRef;

/**
 * Lifecycle container for stubs
 */
public interface StubContainerAmp
{
  ServiceRef addService(String path, ServiceRef serviceRef);

  ServiceRef getService(String path);

  void addModifiedChild(StubAmp stub);

  void afterBatch(StubAmp stub);
  
  void onSave(Result<Void> result);

  boolean isJournalReplay();

  void onActive();

  boolean isModifiedChild(StubAmp actor);

  String getChildPath(String path);

  void onLruModified(ServiceRefAmp serviceRef);

  ResultChain<?> ensure(StubAmpBean stub, 
                        MethodAmp method,
                        ResultChain<?> result, 
                        Object[] args);

  void onActiveEnsure(MethodAmp method);

  default boolean isAutoStart()
  {
    return false;
  }
}
