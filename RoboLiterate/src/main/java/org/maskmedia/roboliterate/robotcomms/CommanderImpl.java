package org.maskmedia.roboliterate.robotcomms;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.maskmedia.roboliterate.rlit.instructions.AndroidStopSound;
import org.maskmedia.roboliterate.rlit.instructions.Instruction;
import org.maskmedia.roboliterate.rlit.instructions.Program;
import org.maskmedia.roboliterate.robotcomms.ev3.ByteTranslatorEV3;
import org.maskmedia.roboliterate.robotcomms.ev3.MotorListenerEV3;
import org.maskmedia.roboliterate.robotcomms.nxt.ByteTranslatorNXT;
import org.maskmedia.roboliterate.robotcomms.nxt.DistanceSensorListenerNXT;
import org.maskmedia.roboliterate.robotcomms.nxt.MotorListenerNXT;

import java.io.IOException;

import static org.maskmedia.roboliterate.robotcomms.Robot.MODEL_EV3;
import static org.maskmedia.roboliterate.robotcomms.Robot.MODEL_NXT;
import static org.maskmedia.roboliterate.robotcomms.Robot.Motor;
import static org.maskmedia.roboliterate.robotcomms.Robot.Sensor;

/**
 * Created by geoff on 10/02/2014.
 */
public class CommanderImpl implements Commander {
    private static final String TAG = "CommanderImpl";
    private ByteTranslator mTranslator;
    private CommanderListener mListener;
    private BluetoothCommunicator mBTComms;
    protected Context mContext;
    private Thread fileUploadThread;
    private Thread programExecutionThread;
    RobotListener mRobotListener;
    private RobotProgramLooper mProgramLooper;
    private SoundUploader mSoundUploader;



