package com.fewlaps.flone.service;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.fewlaps.flone.DesiredPitchRollCalculator;
import com.fewlaps.flone.DesiredYawCalculator;
import com.fewlaps.flone.data.CalibrationDatabase;
import com.fewlaps.flone.data.DefaultValues;
import com.fewlaps.flone.data.KnownDronesDatabase;
import com.fewlaps.flone.data.bean.Drone;
import com.fewlaps.flone.io.bean.ActualArmedData;
import com.fewlaps.flone.io.bean.ArmedDataChangeRequest;
import com.fewlaps.flone.io.bean.CalibrateDroneAccelerometerRequest;
import com.fewlaps.flone.io.bean.CalibrateDroneMagnetometerRequest;
import com.fewlaps.flone.io.bean.CalibrationDataChangedEvent;
import com.fewlaps.flone.io.bean.DelayData;
import com.fewlaps.flone.io.bean.DroneConnectionStatusChanged;
import com.fewlaps.flone.io.bean.DroneSensorData;
import com.fewlaps.flone.io.bean.MultiWiiValues;
import com.fewlaps.flone.io.bean.UserTouchModeChangedEvent;
import com.fewlaps.flone.io.bean.UsingRawDataChangeRequest;
import com.fewlaps.flone.io.communication.Bluetooth;
import com.fewlaps.flone.io.communication.Communication;
import com.fewlaps.flone.io.communication.RCSignals;
import com.fewlaps.flone.io.communication.protocol.MultiWii230;
import com.fewlaps.flone.io.communication.protocol.MultirotorData;
import com.fewlaps.flone.io.input.phone.PhoneOutputData;
import com.fewlaps.flone.io.input.phone.PhoneSensorsInput;
import com.fewlaps.flone.io.input.phone.RawDataInput;
import com.fewlaps.flone.util.NotificationUtil;

import de.greenrobot.event.EventBus;

/**
 * This Sercive is the responsable of maintaining a connection with the Drone, asking for data, and sending data
 *
 * @author Roc Boronat (roc@fewlaps.com)
 * @date 15/02/2015
 */
public class DroneService extends BaseService {

    public static final boolean ARMED_DEFAULT = false;
    public static final boolean USING_RAW_DATA_DEFAULT = false;

    public static final Object ACTION_GET_ARMED = "getArmed";
    public static final String ACTION_CONNECT = "connect";
    public static final String ACTION_DISCONNECT = "disconnect";

    private boolean armed = ARMED_DEFAULT;
    private boolean usingRawData = USING_RAW_DATA_DEFAULT;

    private static final int BAUD_RATE = 115200; //The baud rate where the BT works

    private static final int DELAY_RECONNECT = 2000; //The time the reconnect task will wait between launches
    private static final int COMMAND_TIMEOUT = 1000; //The time we consider that was "too time ago for being connected"
    private static final int TELEMETRY_INTERVAL = 50;
    private static final int SEND_RAW_DATA_INTERVAL = 10;

    public Communication communication;
    public MultirotorData protocol;

    public boolean running = false;
    public boolean isUserTouching = false;

    private Handler connectTask = new Handler();
    private Handler telemetryTask = new Handler();
    private Handler sendRawDataTask = new Handler();

    private long lastTelemetryRequestSent = 0;

    private PhoneSensorsInput phoneSensorsInput;
    private DroneSensorData droneSensorInput;
    private PhoneOutputData phoneOutputData = new PhoneOutputData();

    public static MultiWiiValues valuesSent = new MultiWiiValues(); //Created at startup, never changed, never destroyed, totally reused at every request
    public static final RCSignals rc = new RCSignals(); //Created at startup, never changed, never destroyed, totally reused at every request

    private DesiredYawCalculator yawCalculator = new DesiredYawCalculator();
    private DesiredPitchRollCalculator pitchRollCalculator = new DesiredPitchRollCalculator(DefaultValues.DEFAULT_PITCH_ROLL_LIMIT);
    private double headingDifference = 0.0;

    private int yaw;
    private int pitch;
    private int roll;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        communication = new Bluetooth(getApplicationContext());
        protocol = new MultiWii230(communication);

        startForeground(NotificationUtil.KEY_FOREGROUND_NOTIFICATION, NotificationUtil.getForegroundServiceNotification(this));

        onEventMainThread(ACTION_CONNECT);

        if (phoneSensorsInput == null) {
            phoneSensorsInput = new PhoneSensorsInput(this);
        }

        updateCalibrationData();

