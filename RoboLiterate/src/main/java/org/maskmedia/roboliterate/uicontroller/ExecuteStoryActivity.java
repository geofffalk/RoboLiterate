/**
 * Copyright (C) 2013 Geoffrey Falk
 *
 * Based on Android guide to Bound Services:
 * http://developer.android.com/guide/components/bound-services.html
 *
 * Copyright (C) 2010 The Android Open Source Project
 *
 */

package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitInterpreter;
import org.maskmedia.roboliterate.rlit.RLitInterpreterImpl;
import org.maskmedia.roboliterate.rlit.RLitSentence;
import org.maskmedia.roboliterate.rlit.RLitStory;
import org.maskmedia.roboliterate.rlit.instructions.Program;
import org.maskmedia.roboliterate.robotcomms.Robot;
import org.maskmedia.roboliterate.robotcomms.RobotConnectorService;

import java.util.Map;

import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_COMPLETE;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_CONNECTIONERROR;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_CONNECTIONERROR_PAIRING;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_INITIATING;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_NO_MOTOR;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_NO_SENSOR;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_OK;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_RUNNING;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_SENSOR_ERROR;
import static org.maskmedia.roboliterate.robotcomms.Robot.MODEL_NXT;
import static org.maskmedia.roboliterate.robotcomms.Robot.Motor;
import static org.maskmedia.roboliterate.robotcomms.Robot.Sensor;
import static org.maskmedia.roboliterate.robotcomms.RobotConnector.MSG_MOTOR_READING;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_CANCEL_CONNECT_TO_ROBOT;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_CHECK_CONNECTION_STATUS;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_CONFIGURE_PORTS;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_CONNECT_TO_ROBOT;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_DETECT_SENSORPORTS;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_EXECUTE_PROGRAM;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_REGISTER_CLIENT;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_SENSOR_READING;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_UNREGISTER_CLIENT;
import static org.maskmedia.roboliterate.robotcomms.RobotConnectorService.MSG_UPLOAD_FILES;


/**
 * UI for program execution. Manages UI for robot connection, then once connection is made
 * binds to RobotConnectorService which manages all robot communication.
 * Contains a console view and sensor readout keeping users informed of how 'program' is executing.
 */
public class ExecuteStoryActivity extends ActionBarActivity {

    private static final boolean D = true;
    private static final String TAG = "ExecuteStoryActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;
 private static final int REQUEST_CONFIGURE_DEVICE = 3;
    private static final String MY_PREFS = "MyPrefs";



