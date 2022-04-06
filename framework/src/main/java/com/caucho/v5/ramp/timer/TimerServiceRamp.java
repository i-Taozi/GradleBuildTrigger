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

package com.caucho.v5.ramp.timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongUnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.Direct;
import com.caucho.v5.config.types.CronType;
import com.caucho.v5.lifecycle.Lifecycle;
import com.caucho.v5.util.Alarm;
import com.caucho.v5.util.AlarmListener;
import com.caucho.v5.util.CurrentTime;

import io.baratine.service.Cancel;
import io.baratine.service.OnDestroy;
import io.baratine.service.OnInit;
import io.baratine.service.OnLookup;
import io.baratine.service.Pin;
import io.baratine.service.Result;
import io.baratine.service.Service;
import io.baratine.service.ServiceRef;
import io.baratine.timer.Timers;

/**
 * Timer service.
 */
@Service
public class TimerServiceRamp implements TimerServiceAmp
{
  private static final Logger log
    = Logger.getLogger(TimerServiceRamp.class.getName());
  
  private final Lifecycle _lifecycle = new Lifecycle();

  private HashMap<Runnable,TimerListener> _timerMap = new HashMap<>();
  
  private TimerServiceAmp _timerSelf;


  @OnInit
  private void onStart()
  {
    _lifecycle.toActive();
    
    _timerSelf = ServiceRef.current().as(TimerServiceAmp.class);
  }
  
  @OnLookup
  public TimerServiceRamp onLookup(String path)
  {
    return this;
  }
  
  /**
   * Implements {@link Timers#getCurrentTime()}
   */
  @Direct
  public long getCurrentTime()
  {
    return CurrentTime.currentTime();
  }

  /**
   * Implements {@link Timers#runAfter(Runnable, long, TimeUnit)}
   */
  @Override
  public void runAfter(@Pin Consumer<? super Cancel> task, 
                       long delay, 
                       TimeUnit unit,
                       Result<? super Cancel> result)
  {
    Objects.requireNonNull(task);
    Objects.requireNonNull(unit);

    TimerListener listener = new TimerListener(task, null);

    // _timerMap.put(task, listener);
    
    listener.alarm().runAfter(unit.toMillis(delay));
    
    result.ok(listener);
  }

  /**
   * Implements {@link Timers#runAfter(Runnable, long, TimeUnit)}
   */
  @Override
  public void runEvery(@Pin Consumer<? super Cancel> task, 
                       long delay, 
                       TimeUnit unit,
                       Result<? super Cancel> result)
  {
    // cancel(task);
    
    long period = unit.toMillis(delay);
    
    if (period <= 0) {
      throw new IllegalArgumentException();
    }

    schedule(task, new RunEveryScheduler(period), result);
  }

  /**
   * Implements {@link Timers#schedule(Runnable, TimerScheduler)}
   */
  @Override
  public void schedule(@Pin Consumer<? super Cancel> task, 
                       LongUnaryOperator scheduler,
                       Result<? super Cancel> result)
  {
    Objects.requireNonNull(task);
    Objects.requireNonNull(scheduler);

    // cancel(task);

    long now = CurrentTime.currentTime();
    long nextTime = scheduler.applyAsLong(now);

    TimerListener listener = new TimerListener(task, scheduler);
    
    result.ok(listener);
    
    if (now <= nextTime) {
      listener.queueAt(nextTime);
    }
    
    /*
      return listener;
      */
  }

  /**
   * Implements {@link Timers#runAt(Runnable, long)}
   */
  @Override
  public void runAt(@Pin Consumer<? super Cancel> task, 
                    long time,
                    Result<? super Cancel> result)
  {
    Objects.requireNonNull(task);

    // unregister(task);

    RunAtScheduler scheduler = new RunAtScheduler(time);
    
    long now = CurrentTime.currentTime();
    long nextTime = scheduler.applyAsLong(now);

    TimerListener listener = new TimerListener(task, scheduler);

    result.ok(listener);
    
    if (nextTime <= now) {
      return;
    }
    // _timerMap.put(task, listener);
    
    listener.alarm().queueAt(nextTime);
  }

  /**
   * Implements {@link Timers#cron(Runnable, String)}
   */
  /*
  @Override
  public void cron(@Pin Consumer<? super Cancel> task, 
                   String cronExpr,
                   Result<? super Cancel> result)
  {
    CronType cron = new CronType(cronExpr);

    schedule(task, new CronScheduler(cron), result);
  }
  */