    /**
     *
     * Handler for incoming messages from SoundUploader and ProgramExecution threads
     */
    class ThreadHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_PROGRAM_THREAD:
                    // Messages from ProgramExecutor thread
                    switch (message.arg1) {
                        case STATUS_RUNNING:
                            // A program Instruction has been executed
                            mListener.onProgramLineExecuted(message.arg2);
                            break;
                        case STATUS_COMPLETE:
                            // the program is complete
                            mListener.onProgramComplete(message.arg2);
                            break;

                    }

                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }



    public CommanderImpl(Context context, BluetoothCommunicator btComms, CommanderListener listener) {
     //   mTranslator = ByteTranslatorEV3.getInstance();

        mBTComms = btComms;
        mListener = listener;
        mContext = context;
        setTranslator(ByteTranslatorEV3.getInstance());
        detectRobotType();


    }

    public void detectRobotType() {


        // first send EV3 GET_INPUT_TYPE_AND_MODE message to see if she understands.


       byte[] message = new byte[]{0,15,0,2,0,-103,5,-127,0,-127,0,-31,0,-31,1};
        sendMessageToRobot(message);
        byte[] returnMessage = receiveMessageFromRobot();
if (returnMessage!=null) {
        if (returnMessage[0]==0 && returnMessage[2]==2) {
            Log.d(TAG,"EV3 detected");
            Robot.setRobotType(MODEL_EV3);
            setTranslator(ByteTranslatorEV3.getInstance());

            mListener.onRobotTypeDetected(MODEL_EV3);
        } else if (returnMessage[0]==2) {
                Log.d(TAG,"NXT detected");
                Robot.setRobotType(MODEL_NXT);
            setTranslator(ByteTranslatorNXT.getInstance());
            mListener.onRobotTypeDetected(MODEL_NXT);
            }
}



    }

    public void setTranslator(ByteTranslator bt) {
        mTranslator = bt;
    }


    /**
     * Implementation of InstructionExecutor interface. Moves robot
     * @param throttle              int - speed of robot movement
     * @param distance              int - distance of robot movement
     * @param waitUntilComplete     int - wait for motors to stop before carrying on
     */
    @Override
    public void doRobotMove(int throttle, int distance, boolean waitUntilComplete) {
        if (throttle > 100)
            throttle = 100;
        else if (throttle < -100)
            throttle = -100;
        if (throttle==0) {
            doRobotStopMotors();
            return;
        }
        else if (Motor.LEFT.getPort()!=-1 && Motor.LEFT.getPort()!=-1) {
            byte[] message_leftMotor = mTranslator.getMotorMessage(Motor.LEFT, throttle, distance, true);
            byte[] message_rightMotor = mTranslator.getMotorMessage(Motor.RIGHT, throttle, distance, true);
            sendMessageToRobot(message_leftMotor);
            sendMessageToRobot(message_rightMotor);
            if (waitUntilComplete) {
                waitForRobot(LISTENER_MOTORS_MOVE, 0, 0, 0, 0, 0);
            }
        } else {
            mListener.updateMotorReading(STATUS_NO_MOTOR, 0);
        }

    }

    @Override
    public void doRobotMove(Motor motor, int throttle, int distance, boolean waitUntilComplete) {

        if (throttle > 100)
            throttle = 100;
        else if (throttle < -100)
            throttle = -100;
        if (throttle==0) {
            doRobotStopMotors();
            return;
        }

        else if (motor.getPort()!=-1) {
            byte[] message_motor = mTranslator.getMotorMessage(motor, throttle, distance, true);

            sendMessageToRobot(message_motor);
            if (waitUntilComplete) {
                int listenerType = LISTENER_MOTORS_MOVE;
                switch (motor) {
                    case A:
                        listenerType = LISTENER_MOTOR_A;
                        break;
                    case B:
                        listenerType = LISTENER_MOTOR_B;
                        break;
                    case C:
                        listenerType = LISTENER_MOTOR_C;
                        break;
                    case D:
                        listenerType = LISTENER_MOTOR_D;
                        break;

                }
                waitForRobot(listenerType, 0, 0, 0, 0, 0);
            }
        } else {
            mListener.updateMotorReading(STATUS_NO_MOTOR, 0);
        }
    }

    @Override
    public void doRobotMove(int portIndex, int throttle, int degrees, boolean blockInstruction) {
        Motor motor = Motor.getMotorForPortIndex(portIndex);
        doRobotMove(motor, throttle, degrees, blockInstruction);
    }

    /**
     * Implementation of InstructionExecutor interface. Rotates robot
     * @param throttle              int - speed of robot movement
     * @param degrees               int - 'degree' of turn
     * @param waitUntilComplete     int - wait for motors to stop before continuing
     */
    @Override
    public void doRobotRotate(int throttle, int degrees, boolean waitUntilComplete) {
        if (throttle > 100)
            throttle = 100;
        else if (throttle < -100)
            throttle = -100;
        if (throttle==0) {
            doRobotStopMotors();
            return;
        }
        if (Motor.LEFT.getPort()!=-1 && Motor.LEFT.getPort()!=-1) {
            byte[] message_leftMotor = mTranslator.getMotorMessage(Motor.LEFT, throttle, degrees, false);
            byte[] message_rightMotor = mTranslator.getMotorMessage(Motor.RIGHT, -throttle, degrees, false);
            sendMessageToRobot(message_leftMotor);
            sendMessageToRobot(message_rightMotor);
            if (waitUntilComplete) {
                waitForRobot(LISTENER_MOTORS_MOVE, 0, 0, 0, 0, 0);
            }
        } else {
            mListener.updateMotorReading(STATUS_NO_MOTOR, 0);
        }

    }

    @Override
    public void doRobotTurnArm(int throttle, int degrees, boolean waitUntilComplete) {
        if (throttle > 100)
            throttle = 100;
        else if (throttle < -100)
            throttle = -100;

        if (Motor.ARM.getPort()!=-1) {
            byte[] message_armMotor = mTranslator.getMotorMessage(Motor.ARM, throttle, degrees, false);
            sendMessageToRobot(message_armMotor);
            if (waitUntilComplete) {
                waitForRobot(LISTENER_MOTORS_ARM, 0, 0, 0, 0, 0);
            }
        } else {
            mListener.updateMotorReading(STATUS_NO_MOTOR, 0);
        }
    }
    /**
     * Implementation of InstructionExecutor interface. Resets motors' tachocount
     */
    @Override
    public void doResetMotors() {
        byte[] message0 = mTranslator.getResetMessage(Motor.RIGHT);
        byte[] message1 = mTranslator.getResetMessage(Motor.LEFT);
        byte[] message2 = mTranslator.getResetMessage(Motor.ARM);
        sendMessageToRobot(message0);
        sendMessageToRobot(message1);
        sendMessageToRobot(message2);
    }

    /**
     * Implementation of InstructionExecutor interface. Stops robot's motors
     */
    @Override
    public void doRobotStopMotors() {
        byte[] message_leftMotor = mTranslator.getMotorMessage(Motor.LEFT, 0, 0, false);
        byte[] message_rightMotor = mTranslator.getMotorMessage(Motor.RIGHT, 0, 0, false);
        byte[] message_armMotor = mTranslator.getMotorMessage(Motor.ARM, 0, 0, false);
        sendMessageToRobot(message_leftMotor);
        sendMessageToRobot(message_rightMotor);
        sendMessageToRobot(message_armMotor);
    }

    /**
     * Implementation of InstructionExecutor interface. Make a beep
     * @param tone                  int - frequency of tone
     * @param duration              int -   duration of tone in seconds
     * @param waitUntilComplete     boolean -   wait for duration before continuing
     */
    @Override
    public void doRobotBeep(int tone, int duration, boolean waitUntilComplete) {
        byte[] message = mTranslator.getBeepMessage(tone, duration);
        sendMessageToRobot(message);
        if (waitUntilComplete) {
            waitSomeTime(duration);

        }
    }


    public void doRobotBeep(int  volume, int tone, int duration, boolean waitUntilComplete) {
        byte[] message = null;
        if (Robot.getRobotType()==MODEL_EV3) {
       message = mTranslator.getBeepMessage(volume, tone, duration);
        } else {
            message = mTranslator.getBeepMessage(tone,duration);
        }
        sendMessageToRobot(message);
        if (waitUntilComplete) {
            waitSomeTime(duration);

        }
    }


    public void configurePorts() {
        if (Robot.getRobotType()==MODEL_NXT) {
            registerSensor(Sensor.LIGHT);
            registerSensor(Sensor.SOUND);
            registerSensor(Sensor.ULTRASONIC);
            registerSensor(Sensor.SWITCH);
            mListener.onPortsConfigured(STATUS_OK);

        } else {

        mListener.onPortsConfigured(STATUS_OK);
        }
    }



    /**
     * Registers a sensor type to a port
     * @param sensor        int - sensor type
     */
    @Override
    public void registerSensor(Sensor sensor) {
// for NXT only
        if (mTranslator instanceof ByteTranslatorNXT) {
        byte[] message = ((ByteTranslatorNXT)mTranslator).getSensorModeMessage(sensor);
        sendMessageToRobot(message);
        }
    }

    /**
     * Implementation of InstructionExecutor interface. Monitor robot's sensors until a target value has been reached
     * @param listenerType      int -   type of sensor
     * @param duration          int -   duration of time to listen to port
     * @param lowerTarget       int -   lowest point in range of values that needs to be reached
     * @param upperTarget       int -   highest point in range of values that needs to be reached
     * @param lowerTarget2      int -   optional second lowest point
     * @param upperTarget2      int -   option second highest point
     */
    @Override
    public void waitForRobot(int listenerType, int duration, int lowerTarget, int upperTarget, int lowerTarget2, int upperTarget2) {

        switch (listenerType) {
            case LISTENER_MOTORS_ARM:
                    mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                            new MotorListenerEV3(this, Motor.ARM, duration):
                            new MotorListenerNXT(this, Motor.ARM, duration, lowerTarget, upperTarget);

                break;
            case LISTENER_MOTORS_MOVE:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                        new MotorListenerEV3(this, Motor.LEFT, duration):
                        new MotorListenerNXT(this, Motor.LEFT, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_MOTOR_A:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                        new MotorListenerEV3(this, Motor.A, duration):
                        new MotorListenerNXT(this, Motor.A, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_MOTOR_B:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                        new MotorListenerEV3(this, Motor.B, duration):
                        new MotorListenerNXT(this, Motor.B, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_MOTOR_C:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                        new MotorListenerEV3(this, Motor.C, duration):
                        new MotorListenerNXT(this, Motor.C, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_MOTOR_D:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                        new MotorListenerEV3(this, Motor.D, duration): null;
                break;
            case LISTENER_LIGHT:
                    mRobotListener = new SensorListener(this, Sensor.LIGHT, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_SOUND:
                    mRobotListener = new SensorListener(this, Sensor.SOUND, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_SWITCH:
                    mRobotListener = new SensorListener(this, Sensor.SWITCH, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_INFRARED:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                mRobotListener = new SensorListener(this, Sensor.INFRARED, duration, lowerTarget, upperTarget):
                new DistanceSensorListenerNXT(this, (ByteTranslatorNXT)mTranslator, duration, lowerTarget, upperTarget);
                break;
            case LISTENER_ULTRASONIC:
                mRobotListener = (Robot.getRobotType()==MODEL_EV3) ?
                mRobotListener = new SensorListener(this, Sensor.ULTRASONIC, duration, lowerTarget, upperTarget):
                new DistanceSensorListenerNXT(this, (ByteTranslatorNXT)mTranslator, duration, lowerTarget, upperTarget);
                break;
            default:
                break;

        }
if (mRobotListener!=null) {
    mRobotListener.startListening();

}
    }

    /**
     * Passes sensor reading on to CommanderListener object
     * @param sensor    int - type of sensor
     * @param status        int - whether target has been reached
     * @param value         int - value of reading
     */
    @Override
    public void onSensorReport(Sensor sensor, int status, int value) {

        mListener.updateSensorReading(sensor, status, value);
    }

    /**
     * Passes motor reading on to CommanderListener object
     * @param motor     int - motor port
     * @param status    int - status of motor
     * @param value     int - tacho count of motor
     */
    @Override
    public void onMotorReport(Motor motor, int status, int value) {
        switch (status) {
            case STATUS_MOTORS_STOPPED:
            case STATUS_TIMEDOUT:
                byte[] message = mTranslator.getResetMessage(Motor.LEFT);
                sendMessageToRobot(message);
                message = mTranslator.getResetMessage(Motor.RIGHT);
                sendMessageToRobot(message);
                break;

            case STATUS_NO_MOTOR:
                mListener.updateMotorReading(status,value);
            default:
                break;
        }


    }

    /**
     * Run robot program in a seperate thread
     * @param program   Program - collection of Instructions to execute
     */
    public void runRobotProgram(Program program) {

        // waitForRobot(LISTENER_SWITCH,10000,5,5,0,0);




        if (programExecutionThread != null) {
            programExecutionThread.interrupt();
        }
        mProgramLooper =new RobotProgramLooper(new ThreadHandler(), this, program);
        programExecutionThread = new Thread(mProgramLooper);
        programExecutionThread.start();


    }

    public void stopRobotProgram() {
        if (mRobotListener!=null) {
            mRobotListener.endListening();
        }
        doRobotStopMotors();

        if (programExecutionThread != null) {
            programExecutionThread.interrupt();
        }
        if (fileUploadThread != null) {
            fileUploadThread.interrupt();
        }
        if (mProgramLooper!=null)
            mProgramLooper.endProgram();
        if (mSoundUploader!=null)
            mSoundUploader.quitUploading();

        Instruction stopSound = AndroidStopSound.getInstance();
        stopSound.executeInstruction(this);
    }

    @Override
    public void detectSensorPorts() {
        if (Robot.getRobotType()==MODEL_EV3) {
// set all ports to -1 (Undetected)
        Sensor.SOUND.setPort(-1);
        Sensor.LIGHT.setPort(-1);
        Sensor.SWITCH.setPort(-1);
        Sensor.ULTRASONIC.setPort(-1);
            Sensor.INFRARED.setPort(-1);

        for (int i=0;i<4;i++) {


            byte[] message = mTranslator.getInputTypeAndModeMessage(i);
            sendMessageToRobot(message);
            byte[] returnMessage = receiveMessageFromRobot();
            if(returnMessage[2] == 2)
            {
                switch (returnMessage[3]) {
                    case 3:
                    case 1:
                        Sensor.SOUND.setPort(i);
                        break;
                    case 2:
                    case 29:
                        Sensor.LIGHT.setPort(i);
                        break;
                    case 16:
                        Sensor.SWITCH.setPort(i);
                        break;
                    case 30:
                        Sensor.ULTRASONIC.setPort(i);
                        break;
                    case 33:
                        Sensor.INFRARED.setPort(i);
                        break;
                }


            } else
            {
                Log.d("CommanderEV3","Error returned "+returnMessage[2]);
                mListener.onPortsDetected(STATUS_UNKNOWN);
            }


        }
        }
        mListener.onPortsDetected(STATUS_OK);


    }



    public ByteTranslator getTranslator() {
        if (mTranslator != null) {
            return mTranslator;
        }
        return null;
    }

    public boolean sendMessageToRobot(byte[] message) {
        try {
            mBTComms.sendByteMessageToRobot(message);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Communication failure: " + e.getMessage());
            if (mProgramLooper!=null && mProgramLooper.isRunning()) {
            mListener.onCommunicationFailure(STATUS_CONNECTIONERROR);
                mProgramLooper.endProgram();

        }
        return false;
    }

    }

    public byte[] receiveMessageFromRobot() {

        try {
            byte[] message = mBTComms.receiveByteMessageFromRobot();
            waitForRobotLatencyPeriod();
            return message;
        } catch (IOException e) {
            if (mProgramLooper!=null && mProgramLooper.isRunning()) {
            mListener.onCommunicationFailure(STATUS_CONNECTIONERROR);
            Log.e(TAG, "Communication failure: " + e.getMessage());
                mProgramLooper.endProgram();
            }


        }
        return null;
    }



    @Override
    public void waitSomeTime(int millis) {

        long endTime = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                    Log.e(TAG,"Failed to wait: "+e.getMessage());
                }
            }
        }
    }

    private void waitForRobotLatencyPeriod() {
        waitSomeTime(100);


    }
}
