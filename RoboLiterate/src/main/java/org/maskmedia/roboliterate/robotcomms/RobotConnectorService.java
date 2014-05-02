/**
 * Copyright (C) 2013 Geoffrey Falk
 *
 * Service Messenger architecture based on Android Bound Services tutorial:
 * http://developer.android.com/guide/components/bound-services.html
 *
 * Copyright (C) 2010 The Android Open Source Project
 *
 *  Method of connecting to robot adapted from MINDdroid application: Copyright (C) 2010 Guenther Hoelzl, Shawn Brown
 *  https://github.com/NXT/LEGO-MINDSTORMS-MINDdroid
 *
 */

package org.maskmedia.roboliterate.robotcomms;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import org.maskmedia.roboliterate.rlit.instructions.Program;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_COMPLETE;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_CONNECTIONERROR;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_CONNECTIONERROR_PAIRING;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_INITIATING;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_OK;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_RUNNING;

/**
 * Android Service layer that manages connection with robot device and passes messages back to the UI layer
 */
public class RobotConnectorService extends Service implements RobotConnector, Commander.CommanderListener

{


    // for debugging
    private static final boolean D = true;
    private static final String TAG = "RobotConnectorService";


    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // this is the only OUI registered by LEGO, see http://standards.ieee.org/regauth/oui/index.shtml

    public static final String OUI_LEGO = "00:16:53";
    private static final long POST_RESET_DELAY = 1000;
    public static boolean ServiceIsRunning = false;
    private static BluetoothSocket RobotSocket;
    //private static int RobotType;
    private static BluetoothCommunicator BTComms;
    private BluetoothSocket robotBTSocketTemporary;
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    private boolean isPairing = false;
    protected boolean mRobotConnected;
    private Commander mCommander;



    /**
     *
     * Handler for incoming messages from UI client
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
                case MSG_REGISTER_CLIENT:

                    // register UI client to service

                    mClients.add(message.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:

                    // unregister UI client

                    mClients.remove(message.replyTo);
                    break;
                case MSG_CONNECT_TO_ROBOT:

                    // connect to robot with address and name sent by client

                    if (message.obj instanceof String[])
                        connectToRobot(((String[]) message.obj)[0], ((String[]) message.obj)[1]);
                    break;
                case MSG_CANCEL_CONNECT_TO_ROBOT:
                    cancelConnectToRobot();
                    break;
                case MSG_EXECUTE_PROGRAM:
                    switch (message.arg1) {
                        case STATUS_INITIATING:

                            // execute 'program' packaged in message object

                            if (message.obj instanceof Program)
                                mCommander.runRobotProgram((Program) message.obj);
                            break;
                    }
                    break;
                case MSG_STOP_PROGRAM:
                    mCommander.stopRobotProgram();
                    break;
                case MSG_DETECT_SENSORPORTS:
                    mCommander.detectSensorPorts();
                    break;
                case MSG_CONFIGURE_PORTS:
                    mCommander.configurePorts();
                    break;
                case MSG_CHECK_CONNECTION_STATUS:

                    // check status of connection for UI client

                    checkRobotConnection();
                    try {
                        if (message.replyTo != null) {
                            message.replyTo.send(Message.obtain(null, MSG_CHECK_CONNECTION_STATUS, mRobotConnected));
                        }
                    } catch (RemoteException e) {
                        if (D) {
                            Log.e(TAG, "Client " + message.replyTo + " is no longer with this world.");
                        }
                    }
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT>=14) {
        if (RobotSocket != null && RobotSocket.isConnected() && BTComms.isConnected()) {
            initRobot();
        }
        } else {
            if (RobotSocket != null && BTComms.isConnected()) {
                initRobot();
            }
        }
        Log.d(TAG, "Service onCreate called");
    }


    public class LocalBinder extends Binder {
        RobotConnectorService getService() {
            return RobotConnectorService.this;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);
        Log.i("RobotConnectorService", "Received start id " + startId + ": " + intent);
        ServiceIsRunning = true;
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //  return START_STICKY;
        return res;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ServiceIsRunning = false;
//        mCommander.doRobotStopMotors();
//        mCommander.doRobotStopSounds();
        cancelConnectToRobot();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service
     *
     * @param intent
     * @return IBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCommunicationFailure(int status) {
        notifyClients(MSG_CONNECT_TO_ROBOT, STATUS_CONNECTIONERROR,0);

    }

    /**
     * Update from Commander on status of port configuration
     *
     * @param status        int - status constant
     */
    @Override
    public void onPortsConfigured(int status) {
        notifyClients(MSG_CONFIGURE_PORTS, status, 0);
    }

    /**
     * Update from Commander reporting that a program line has been executed
     * @param status    int - status constant
     */
    @Override
    public void onProgramLineExecuted(int status) {
        notifyClients(MSG_EXECUTE_PROGRAM, STATUS_RUNNING, status);
    }

    /**
     * Update from Commander reporting that program has finished execution
     * @param status    int - status constant
     */
    @Override
    public void onProgramComplete(int status) {
        notifyClients(MSG_EXECUTE_PROGRAM, STATUS_COMPLETE, 0);
    }