  /**
   * Implements {@link Timers#cancel(Runnable)}
   */
  // @Override
  public void cancel(Cancel handle)
  {
    if (handle instanceof TimerListener) {
      TimerListener listener = (TimerListener) handle;
      
      listener.close();
    }
    /*
    TimerListener listener = _timerMap.remove(task);

    if (listener != null) {
      listener.close();
    }
    */
  }

  void remove(Runnable task)
  {
    _timerMap.remove(task);
  }

  boolean isClosed()
  {
    return _lifecycle.isDestroyed();
  }

  /*
  @Override
  public void getTask(Runnable task, Result<TaskInfo> result)
  {
    result.complete(null);
  }

  @Override
  public void getTasks(Result<List<TaskInfo>> result)
  {
    result.complete(null);
  }
  */
  
  @OnDestroy
  public void onShutdown()
  {
    _lifecycle.toDestroy();

    ArrayList<TimerListener> listeners = new ArrayList<>(_timerMap.values());

    _timerMap.clear();

    for (TimerListener listener : listeners) {
      listener.close();
    }
  }

  @Override
  public String toString()
  {
    //return getClass().getSimpleName() + "[" + getTasks() + "]";
    return getClass().getSimpleName() + "[]";
  }

  private class TimerListener implements AlarmListener, Cancel
  {
    private final Consumer<? super Cancel> _task;
    private final LongUnaryOperator _nextTime;

    // private final TaskInfo _info;

    private Alarm _alarm;

    TimerListener(Consumer<? super Cancel> task,
                  LongUnaryOperator nextTime)
    {
      _task = task;
      _nextTime = nextTime;

      // _info = new TaskInfo(_task, CurrentTime.getCurrentTime());
      
      _alarm = new Alarm(this);
    }
    
    @Override
    public void cancel()
    {
      _timerSelf.cancel(this);
      
      Alarm alarm = _alarm;
      _alarm = null;
      
      if (alarm != null) {
        alarm.close();
      }
    }
    
    void queueAt(long time)
    {
      Alarm alarm = _alarm;
      
      if (alarm != null) {
        alarm.queueAt(time);
      }
    }

    boolean isClosed()
    {
      return _alarm == null || TimerServiceRamp.this.isClosed();
    }

    Alarm alarm()
    {
      return _alarm;
    }

    void close()
    {
      Alarm alarm = _alarm;
      _alarm = null;

      if (alarm != null) {
        alarm.dequeue();
      }

      // _service.remove(_task);
    }

    @Override
    public void handleAlarm(Alarm alarm)
    {
      if (isClosed()) {
        return;
      }

      try {
        _task.accept(this);
      } catch (Throwable e) {
        log.log(Level.FINER, e.toString(), e);
        
        // _info.lastException(e);

        throw e;
      } finally {
        // _info.lastCompletedTime(CurrentTime.getCurrentTime());

        LongUnaryOperator nextTimeOp = _nextTime;

        boolean isScheduled = false;

        if (nextTimeOp != null && _alarm != null) {
          long now = CurrentTime.currentTime();
          long nextTime = nextTimeOp.applyAsLong(now);

          if (now < nextTime) {
            _alarm.queueAt(nextTime);
            isScheduled = true;

            // _info.nextRunTime(nextTime);
          }
        }

        if (! isScheduled) {
          close();
        }
      }
    }
    
    public String toString()
    {
      return getClass().getSimpleName() + "[" + _task + "]";
    }
  }

  private static class CronScheduler implements LongUnaryOperator
  {
    private final CronType _cronType;

    CronScheduler(CronType cronType)
    {
      _cronType = cronType;
    }

    @Override
    public long applyAsLong(long now)
    {
      return _cronType.nextTime(now);
    }
  }

  private static class RunAtScheduler implements LongUnaryOperator
  {
    private final long _time;

    RunAtScheduler(long time)
    {
      _time = time;
    }

    @Override
    public long applyAsLong(long now)
    {
      return _time;
    }
  }

  private static class RunEveryScheduler implements LongUnaryOperator
  {
    private final long _period;

    RunEveryScheduler(long period)
    {
      _period = period;
    }

    @Override
    public long applyAsLong(long now)
    {
      return now + _period;
    }
  }
}
