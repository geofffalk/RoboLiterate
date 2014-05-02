/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */

package org.maskmedia.roboliterate.robotcomms.ev3;

import android.util.Log;

import org.maskmedia.roboliterate.robotcomms.BluetoothCommunicator;
import org.maskmedia.roboliterate.robotcomms.ByteTranslator;
import org.maskmedia.roboliterate.robotcomms.Commander;
import org.maskmedia.roboliterate.robotcomms.Robot;
import org.maskmedia.roboliterate.robotcomms.Robot.Motor;
import org.maskmedia.roboliterate.robotcomms.RobotListener;

import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_MOTORS_STOPPED;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_TIMEDOUT;

/**
 * Listens to Motor ports and reports readings to Commander object.
 */
public class MotorListenerEV3 implements RobotListener {

    private static final String TAG = "MotorListenerEV3";
    private static final boolean D = true;

    private Commander mCommander;
    private BluetoothCommunicator btComms;
    private int mDuration;
    private ByteTranslator mTranslator;
    private Motor mMotor;
    private boolean isListening;


    public MotorListenerEV3(Commander commander, Motor motor, int duration) {
        if (D) Log.d(TAG, "Init MotorListenerEV3");
        mCommander = commander;
        mTranslator = mCommander.getTranslator();
        mMotor = motor;
        mDuration = duration;

        btComms = BluetoothCommunicator.getInstance();

    }


    public int getScaledReading() {
        return 0;
    }


    @Override
    public void endListening() {
        isListening = false;
    }


    /**
     * Carry on listening to motor port until target has been reached. Send updated readings to Commander
     * object
     */
    public void startListening() {
        if (mMotor.getPort()==-1) {
            mCommander.onMotorReport(mMotor, Commander.STATUS_NO_MOTOR, 0);
        } else {
        isListening = true;
        long endTime = System.currentTimeMillis() + ((mDuration > 0) ? mDuration * 1000 : 600000);
        while (isListening && btComms.isConnected() && !Thread.currentThread().isInterrupted() && (System.currentTimeMillis() < endTime)) {


    byte[] message = mTranslator.getOutputStateMessage(mMotor);
            mCommander.sendMessageToRobot(message);
            byte[] returnMessage = mCommander.receiveMessageFromRobot();
            for (int i=0;i<=3;i++) {
                Log.d(TAG, " RETURN motor message " + i + " : "+returnMessage[i]);
            }
        Log.d(TAG,""+"\n");
           if (mTranslator.validateStatusMessage(returnMessage, Robot.COMMAND_GET_OUTPUT_STATE)) {
      if (returnMessage[3]==0) {
                        if (D) Log.d(TAG, "TARGET REACHED - STOPPING");

                        mCommander.onMotorReport(mMotor, STATUS_MOTORS_STOPPED, 0);
                        break;
                    }
                } else {
               isListening = false;
           }
          }

        mCommander.onMotorReport(mMotor, STATUS_TIMEDOUT, 0);
        }
    }




    }


