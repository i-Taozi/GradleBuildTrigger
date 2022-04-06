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

package com.caucho.v5.bartender.proc;

import java.io.IOException;
import java.nio.file.Path;

import com.caucho.v5.baratine.ServiceApi;
import com.caucho.v5.io.WriteStream;
import com.caucho.v5.kelp.DebugKelp;
import com.caucho.v5.kraken.KrakenSystem;
import com.caucho.v5.kraken.table.TableKraken;
import com.caucho.v5.kraken.table.KrakenImpl;

import io.baratine.files.BfsFileSync;

/**
 * Entry to the filesystem.
 */
@ServiceApi(BfsFileSync.class)
public class ProcKrakenDebugTable extends ProcFileBase
{
  private KrakenImpl _tableManager;
  private String _tableName;

  public ProcKrakenDebugTable(String tableName)
  {
    super("/kraken/pages");
    
    _tableManager = KrakenSystem.current().getTableManager();
    _tableName = tableName;
  }
  
  @Override
  protected boolean fillRead(WriteStream out)
    throws IOException
  {
    TableKraken table = _tableManager.getTable(_tableName);
    
    if (table == null) {
      return false;
    }
    
    Path path = _tableManager.getStorePath();
    
    out.println("\"");
    
    new DebugKelp().debug(out, path, table.getTableKey());

    out.println("\"");
    
    return true;
  }
}
