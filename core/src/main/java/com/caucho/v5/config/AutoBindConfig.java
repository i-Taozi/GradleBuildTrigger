/*
 * Copyright (c) 1998-2016 Caucho Technology -- all rights reserved
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

package com.caucho.v5.config;

import javax.inject.Provider;

import io.baratine.config.Config;
import io.baratine.config.Config.ConfigBuilder;
import io.baratine.inject.Injector;
import io.baratine.inject.Key;
import io.baratine.inject.Injector.InjectAutoBind;

public class AutoBindConfig implements InjectAutoBind
{
  private ConfigBuilder _config;

  public AutoBindConfig(ConfigBuilder config)
  {
    _config = config;
  }

  @Override
  public <T> Provider<T> provider(Injector manager, Key<T> key) {
    if (! Config.class.equals(key.rawClass())) {
      return null;
    }

    return () -> (T) _config.get();
  }
}
