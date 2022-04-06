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
 * @author Nam Nguyen
 */

package com.caucho.v5.cli.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import com.caucho.v5.baratine.client.ServiceManagerClient;
import com.caucho.v5.bartender.files.FilesDeployService;
import com.caucho.v5.cli.baratine.ArgsCli;
import com.caucho.v5.health.shutdown.ExitCode;
import com.caucho.v5.util.L10N;
import com.caucho.v5.vfs.StreamSourceInputStream;

public class BfsTouchCommand extends BfsCommand
{
  private static final L10N L = new L10N(BfsTouchCommand.class);

  public String name()
  {
    return "touch";
  }

  @Override
  public String getDescription()
  {
    return "create a zero-lengthed file";
  }

  @Override
  public String getUsageTailArgs()
  {
    return " file ...";
  }

  @Override
  public int getTailArgsMinCount()
  {
    return 1;
  }

  @Override
  protected void initBootOptions()
  {
    addFlagOption("force", "overwrite file if file already exists").tiny("f");

    super.initBootOptions();
  }

  @Override
  protected ExitCode doCommandImpl(ArgsCli args,
                               ServiceManagerClient client)
  {
    FilesDeployService service = client.service("remote:///bartender-files")
                                       .as(FilesDeployService.class);

    ArrayList<String> argList = args.getTailArgs();

    boolean isOverwrite = args.getArgFlag("force");

    ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
    StreamSourceInputStream stream = new StreamSourceInputStream(bis);

    for (String path : argList) {
      boolean isSuccessful = service.putFile(path, stream);

      if (! isSuccessful) {
        System.out.println(L.l("unable to create file: {0}", path));
      }
    }

    return ExitCode.OK;
  }
}
