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
 * @author Alex Rojkov
 */

package com.caucho.junit;

import java.util.concurrent.TimeUnit;

import com.caucho.v5.util.CurrentTime;
import com.caucho.v5.util.CurrentTimeTest;

/**
 * Class TestTime is used to control Baratine Clock during the test. This is useful
 * when testing time sensitive behaviour such as time-outs, timers etc.
 *
 * Baratine clock can be fast forwarded using method addTime().
 */
public class TestTime
{
  /**
   * Clears all alarms
   */
  public static void clear()
  {
    CurrentTimeTest.clear();
  }

  /**
   * Sets Baratine time
   * @param time
   */
  public static void setTime(long time)
  {
    CurrentTimeTest.setTime(time);
  }

  /**
   * Adds time to Baratine time
   * @param delta
   * @param timeUnit
   */
  public static void addTime(long delta, TimeUnit timeUnit)
  {
    delta = timeUnit.toMillis(delta);

    long time = CurrentTime.currentTime() + delta;

    CurrentTimeTest.setTime(time);
  }
}
