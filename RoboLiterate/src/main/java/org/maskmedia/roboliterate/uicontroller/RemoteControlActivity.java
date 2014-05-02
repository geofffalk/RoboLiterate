package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.robotcomms.BluetoothCommunicator;
import org.maskmedia.roboliterate.robotcomms.Commander;
import org.maskmedia.roboliterate.robotcomms.CommanderImpl;
import org.maskmedia.roboliterate.robotcomms.Robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.maskmedia.roboliterate.robotcomms.Robot.MODEL_EV3;

/**
 * Created by geoff on 15/02/2014.
 */
public class RemoteControlActivity extends ActionBarActivity {

    private static final boolean D = true;
    private static final String TAG = "RemoteControlActivity";
    private static final int MSG_ROBOT_TYPE_DETECTED = 3;
    private static final int MESSAGE_TOAST = 4;
    private static final int MSG_STATE_CHANGE = 5;
    private static final int MSG_ROBOT_CONNECTION = 6;
    private static final int STATUS_OK = 0;
    private static final int STATUS_ERROR = 16;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_CONFIGURE_DEVICE = 3;

    private static final int IDLE = 9;
    private static final int MOVING_FORWARD =10;
    private static final int MOVING_BACK =11;
    private static final int MOVING_LEFT =12;
    private static final int MOVING_RIGHT =13;
    private static final int MOVING_ARM =14;
    private static final int MOVING_MOTOR_A = 0;
    private static final int MOVING_MOTOR_B = 1;
    private static final int MOVING_MOTOR_C = 2;
    private static final int MOVING_MOTOR_D = 3;
    private static final int MOTOR_A = 0;
    private static final int MOTOR_B = 1;
    private static final int MOTOR_C = 2;
    private static final int MOTOR_D = 3;


    private Dialog connectingProgressDialog;
    private static final String MY_PREFS = "MyPrefs";

    private static final int STATE_IDLE = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String mRobotAddress;
    private int speed;
    private int robotState;
    private BluetoothAdapter mAdapter;

    private RobotConnectorThread mRobotConnectorThread;
    private RobotCommanderThread mRobotCommanderThread;



    public BluetoothSocket RobotSocket;