    @Override
    public void onPortsDetected(int status) {
        notifyClients(MSG_DETECT_SENSORPORTS, STATUS_COMPLETE, 0);
    }



    /**
     * Notify UI clients with message parameter
     * @param message   - Message
     */
    public void notifyClients(Message message) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(message);
            } catch (RemoteException e) {
                if (D) {
                    Log.e(TAG, "Client " + mClients.get(i) + " is no longer with this world.");
                }
                mClients.remove(i);
            }
        }
    }

    public void notifyClients(int key, int arg1, int arg2) {
        Message message = Message.obtain(null, key, arg1, arg2);
        notifyClients(message);
    }

    public void notifyClients(int key, int arg1, int arg2, Object obj) {
        Message message = Message.obtain(null, key, arg1, arg2, obj);
        notifyClients(message);
    }

    public void notifyClients(int key, boolean status) {
        Message message = Message.obtain(null, key, status);
        notifyClients(message);
    }


    public void updateSensorReading(Robot.Sensor sensor, int status, int value) {
        notifyClients(MSG_SENSOR_READING, status, value, sensor);
    }

    @Override
    public void onRobotTypeDetected(int robotType) {



    }

    @Override
    public void updateMotorReading(int status, int value) {
        notifyClients(MSG_MOTOR_READING, status, value);
    }

    /**
     * Attempt to connect to Robot. Adapted from MindDROID application
     *
     * @param robotType - String - NXT or EV3
     * @param robotAddress  -   String - MAC address of Bluetooth connection
     */
    public void connectToRobot(String robotType, String robotAddress) {

        final String finalRobotAddress = robotAddress;
        final RobotConnector connector = this;

        Log.d(TAG, "Connecting to robot " + robotType + " at " + robotAddress);
        ExecutorService robotConnectionExecutor = Executors.newSingleThreadExecutor();
        robotConnectionExecutor.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    BluetoothSocket robotBTSocketTemporary;
                    BluetoothDevice robotDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(finalRobotAddress);
                    if (robotDevice == null) {
                        notifyClients(MSG_CONNECT_TO_ROBOT, STATUS_CONNECTIONERROR, 0);
                        return;
                    }
                    robotBTSocketTemporary = robotDevice.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
                    try {
                        robotBTSocketTemporary.connect();
                    } catch (IOException e) {
                        if (isPairing) {
                            notifyClients(MSG_CONNECT_TO_ROBOT, STATUS_CONNECTIONERROR_PAIRING, 0);
                            return;
                        }
                        // try another method for connection, this should work on the HTC desire, credited to Michael Biermann, MindDROID project

                        try {
                            Method mMethod = robotDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                            robotBTSocketTemporary = (BluetoothSocket) mMethod.invoke(robotDevice, Integer.valueOf(1));
                            robotBTSocketTemporary.connect();

                        } catch (IOException e1) {
                            notifyClients(MSG_CONNECT_TO_ROBOT, STATUS_CONNECTIONERROR, 0);
                            return;
                        }

                    }
                    RobotSocket = robotBTSocketTemporary;
                    InputStream robotInputStream = RobotSocket.getInputStream();
                    OutputStream robotOutputStream = RobotSocket.getOutputStream();
                    BTComms = BluetoothCommunicator.getInstance(robotInputStream, robotOutputStream);



                    mRobotConnected = true;
                } catch (Exception e) {
                    if (isPairing) {
                        notifyClients(MSG_CONNECT_TO_ROBOT, STATUS_CONNECTIONERROR_PAIRING, 0);
                        return;
                    }
                }


              //  RobotType = (robotType.startsWith("EV3") ? Robot.MODEL_EV3 : Robot.MODEL_NXT);

                initRobot();

                notifyClients(MSG_CONNECT_TO_ROBOT, STATUS_OK,0);

            }
        });

    }

    public void initRobot() {
        mCommander = new CommanderImpl(getApplicationContext(), BTComms, this);
        mCommander.detectRobotType();
    }

    public void cancelConnectToRobot() {
        if (mCommander!=null) {
        mCommander.stopRobotProgram();
        }
        try {
            if (RobotSocket != null)
                RobotSocket.close();
            if (robotBTSocketTemporary != null)
                robotBTSocketTemporary.close();
            mRobotConnected = false;
        } catch (IOException e) {
            //         Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
            if (D) Log.d(TAG, "Connection cancelled: " + e.getMessage());
        }


    }




    public boolean checkRobotConnection() {
      mRobotConnected = false;
        if (RobotSocket!=null) {
        if (Build.VERSION.SDK_INT>=14) {
        mRobotConnected = (RobotSocket != null && RobotSocket.isConnected()) ? true : false;
        } else {
            try {
                RobotSocket.connect();
                SystemClock.sleep(POST_RESET_DELAY);
                mRobotConnected = false;

            } catch (Exception e) {
         mRobotConnected = false;
            }





//            mRobotConnected = (RobotSocket != null && RobotSocket.getRemoteDevice()!=null) ? true : false;
        }
        }
            return mRobotConnected;
    }

    public boolean isConnected() {
        return mRobotConnected;
    }




}
