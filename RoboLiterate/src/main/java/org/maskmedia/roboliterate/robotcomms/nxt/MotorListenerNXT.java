/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */

package org.maskmedia.roboliterate.robotcomms.nxt;

import android.util.Log;

import org.maskmedia.roboliterate.robotcomms.BluetoothCommunicator;
import org.maskmedia.roboliterate.robotcomms.Commander;
import org.maskmedia.roboliterate.robotcomms.Robot;
import org.maskmedia.roboliterate.robotcomms.RobotListener;
import org.maskmedia.roboliterate.robotcomms.ByteTranslator;

import static org.maskmedia.roboliterate.robotcomms.Robot.*;

/**
 * Listens to Motor ports and reports readings to Commander object.
 */
public class MotorListenerNXT implements RobotListener {

    private static final String TAG = "MotorListenerNXT";
    private static final boolean D = true;

    private Commander mCommander;
    private BluetoothCommunicator btComms;
    private int mUpperLimit;
    private int mLowerLimit;
    private int mUpperLimit2;
    private int mLowerLimit2;
    private int mDuration;
    private ByteTranslator mTranslator;
    private Motor mMotor;
    private boolean isListening;


    public MotorListenerNXT(Commander commander, Motor motor, int duration, int lowerLimit, int upperLimit) {
        if (D) Log.d(TAG, "Init MotorListenerNXT");
        mCommander = commander;
        mTranslator = mCommander.getTranslator();
        mMotor = motor;
        mDuration = duration;
        mUpperLimit = upperLimit;
        mLowerLimit = lowerLimit;
        btComms = BluetoothCommunicator.getInstance();

    }

    public MotorListenerNXT(Commander commander, Motor motor, int duration, int lowerLimit, int upperLimit, int lowerLimit2, int upperLimit2) {
        this(commander, motor, duration, lowerLimit, upperLimit);
        mLowerLimit2 = lowerLimit2;
        mUpperLimit2 = upperLimit2;

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
        isListening = true;
        int oldTachoCount = 0;
        long endTime = System.currentTimeMillis() + ((mDuration > 0) ? mDuration * 1000 : 600000);
        while (isListening && btComms.isConnected() && !Thread.currentThread().isInterrupted() && (System.currentTimeMillis() < endTime)) {

            byte[] message = mTranslator.getOutputStateMessage(mMotor);
            mCommander.sendMessageToRobot(message);
            byte[] returnMessage = mCommander.receiveMessageFromRobot();
            if (mTranslator.validateStatusMessage(returnMessage, Robot.COMMAND_GET_OUTPUT_STATE)) {
                int newTachoCount = mTranslator.getTachoCountFromMessage(returnMessage);
                if (oldTachoCount == 0) {
                    oldTachoCount = newTachoCount;
                } else {
                    int diff = newTachoCount - oldTachoCount;
                    if (D) Log.d(TAG, "Acceleration is " + diff);
                    if ((diff >= (mLowerLimit) && diff <= (mUpperLimit)) || ((mUpperLimit2 - mLowerLimit2 != 0) &&
                            (diff >= mLowerLimit2 && diff <= mUpperLimit2))) {

                        if (D) Log.d(TAG, "TARGET REACHED - STOPPING");

                        mCommander.onMotorReport(mMotor, Commander.STATUS_MOTORS_STOPPED, 0);
                        break;
                    } else {
                        oldTachoCount = newTachoCount;
                    }
                }
            } else {
                isListening = false;
            }


        }
        mCommander.onMotorReport(mMotor, Commander.STATUS_TIMEDOUT, 0);
    }

}
