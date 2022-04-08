package org.thosp.yourlocalweather.service;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.thosp.yourlocalweather.model.Location;
import org.thosp.yourlocalweather.utils.ForecastUtil;
import org.thosp.yourlocalweather.utils.Utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.thosp.yourlocalweather.utils.LogToFile.appendLog;

@TargetApi(Build.VERSION_CODES.M)
public abstract class AbstractAppJob extends JobService {

    private static final String TAG = "AbstractAppJob";

    protected Messenger currentWeatherService;
    protected Lock currentWeatherServiceLock = new ReentrantLock();
    protected Queue<Message> currentWeatherUnsentMessages = new LinkedList<>();
    private Messenger wakeUpService;
    private Lock wakeUpServiceLock = new ReentrantLock();
    private Queue<Message> wakeUpUnsentMessages = new LinkedList<>();

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    protected void reScheduleNextAlarm(int jobId, String updatePeriodStr, Class serviceClass) {
        long updateAutoPeriodMills = Utils.intervalMillisForAlarm(updatePeriodStr);
        reScheduleNextAlarm(jobId, updateAutoPeriodMills, serviceClass);
    }

    protected void reScheduleNextAlarm(int jobId, long updatePeriod, Class serviceClass) {
        appendLog(getBaseContext(), TAG, "next alarm:", updatePeriod,
                ", serviceClass=", serviceClass);
        ComponentName serviceComponent = new ComponentName(this, serviceClass);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setMinimumLatency(updatePeriod); // wait at least
        builder.setOverrideDeadline(updatePeriod + (3 * 1000)); // maximum delay
        JobScheduler jobScheduler = getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    protected void sendMessageToWeatherForecastService(long locationId) {
        sendMessageToWeatherForecastService(locationId, null);
    }

    protected void sendMessageToWeatherForecastService(long locationId, String updateSource) {
        if (!ForecastUtil.shouldUpdateForecast(this, locationId, UpdateWeatherService.WEATHER_FORECAST_TYPE)) {
            return;
        }
        currentWeatherServiceLock.lock();
        try {
            Message msg = Message.obtain(
                    null,
                    UpdateWeatherService.START_WEATHER_FORECAST_UPDATE,
                    new WeatherRequestDataHolder(locationId, updateSource, UpdateWeatherService.START_WEATHER_FORECAST_UPDATE)
            );
            if (checkIfCurrentWeatherServiceIsNotBound()) {
                //appendLog(getBaseContext(), TAG, "WidgetIconService is still not bound");
                currentWeatherUnsentMessages.add(msg);
                return;
            }
            //appendLog(getBaseContext(), TAG, "sendMessageToService:");
            currentWeatherService.send(msg);
        } catch (RemoteException e) {
            appendLog(getBaseContext(), TAG, e.getMessage(), e);
        } finally {
            currentWeatherServiceLock.unlock();
        }
    }

    protected void unbindAllServices() {
        unbindCurrentWeatherService();
        unbindWakeUpService();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        try {
            super.unbindService(conn);
        } catch (Exception e) {
            appendLog(this, "TAG", e.getMessage(), e);
        }
    }

    protected void sendMessageToCurrentWeatherService(Location location,
                                                      int wakeUpSource,
                                                      boolean updateWeatherOnly) {
        sendMessageToCurrentWeatherService(location, null, wakeUpSource, updateWeatherOnly);
    }

    protected void sendMessageToCurrentWeatherService(Location location,
                                                      String updateSource,
                                                      int wakeUpSource,
                                                      boolean updateWeatherOnly) {
        sendMessageToWakeUpService(
                AppWakeUpManager.WAKE_UP,
                wakeUpSource
        );
        currentWeatherServiceLock.lock();
        try {
            Message msg = Message.obtain(
                    null,
                    UpdateWeatherService.START_CURRENT_WEATHER_UPDATE,
                    new WeatherRequestDataHolder(location.getId(), updateSource, updateWeatherOnly, UpdateWeatherService.START_CURRENT_WEATHER_UPDATE)
            );
            if (checkIfCurrentWeatherServiceIsNotBound()) {
                currentWeatherUnsentMessages.add(msg);
                return;
            }
            currentWeatherService.send(msg);
            serviceConnected(currentWeatherServiceConnection);
        } catch (RemoteException e) {
            appendLog(getBaseContext(), TAG, e.getMessage(), e);
        } finally {
            currentWeatherServiceLock.unlock();
        }
    }

    protected boolean checkIfCurrentWeatherServiceIsNotBound() {
        if (currentWeatherService != null) {
            return false;
        }
        try {
            bindCurrentWeatherService();
        } catch (Exception ie) {
            appendLog(getBaseContext(), TAG, "currentWeatherServiceIsNotBound interrupted:", ie);
        }
        return (currentWeatherService == null);
    }

    private void bindCurrentWeatherService() {
        appendLog(getBaseContext(), getClass().getSimpleName(), "bind current weather service:", this.toString());
        getApplicationContext().bindService(
                new Intent(getApplicationContext(), UpdateWeatherService.class),
                currentWeatherServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void unbindCurrentWeatherService() {
        if (currentWeatherService == null) {
            return;
        }
        getApplicationContext().unbindService(currentWeatherServiceConnection);
    }

    private ServiceConnection currentWeatherServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binderService) {
            currentWeatherService = new Messenger(binderService);
            currentWeatherServiceLock.lock();
            try {
                while (!currentWeatherUnsentMessages.isEmpty()) {
                    currentWeatherService.send(currentWeatherUnsentMessages.poll());
                }
            } catch (RemoteException e) {
                appendLog(getBaseContext(), TAG, e.getMessage(), e);
            } finally {
                currentWeatherServiceLock.unlock();
            }
            serviceConnected(currentWeatherServiceConnection);
        }
        public void onServiceDisconnected(ComponentName className) {
            currentWeatherService = null;
        }
    };

    protected abstract void serviceConnected(ServiceConnection serviceConnection);

    protected void sendMessageToWakeUpService(int wakeAction, int wakeupSource) {
        wakeUpServiceLock.lock();
        try {
            Message msg = Message.obtain(
                    null,
                    wakeAction,
                    wakeupSource,
                    0
            );
            if (checkIfWakeUpServiceIsNotBound()) {
                wakeUpUnsentMessages.add(msg);
                return;
            }
            wakeUpService.send(msg);
        } catch (RemoteException e) {
            appendLog(getBaseContext(), TAG, e.getMessage(), e);
        } finally {
            wakeUpServiceLock.unlock();
        }
    }

    private boolean checkIfWakeUpServiceIsNotBound() {
        if (wakeUpService != null) {
            return false;
        }
        try {
            bindWakeUpService();
        } catch (Exception ie) {
            appendLog(getBaseContext(), TAG, "currentWeatherServiceIsNotBound interrupted:", ie);
        }
        return (wakeUpService == null);
    }

    private void bindWakeUpService() {
        appendLog(getBaseContext(), getClass().getSimpleName(), "bind wakeup service:", this.toString());
        getApplicationContext().bindService(
                new Intent(getApplicationContext(), AppWakeUpManager.class),
                wakeUpServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void unbindWakeUpService() {
        if (wakeUpService == null) {
            return;
        }
        getApplicationContext().unbindService(wakeUpServiceConnection);
    }

    private ServiceConnection wakeUpServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binderService) {
            wakeUpService = new Messenger(binderService);
            wakeUpServiceLock.lock();
            try {
                while (!wakeUpUnsentMessages.isEmpty()) {
                    wakeUpService.send(wakeUpUnsentMessages.poll());
                }
            } catch (RemoteException e) {
                appendLog(getBaseContext(), TAG, e.getMessage(), e);
            } finally {
                wakeUpServiceLock.unlock();
            }
            serviceConnected(currentWeatherServiceConnection);
        }
        public void onServiceDisconnected(ComponentName className) {
            wakeUpService = null;
        }
    };
}