    private ConsoleTextView mConsoleTextView;
    private TextView mSensorReading;
    private Button mRunProgram;
    private boolean mIsBound;
    private RLitInterpreter mTranslator;
    private boolean isConnected;
    private boolean isConnecting;
    private ProgressDialog connectingProgressDialog;
    private Messenger mService;
    private BluetoothAdapter mBAdapter;
    private boolean mPortsConfigured;
    private String mFileCurrentlyUploading;
    private boolean mFilesUploaded;
    /**
     * Target for clients to send messages to
     */
    final Messenger mMessenger = new Messenger(new UIIncomingHandler());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.execute_story_activity);

        // keep screen on while this activity is running

      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayUseLogoEnabled(true);
            bar.setDisplayShowTitleEnabled(false);
        }
        mFilesUploaded = false;
        if (savedInstanceState != null) {

            // Don't bother to upload files or configure ports as this has already been done so for the
            // present configuration

            mFilesUploaded = savedInstanceState.getBoolean("filesUploaded");
            mPortsConfigured = savedInstanceState.getBoolean("portsConfigured");
        } else mPortsConfigured = false;

        mSensorReading = (TextView) findViewById(R.id.sensorReading);
        mConsoleTextView = (ConsoleTextView) findViewById(R.id.consoleView);
        mRunProgram = (Button) findViewById(R.id.startButton);
        mRunProgram.setVisibility(Button.INVISIBLE);
        mRunProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareProgram(true);
            }
        });

        // get default RLit translator

        mTranslator = RLitInterpreterImpl.getTranslator();

        // get device's Bluetooth adapter

        mBAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    //    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsBound) {

            // Bind activity to RobotConnectorService

            doBindService();
        }
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.

    }

    /**
     * Saved status of port configuration and whether files have been uploaded
     *
     * @param outState  Bundle with data to be used when Activity is resumed
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("portsConfigured", mPortsConfigured);
        outState.putBoolean("filesUploaded", mFilesUploaded);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        Message message = Message.obtain(null, MSG_CANCEL_CONNECT_TO_ROBOT);
        sendMessage(message);

        if (mIsBound) {
            doUnbindService();
        }

        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_action, menu);
        //MenuItem item = menu.findItem(R.id.action_edit);

        return true;
    }

    @Override
    public void onBackPressed() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // disconnect from robot as Back button has been pressed

        Message message = Message.obtain(null, MSG_CANCEL_CONNECT_TO_ROBOT);
        sendMessage(message);

        Intent intent = new Intent(getApplicationContext(), StoryBuilderActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:

                // Disconnect from robot as edit button has been pressed

                Message message = Message.obtain(null, MSG_CANCEL_CONNECT_TO_ROBOT);
                sendMessage(message);

                Intent intent = new Intent(getApplicationContext(), StoryBuilderActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * If Bluetooth Adapter has been detected, and Bluetooth has been turned on,
     * start ChooseDeviceActivity to allow user to select robot to connect with. Otherwise open device's
     * activity for activating Bluetooth and wait for result
     */
    private void findRobot() {
        if (mBAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_LONG).show();
            finish();

        } else {

            if (!mBAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else if (!isConnecting) {
                isConnecting = true;
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
                        String robotAddress = extras.getString(ChooseDeviceActivity.EXTRA_DEVICE_ADDRESS);
                        String robotType = extras.getString(ChooseDeviceActivity.EXTRA_DEVICE_NAME);
                        // pairing = data.getExtras().getBoolean(ChooseDeviceActivity.PAIRING);
                        Message message = Message.obtain(null, MSG_CONNECT_TO_ROBOT);
                        if (message != null) {
                            message.obj = new String[]{robotType, robotAddress};
                            sendMessage(message);
                        }
                        isConnected = false;
                        connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);

                    }
                } else if (resultCode==Activity.RESULT_CANCELED) {

                    onBackPressed();

                }


                break;
            case REQUEST_CONFIGURE_DEVICE:

                // Tell Service to configure robot ports according to the mappings chosen in ConfigureDeviceActivity

                if (resultCode == Activity.RESULT_OK) {
                    if (Robot.getRobotType()==MODEL_NXT)
               {
                    Message message = Message.obtain(null, MSG_CONFIGURE_PORTS);
                    sendMessage(message);
                } else {
                    prepareProgram(true);
                }
                }  else if (resultCode==Activity.RESULT_CANCELED) {

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

    /**
     * Open ConfigureDevice activity if connected to robot
     */
    private void configureRobotPorts() {

        if (isConnected) {
            SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS,Activity.MODE_PRIVATE);
            if (Robot.getRobotType()== Robot.MODEL_NXT) {
                Sensor.SWITCH.setPort(sharedPreferences.getInt("SENSOR_SWITCH", Sensor.SWITCH.getPort()));
                Sensor.LIGHT.setPort(sharedPreferences.getInt("SENSOR_LIGHT", Sensor.LIGHT.getPort()));
                Sensor.SOUND.setPort(sharedPreferences.getInt("SENSOR_SOUND", Sensor.SOUND.getPort()));
                Sensor.ULTRASONIC.setPort(sharedPreferences.getInt("SENSOR_ULTRASONIC", Sensor.ULTRASONIC.getPort()));
                Motor.LEFT.setPort(sharedPreferences.getInt("MOTOR_LEFT", Motor.LEFT.getPort()));
                Motor.RIGHT.setPort(sharedPreferences.getInt("MOTOR_RIGHT", Motor.RIGHT.getPort()));
                Motor.ARM.setPort(sharedPreferences.getInt("MOTOR_ARM", Motor.ARM.getPort()));
                openConfigureDialog();
            } else  if (Robot.getRobotType()== Robot.MODEL_EV3) {
                // Detect sensors in each port
                Motor.LEFT.setPort(sharedPreferences.getInt("MOTOR_LEFT", Motor.LEFT.getPort()));
                Motor.RIGHT.setPort(sharedPreferences.getInt("MOTOR_RIGHT", Motor.RIGHT.getPort()));
                Motor.ARM.setPort(sharedPreferences.getInt("MOTOR_ARM", Motor.ARM.getPort()));

                Message message = Message.obtain(null, RobotConnectorService.MSG_DETECT_SENSORPORTS);
                if (message != null) {
                    sendMessage(message);
                }
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

    private void openConfigureDialog() {

        Intent configureDeviceIntent = new Intent(this, ConfigureDeviceActivity.class);
        startActivityForResult(configureDeviceIntent, REQUEST_CONFIGURE_DEVICE);
    }

    /**
     * Check to see whether they are any sound files to upload through analysis of interpreted RLit Story. If so, request upload
     * of sounds to robot and update ConsoleView accordingly
     * @param checkSounds       boolean - check whether to upload sounds or not
     */
    private void prepareProgram(boolean checkSounds) {
        if (checkSounds && !mFilesUploaded) {
            mRunProgram.setVisibility(Button.INVISIBLE);
            Map<String, Integer> soundFilesToSend = mTranslator.extractRobotSoundFiles(RLitStory.getStory().getSentenceList());
            if (soundFilesToSend != null && soundFilesToSend.size() > 0) {
                mConsoleTextView.update(ConsoleTextView.INFO, soundFilesToSend.size() + "", ConsoleTextView.APPEND_SPACE);
                mConsoleTextView.update(ConsoleTextView.INFO, R.string.console_found_sound, ConsoleTextView.APPEND_NEWLINE);
                uploadSoundFilesToRobot(soundFilesToSend);
            } else runProgram();
        } else runProgram();
    }

    /**
     * Send the Service the RLit program to execute
     */
    private void runProgram() {
        mConsoleTextView.update(ConsoleTextView.INFO, R.string.program_start, ConsoleTextView.APPEND_NEWLINE);
        Program program = mTranslator.toProgram(getApplicationContext(), RLitStory.getStory().getSentenceList());
        Message message = Message.obtain(null, RobotConnectorService.MSG_EXECUTE_PROGRAM);
        if (message != null) {
            message.arg1 = STATUS_INITIATING;
            message.obj = program;
            sendMessage(message);
        }

    }

    /**
     * update the sensor display with latest data from RobotService
     * @param value             latest value from Service
     * @param unit
     * @param targetReached     whether the target has been reached for the RLit program to continue
     */
    private void updateSensorReading(int value, String unit, boolean targetReached) {
        Resources res = getResources();
        mSensorReading.setText((value < 0) ? "--" : value + " "+unit);
        if (targetReached) {
            mSensorReading.setTextColor(res.getColor(R.color.color4));
        } else {
            mSensorReading.setTextColor(res.getColor(R.color.color1a));
        }

    }

    /**
     * Send references to sound files that need to be uploaded to robot
     *
     * @param sounds    a Map of sound filename keys with values of upload 'slots' on robot
     */

    private void uploadSoundFilesToRobot(Map<String, Integer> sounds) {
        int numberOfFiles = sounds.size();
        String[] robotFilenames = new String[numberOfFiles];
        String[] sourceFiles = new String[numberOfFiles];
        int counter = 0;
        for (Map.Entry<String, Integer> entry : sounds.entrySet()) {
            robotFilenames[counter] = "RLit" + entry.getValue() + ".rso";
            sourceFiles[counter] = entry.getKey();
            counter++;
        }
        Message message = Message.obtain(null, MSG_UPLOAD_FILES);
        Bundle bundle = new Bundle();
        bundle.putStringArray("sourceFiles", sourceFiles);
        bundle.putStringArray("targetFiles", robotFilenames);
        mFileCurrentlyUploading = "";
        if (message != null) {
            message.arg1 = STATUS_INITIATING;
            message.obj = bundle;
        }
        sendMessage(message);
    }


    private void sendMessage(Message message) {
        if (mService != null) {
            try {
                mService.send(message);
            } catch (RemoteException e) {
                if (D) Log.e(TAG, "Cannot connect to service.");
            }
        }
    }


    /**
     * Class for interacting with the main interface of the service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);
            Message message = Message.obtain(null, MSG_REGISTER_CLIENT);
            if (message != null) {
                message.replyTo = mMessenger;
                sendMessage(message);
            }
            message = Message.obtain(null, MSG_CHECK_CONNECTION_STATUS);
            if (message != null) {
                message.replyTo = mMessenger;
                sendMessage(message);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            Toast.makeText(ExecuteStoryActivity.this, R.string.remote_service_disconnected, Toast.LENGTH_SHORT).show();

        }
    };

    /**
     * Establish a connection with RobotConnectorService
     */
    void doBindService() {
        bindService(new Intent(ExecuteStoryActivity.this, RobotConnectorService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                Message message = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                if (message != null) {
                    message.replyTo = mMessenger;
                    sendMessage(message);
                }
                unbindService(mConnection);
                mIsBound = false;
            }
        }
    }

    /**
     * Highlight the sensor display when the Service is sending data from robot sensors
     * @param displayOn turn the display on or off
     */

    private void toggleSensorDisplay(boolean displayOn) {
        LinearLayout sensorReadingLayout = (LinearLayout) findViewById(R.id.sensorReadingBox);
        TextView sensorReadingTitle = (TextView) findViewById(R.id.sensorReadingTitle);
        if (displayOn) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN)
            sensorReadingLayout.setBackground(getResources().getDrawable(R.drawable.border2));
            sensorReadingTitle.setTextColor(getResources().getColor(R.color.color1a));
        } else {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN)
            sensorReadingLayout.setBackground(getResources().getDrawable(R.drawable.border));
            sensorReadingTitle.setTextColor(getResources().getColor(R.color.grey));
        }
    }



    /**
     * handler for incoming messages from service
     */
    class UIIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_CONNECT_TO_ROBOT:

                    //   Message with connection status

                    isConnecting = false;
                    connectingProgressDialog.dismiss();
                    switch (message.arg1) {

                        case STATUS_OK:



                            isConnected = true;

                            configureRobotPorts();
                            break;
                        case STATUS_CONNECTIONERROR:
                            isConnected= false;
                            Context context = getApplicationContext();
                            if (context != null) {
                                Toast.makeText(context, "There was a problem with the connection", Toast.LENGTH_SHORT).show();
                                findRobot();
                            }
                            break;
                        case STATUS_CONNECTIONERROR_PAIRING:
                            context = getApplicationContext();
                            if (context != null) {
                                Toast.makeText(context, "The robot is in the middle of pairing", Toast.LENGTH_SHORT).show();
                            }
                        default:
                            break;
                    }
                    break;
                case MSG_CONFIGURE_PORTS:

                    //   Message with status of port configuration

                    switch (message.arg1) {
                        case STATUS_OK:
                            mPortsConfigured = true;
                            prepareProgram(true);
                    }
                    break;
                case MSG_DETECT_SENSORPORTS:
                    // EV3 has detected its sensor ports. Now let user have final say
                    openConfigureDialog();
                    break;
                case MSG_CHECK_CONNECTION_STATUS:

                    //   Message with boolean of connection status

                    if (message.obj instanceof Boolean) {
                        Log.d(TAG, message.replyTo + " is checking connection. Connection is " + message.obj);
                        if (!(Boolean) message.obj) {
                            // stopProgram();
                            findRobot();
                        } else {
                            isConnected = true;
                            configureRobotPorts();
                        }

                    }
                    break;
                case MSG_EXECUTE_PROGRAM:

                    // Message with current status of executing program

                    switch (message.arg1) {
                        case STATUS_RUNNING:
                            RLitSentence sentence = RLitStory.getStory().getSentenceAtPosition(message.arg2);
                            mConsoleTextView.update(ConsoleTextView.EXECUTION,
                                    sentence.toString(), (sentence.getSentenceType() == RLitSentence.TYPE_EVENTLISTENER_WHEN) ?
                                    ConsoleTextView.TRIM : ConsoleTextView.APPEND_NEWLINE);
                            break;
                        case STATUS_COMPLETE:
                            mConsoleTextView.update(ConsoleTextView.INFO, R.string.program_complete, ConsoleTextView.APPEND_NEWLINE);
                            mRunProgram.setVisibility(Button.VISIBLE);
                            //    stopProgram();
                    }
                    break;
                case MSG_SENSOR_READING:
                    Sensor sensor = (Sensor)(message.obj);
                    int value = message.arg2;
                    String unit = (sensor==Sensor.SWITCH && value>0) ? "bump" : sensor.getUnit();
                    switch (message.arg1) {

                        case STATUS_OK:
                            toggleSensorDisplay(true);
                            updateSensorReading(value, unit, false);
                            break;
                        case STATUS_COMPLETE:
                            toggleSensorDisplay(false);
                            updateSensorReading(value, unit, true);
                            break;
                        case STATUS_SENSOR_ERROR:
                            toggleSensorDisplay(false);
                            mConsoleTextView.update(ConsoleTextView.ERROR, sensor.toString() + " " + getResources().getString(R.string.console_sensor_error), ConsoleTextView.APPEND_NEWLINE);
                        case STATUS_NO_SENSOR:
                            toggleSensorDisplay(false);
                            mConsoleTextView.update(ConsoleTextView.ERROR, sensor.toString() + " " + getResources().getString(R.string.console_no_sensor), ConsoleTextView.APPEND_NEWLINE);
                            break;

                    }
                    break;
                case MSG_MOTOR_READING:
                    switch (message.arg1) {
                        case STATUS_NO_MOTOR:
                            mConsoleTextView.update(ConsoleTextView.ERROR, R.string.console_no_motor, ConsoleTextView.APPEND_NEWLINE);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

}