    private boolean isPortsConfigured;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remotecontroller);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        isPortsConfigured = false;

        ActionBar bar = getSupportActionBar();
        if (bar!=null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayUseLogoEnabled(true);
            bar.setDisplayShowTitleEnabled(false);
        }

        if (savedInstanceState != null) {



            if (savedInstanceState.containsKey("speed")) {
                speed = savedInstanceState.getInt("speed");
            }

            if (savedInstanceState.containsKey("ports_configured")) {
                isPortsConfigured = savedInstanceState.containsKey("ports_configured");
            }

        }



    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getConnectionState()==STATE_CONNECTED) {
            connectToRobot();

        } else {
        findRobot();
        }
        EasyTracker.getInstance(this).activityStart(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        closeConnections();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getConnectionState()==STATE_CONNECTED) {
            outState.putString("robot_address", mRobotAddress);
            outState.putBoolean("ports_configured",isPortsConfigured);
        }
        outState.putInt("speed", speed);
    }



    @Override
    public void onBackPressed() {
closeConnections();

        Intent intent = new Intent(getApplicationContext(), LaunchActivity.class);
        startActivity(intent);

    }
    private void setConnectionState(int state) {
        robotState = state;
    }

    public void closeConnections() {

        if (mRobotCommanderThread !=null)
        {
            mRobotCommanderThread.cancel();
            mRobotCommanderThread = null;
        }

        if (mRobotConnectorThread !=null)
        {
            mRobotConnectorThread.cancel();
            mRobotConnectorThread = null;
        }

        if (RobotSocket!=null) {
            try {
                RobotSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG,"Cannot close socket");
            }
        }
    }



    /**
     * If Bluetooth Adapter has been detected, and Bluetooth has been turned on,
     * start ChooseDeviceActivity to allow user to select robot to connect with. Otherwise open device's
     * activity for activating Bluetooth and wait for result
     */
    private void findRobot() {
        if (mAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_LONG).show();

        } else {

            if (!mAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Intent chooseDeviceIntent = new Intent(this, ChooseDeviceActivity.class);
                startActivityForResult(chooseDeviceIntent, REQUEST_CONNECT_DEVICE);
            }
        }
    }


    /**
     * Callback function when returning from other Activities
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {


            case REQUEST_CONNECT_DEVICE:

                // When DeviceListActivity returns with a device to connect

                if (resultCode == Activity.RESULT_OK) {

                    // Get the device MAC address and send it to RobotConnectorService. Show Connecting progress dialog

                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        mRobotAddress = extras.getString(ChooseDeviceActivity.EXTRA_DEVICE_ADDRESS);
                        setConnectionState(STATE_CONNECTING);
                        connectToRobot();


                    }
                } else if (resultCode==Activity.RESULT_CANCELED) {

                    onBackPressed();

                }


                break;
            case REQUEST_CONFIGURE_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    isPortsConfigured = true;
                    setUpUI();
                } else if (resultCode==Activity.RESULT_CANCELED) {

                        onBackPressed();

                }

                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    findRobot();
                }
                break;
        }
    }

    private synchronized void connectToRobot() {
        if (mRobotConnectorThread!=null) {
            mRobotConnectorThread.cancel();
            mRobotConnectorThread = null;
        }
        mRobotConnectorThread = new RobotConnectorThread(mHandler, mRobotAddress);
        mRobotConnectorThread.start();
        connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
    }


    private  synchronized void startRobotCommander() {
        if (mRobotCommanderThread !=null) {
            mRobotCommanderThread.cancel();
            mRobotCommanderThread = null;
        }
        mRobotCommanderThread = new RobotCommanderThread(mHandler);
        mRobotCommanderThread.start();

    }



    /**
     * Open ConfigureDevice activity if connected to robot
     */
    private void configureRobotPorts() {

        if (getConnectionState()==STATE_CONNECTED) {
            SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS,Activity.MODE_PRIVATE);

            Robot.Motor.LEFT.setPort(sharedPreferences.getInt("MOTOR_LEFT", Robot.Motor.LEFT.getPort()));
            Robot.Motor.RIGHT.setPort(sharedPreferences.getInt("MOTOR_RIGHT", Robot.Motor.RIGHT.getPort()));
            Robot.Motor.ARM.setPort(sharedPreferences.getInt("MOTOR_ARM", Robot.Motor.ARM.getPort()));

            final int robotType = Robot.getRobotType();

            if (robotType==Robot.MODEL_EV3 || robotType == Robot.MODEL_NXT) {
            Intent intent = new Intent(this, ConfigureDeviceActivity.class);
            startActivityForResult(intent, REQUEST_CONFIGURE_DEVICE);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.choose_robot_type).setTitle(R.string.cannot_get_robottype);
                builder.setPositiveButton(R.string.ev3, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Robot.setRobotType(Robot.MODEL_EV3);
                        configureRobotPorts();

                    }
                });
                builder.setNegativeButton(R.string.nxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Robot.setRobotType(Robot.MODEL_NXT);
                        configureRobotPorts();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();



            }


        }

    }



    private void setUpUI() {

        speed = 50;
        int robotType = Robot.getRobotType();

        Button upButton = (Button)findViewById(R.id.button_forward);
        upButton.setBackgroundDrawable(robotType == Robot.MODEL_NXT ? getResources().getDrawable(R.drawable.direction_up) : getResources().getDrawable(R.drawable.direction_up_3) );
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    mRobotCommanderThread.robotMove(speed);
                    robotState=MOVING_FORWARD;

                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    mRobotCommanderThread.robotMove(0);
                    robotState=IDLE;
                }
                return false;
            }
        });

        Button downButton = (Button)findViewById(R.id.button_back);
        downButton.setBackgroundDrawable(robotType == Robot.MODEL_NXT ? getResources().getDrawable(R.drawable.direction_down) : getResources().getDrawable(R.drawable.direction_down_3) );
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    mRobotCommanderThread.robotMove(-speed);
                    robotState=MOVING_BACK;
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    mRobotCommanderThread.robotMove(0);
                    robotState=IDLE;                }
                return false;
            }
        });

        Button leftButton = (Button)findViewById(R.id.button_left);
        leftButton.setBackgroundDrawable(robotType == Robot.MODEL_NXT ? getResources().getDrawable(R.drawable.direction_left) : getResources().getDrawable(R.drawable.direction_left_3) );
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    mRobotCommanderThread.robotRotate(-speed);
                    robotState=MOVING_LEFT;
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    mRobotCommanderThread.robotMove(0);
                    robotState=IDLE;                }
                return false;
            }
        });

        Button rightButton = (Button)findViewById(R.id.button_right);
        rightButton.setBackgroundDrawable(robotType == Robot.MODEL_NXT ? getResources().getDrawable(R.drawable.direction_right) : getResources().getDrawable(R.drawable.direction_right_3) );
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    mRobotCommanderThread.robotRotate(speed);
                    robotState=MOVING_RIGHT;
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    mRobotCommanderThread.robotMove(0);
                    robotState=IDLE;                }
                return false;
            }
        });
        ImageView robotImage = (ImageView)findViewById(R.id.robot_icon);

        if (Robot.getRobotType()!=MODEL_EV3) {
            LinearLayout ll = (LinearLayout)findViewById(R.id.motorD);
            ll.setVisibility(View.GONE);

            robotImage.setImageDrawable(getResources().getDrawable(R.drawable.large_icon_nxt));

        } else {
            robotImage.setImageDrawable(getResources().getDrawable(R.drawable.large_icon_ev3));
        }



        Button motorAForward = (Button)findViewById(R.id.motorA_forward);
        motorAForward.setOnTouchListener(motorForwardListener);
        Button motorBForward = (Button)findViewById(R.id.motorB_forward);
        motorBForward.setOnTouchListener(motorForwardListener);
        Button motorCForward = (Button)findViewById(R.id.motorC_forward);
        motorCForward.setOnTouchListener(motorForwardListener);
        Button motorDForward = (Button)findViewById(R.id.motorD_forward);
        motorDForward.setOnTouchListener(motorForwardListener);

        Button motorABack = (Button)findViewById(R.id.motorA_back);
        motorABack.setOnTouchListener(motorBackListener);
        Button motorBBack = (Button)findViewById(R.id.motorB_back);
        motorBBack.setOnTouchListener(motorBackListener);
        Button motorCBack = (Button)findViewById(R.id.motorC_back);
        motorCBack.setOnTouchListener(motorBackListener);
        Button motorDBack = (Button)findViewById(R.id.motorD_back);
        motorDBack.setOnTouchListener(motorBackListener);


  /*      Button armButton = (Button)findViewById(R.id.button_arm);
        armButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    mRobotCommanderThread.robotMoveArm(speed);
                    robotState=MOVING_ARM;
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    mRobotCommanderThread.robotMoveArm(0);
                    robotState=IDLE;                }
                return false;
            }
        });
*/
        SpeedSlider speedSlider = (SpeedSlider)findViewById(R.id.speedSlider);
        speedSlider.setProgress(50);
        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speed = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
 /*               switch (robotState) {
                    case MOVING_FORWARD:
                        mRobotCommanderThread.robotMove(speed);
                        break;
                    case MOVING_BACK:
                        mRobotCommanderThread.robotMove(-speed);
                        break;
                    case MOVING_LEFT:
                        mRobotCommanderThread.robotRotate(-speed);
                        break;
                    case MOVING_RIGHT:
                        mRobotCommanderThread.robotRotate(speed);
                        break;
                    case MOVING_ARM:
                        mRobotCommanderThread.robotMoveArm(speed);
                    default:
                        break;
                }*/

            }
        });

        Button beepA = (Button)findViewById(R.id.button_A);
        beepA.setOnClickListener(noteClickedListener);
        Button beepB = (Button)findViewById(R.id.button_B);
        beepB.setOnClickListener(noteClickedListener);
        Button beepC = (Button)findViewById(R.id.button_C);
        beepC.setOnClickListener(noteClickedListener);
        Button beepD = (Button)findViewById(R.id.button_D);
        beepD.setOnClickListener(noteClickedListener);
        Button beepE = (Button)findViewById(R.id.button_E);
        beepE.setOnClickListener(noteClickedListener);
        Button beepF = (Button)findViewById(R.id.button_F);
        beepF.setOnClickListener(noteClickedListener);
        Button beepG = (Button)findViewById(R.id.button_G);
        beepG.setOnClickListener(noteClickedListener);
        Button beepA2 = (Button)findViewById(R.id.button_A2);
        beepA2.setOnClickListener(noteClickedListener);



    }


    private View.OnTouchListener motorForwardListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            String tag = (String)view.getTag();
            int portIndex = Integer.parseInt(tag);
            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                mRobotCommanderThread.robotMove(portIndex, speed);
                robotState=portIndex;
            } else if (event.getAction()==MotionEvent.ACTION_UP) {
                mRobotCommanderThread.robotMove(portIndex,0);
                robotState=IDLE;                }
            return false;
        }
    };

    private View.OnTouchListener motorBackListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            String tag = (String)view.getTag();
            int portIndex = Integer.parseInt(tag);
            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                mRobotCommanderThread.robotMove(portIndex, -speed);
                robotState=portIndex;
            } else if (event.getAction()==MotionEvent.ACTION_UP) {
                mRobotCommanderThread.robotMove(portIndex,0);
                robotState=IDLE;                }
            return false;
        }
    };



    private View.OnClickListener noteClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

                mRobotCommanderThread.robotBeep(Integer.parseInt((String)(view.getTag())));


        }
    };

    private int getConnectionState() {
        return robotState;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ROBOT_CONNECTION:
                    connectingProgressDialog.dismiss();

                    switch (msg.arg1) {

                        case STATUS_ERROR:

                                Toast.makeText(getApplicationContext(),R.string.connection_failed,Toast.LENGTH_SHORT).show();
                                setConnectionState(STATE_CONNECTING);
                                findRobot();

                            break;
                        case STATUS_OK:

                                setConnectionState(STATE_CONNECTED);

                                startRobotCommander();


                            break;
                        default:
                            break;
                    }

                    break;
                case MSG_ROBOT_TYPE_DETECTED:
                    switch (msg.arg1) {
                        case STATUS_OK:

                            if (!isPortsConfigured) {

                                configureRobotPorts();
                            } else {
                                setUpUI();
                            }
                            break;

                    }
            }
        }
    };



    private class RobotConnectorThread extends Thread {

        private Handler mHandler;
        private String mAddress;

        public RobotConnectorThread( Handler handler, String address) {
mAddress = address;
            mHandler = handler;
        }

        public void run() {
            setName("ConnectThread");
            mAdapter.cancelDiscovery();
            if (RobotSocket!=null) {

            try {
                RobotSocket.close();

            } catch (IOException e2) {
                Log.e(TAG,"Could not close socket");
            }
            }

            try {
                BluetoothSocket robotBTSocketTemporary;
                BluetoothDevice robotDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mAddress);
                if (robotDevice == null) {
                    mHandler.obtainMessage(MSG_ROBOT_CONNECTION, STATUS_ERROR,0).sendToTarget();
                }
                robotBTSocketTemporary = robotDevice.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
                try {
                    robotBTSocketTemporary.connect();
                } catch (IOException e) {


                    // try another method for connection, this should work on the HTC desire, credited to Michael Biermann, MindDROID project

                    try {
                        Method mMethod = robotDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                        robotBTSocketTemporary = (BluetoothSocket) mMethod.invoke(robotDevice, Integer.valueOf(1));
                        robotBTSocketTemporary.connect();

                    } catch (IOException e1) {
                        mHandler.obtainMessage(MSG_ROBOT_CONNECTION, STATUS_ERROR,0).sendToTarget();
                        return;

                    }
                }
                RobotSocket = robotBTSocketTemporary;

                mHandler.obtainMessage(MSG_ROBOT_CONNECTION,STATUS_OK,0).sendToTarget();
            } catch (Exception e) {
                mHandler.obtainMessage(MSG_ROBOT_CONNECTION, STATUS_ERROR,0).sendToTarget();
            }

            synchronized (RemoteControlActivity.this) {
                mRobotConnectorThread = null;
            }

        }

        public void cancel() {


            try {
                if (RobotSocket!=null)
                RobotSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close socket " + e.getMessage());
            }
        }

    }

    private void setButtonColors() {

    }




    private class RobotCommanderThread extends Thread implements Commander.CommanderListener {
        private BluetoothCommunicator mCommunicator;
        private Handler mHandler;
        private Commander mRobotCommander;

        private boolean isTypeDetected;


        public RobotCommanderThread(Handler handler) {

            mHandler = handler;
            isTypeDetected = false;

        }


        public void robotMove(int speed) {
            if (mRobotCommander!=null)
            mRobotCommander.doRobotMove(speed,0,false);
        }


        public void robotMove(int portIndex, int speed) {
            if (mRobotCommander!=null) {

                mRobotCommander.doRobotMove(portIndex, speed, 0, false);
            }
            }

        public void robotRotate(int speed) {
            if (mRobotCommander!=null)
                mRobotCommander.doRobotRotate(speed, 0, false);
        }

/*        public void robotMoveArm(int speed) {
            if (mRobotCommander!=null)
                mRobotCommander.doRobotTurnArm(speed,0,false);
        }*/

        public void robotBeep(int frequency) {
            if (mRobotCommander!=null)
                mRobotCommander.doRobotBeep(speed,frequency,600,false);
        }

        public void run() {

            try {
                InputStream robotInputStream = RobotSocket.getInputStream();
                OutputStream robotOutputStream = RobotSocket.getOutputStream();
                mCommunicator = BluetoothCommunicator.getInstance(robotInputStream, robotOutputStream);
                mRobotCommander = new CommanderImpl(getApplicationContext(), mCommunicator, this);
                mRobotCommander.detectRobotType();


            } catch (IOException e) {
                e.printStackTrace();
                mHandler.obtainMessage(MSG_ROBOT_CONNECTION, STATUS_ERROR).sendToTarget();
            }

        }

        @Override
        public void onCommunicationFailure(int status) {
            if (mHandler!=null) {
                Message message = mHandler.obtainMessage(MSG_ROBOT_CONNECTION, STATUS_ERROR);
                message.sendToTarget();
            }
        }

        public void cancel() {

            mCommunicator=null;
            mRobotCommander=null;
            try {
                if (RobotSocket!=null)
                RobotSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close socket " + e.getMessage());
            }
        }

        @Override
        public void onPortsConfigured(int status) {


        }

        @Override
        public void onProgramLineExecuted(int status) {}

        @Override
        public void onProgramComplete(int status) {}

        @Override
        public void onPortsDetected(int status) {}

        @Override
        public void updateMotorReading(int status, int value) {}

        @Override
        public void updateSensorReading(Robot.Sensor sensor, int status, int value) {}

        @Override
        public void onRobotTypeDetected(int robotType) {
            if (!isTypeDetected)
            mHandler.obtainMessage(MSG_ROBOT_TYPE_DETECTED, STATUS_OK).sendToTarget();
            isTypeDetected = true;

        }

    }

}