        return START_NOT_STICKY;
    }

    public void onEventMainThread(String action) {
        if (action.equals(ACTION_CONNECT)) {
            running = true;
            reconnectRunnable.run();
            telemetryRunnable.run();
            sendRawDataRunnable.run();
        } else if (action.equals(ACTION_DISCONNECT)) {
            phoneSensorsInput.unregisterListeners();
            communication.close();
            running = false;
            stopForeground(true);
            stopSelf();
        } else if (action.equals(ACTION_GET_ARMED)) {
            EventBus.getDefault().post(new ActualArmedData(armed));
        }
    }

    public void onEventMainThread(DroneConnectionStatusChanged status) {
        if (status.isConnected()) {
            protocol.SendRequestMSP_ATTITUDE();
        }
    }

    public void onEventMainThread(DroneSensorData droneSensorData) {
        this.droneSensorInput = droneSensorData;
        EventBus.getDefault().post(new DelayData((int) (System.currentTimeMillis() - lastTelemetryRequestSent)));

        updateRcWithInputData();
        EventBus.getDefault().post(phoneOutputData);

        valuesSent.update(rc);
    }

    public void onEventMainThread(ArmedDataChangeRequest request) {
        armed = request.isArmed();
        EventBus.getDefault().post(new ActualArmedData(armed));
    }

    public void onEventMainThread(UsingRawDataChangeRequest request) {
        usingRawData = request.isUsingRawData();
    }

    public void onEventMainThread(CalibrationDataChangedEvent event) {
        updateCalibrationData();
    }

    public void onEventMainThread(CalibrateDroneAccelerometerRequest event) {
        protocol.SendRequestMSP_ACC_CALIBRATION();
    }

    public void onEventMainThread(CalibrateDroneMagnetometerRequest event) {
        protocol.SendRequestMSP_MAG_CALIBRATION();
    }

    public void onEventMainThread(UserTouchModeChangedEvent event) {
        isUserTouching = event.isTouching();
    }

    private final Runnable reconnectRunnable = new Runnable() {
        public void run() {
            Log.d("RUNNABLE", "reconnectRunnable.run()");
            if (running) {
                if (!communication.Connected) {
                    Drone selectedDrone = KnownDronesDatabase.getSelectedDrone(DroneService.this);
                    if (selectedDrone != null) {
                        protocol.Connect(selectedDrone.address, BAUD_RATE, 0);
                    }
                } else {
                    if (lastTelemetryRequestSent < System.currentTimeMillis() - COMMAND_TIMEOUT) {
                        protocol.SendRequestMSP_ATTITUDE(); //Requesting the attitude, in order to make the connection fail
                    }
                }
                connectTask.postDelayed(reconnectRunnable, DELAY_RECONNECT);
            }
        }
    };

    private final Runnable telemetryRunnable = new Runnable() {
        public void run() {
            Log.d("RUNNABLE", "telemetryRunnable.run()");
            if (running) {
                if (communication.Connected) {
                    lastTelemetryRequestSent = System.currentTimeMillis();
                    protocol.SendRequestMSP_ATTITUDE();
                }
                telemetryTask.postDelayed(telemetryRunnable, TELEMETRY_INTERVAL);
            }
        }
    };

    private final Runnable sendRawDataRunnable = new Runnable() {
        public void run() {
            Log.d("RUNNABLE", "sendRawDataRunnable.run()");
            if (running) {
                if (communication.Connected) {
                    updateRcWithInputData();
                    protocol.sendRequestMSP_SET_RAW_RC(rc.get());
                }
                sendRawDataTask.postDelayed(sendRawDataRunnable, SEND_RAW_DATA_INTERVAL);
            }
        }
    };

    /**
     * Sets the RC data, using the userInput and the droneInput. It's a common task
     * to do before sending the RC to the drone, to make it fly as the user excepts
     */
    private void updateRcWithInputData() {
        if (usingRawData) {
            rc.setThrottle((int) RawDataInput.instance.getThrottle());
            rc.setRoll((int) RawDataInput.instance.getRoll());
            rc.setPitch((int) RawDataInput.instance.getPitch());
            rc.setYaw((int) RawDataInput.instance.getHeading());
            rc.set(RCSignals.AUX1, (int) RawDataInput.instance.getAux1());
            rc.set(RCSignals.AUX2, (int) RawDataInput.instance.getAux2());
            rc.set(RCSignals.AUX3, (int) RawDataInput.instance.getAux3());
            rc.set(RCSignals.AUX4, (int) RawDataInput.instance.getAux4());
        } else {
            if (armed) {
                rc.set(RCSignals.AUX1, RCSignals.RC_MAX);
                if (isUserTouching) {
                    rc.set(RCSignals.AUX2, RCSignals.RC_MIN);
                } else {
                    rc.set(RCSignals.AUX2, RCSignals.RC_MAX);
                }
                Log.i("AUX2", "" + rc.get(RCSignals.AUX2));

                rc.setThrottle((int) phoneSensorsInput.getThrottle());

                yaw = RCSignals.RC_MID + ((int) yawCalculator.getYaw(droneSensorInput.getHeading() + headingDifference, phoneSensorsInput.getHeading()));
                pitch = pitchRollCalculator.getValue((int) phoneSensorsInput.getPitch());
                roll = pitchRollCalculator.getValue((int) phoneSensorsInput.getRoll());
            } else {
                rc.set(RCSignals.AUX1, RCSignals.RC_MIN);
                rc.setThrottle(RCSignals.RC_MIN);

                yaw = RCSignals.RC_MID;
                pitch = RCSignals.RC_MID;
                roll = RCSignals.RC_MID;
            }

            rc.setYaw(yaw);
            rc.setRoll(pitch);
            rc.setPitch(roll);

            phoneOutputData.update(yaw, pitch, roll);
        }
    }

    private void updateCalibrationData() {
        pitchRollCalculator.setLimit(CalibrationDatabase.getPhoneCalibrationData(this).getLimit());

        Drone selectedDrone = KnownDronesDatabase.getSelectedDrone(DroneService.this);
        headingDifference = CalibrationDatabase.getDroneCalibrationData(this, selectedDrone.nickName).getHeadingDifference();
    }
}
