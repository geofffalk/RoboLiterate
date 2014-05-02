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

import static org.maskmedia.roboliterate.robotcomms.Robot.Sensor;

/**
 * Monitor ultrasonic sensors for Commander class
 * Specialist class needed as ultrasonic has i2c bus
 */
public class DistanceSensorListenerNXT implements RobotListener {

    private static final String TAG = "SensorListenerNXT";
    private Commander mCommander;
    private BluetoothCommunicator btComms;
    private ByteTranslatorNXT mTranslator;
    private int mUpperLimit;
    private int mLowerLimit;
    private int mUpperLimit2;
    private int mLowerLimit2;
    private int mDuration;
    private boolean isListening;


    public DistanceSensorListenerNXT(Commander commander, ByteTranslatorNXT translator, int duration, int lowerLimit, int upperLimit) {
        mCommander = commander;
        mTranslator = translator;
        mDuration = duration;
        mUpperLimit = upperLimit;
        mLowerLimit = lowerLimit;
        btComms = BluetoothCommunicator.getInstance();

    }

    public DistanceSensorListenerNXT(Commander commander, ByteTranslatorNXT translator, int duration, int lowerLimit, int upperLimit, int lowerLimit2, int upperLimit2) {
        this(commander, translator, duration, lowerLimit, upperLimit);
        mUpperLimit2 = upperLimit2;
        mLowerLimit2 = lowerLimit2;
    }

    /**
     * Gets Scaled readings from Ultrasonic sensor through polling using LSWrite, LSGetStatus then LSRead
     * @return  int - scaled reading
     */
    public int getScaledReading() {
        int distanceValue = 0;
        int port = Robot.Sensor.ULTRASONIC.getPort();

        // Send LSWrite message to robot

        byte[] message = mTranslator.getLSWriteMessage(port);
        mCommander.sendMessageToRobot(message);

        // Poll robot with LSGetStatus until status ok

        message = mTranslator.getLSGetStatusMessage(port);
        byte[] returnMessage = mCommander.receiveMessageFromRobot();
        while (btComms.isConnected() &&  !Thread.currentThread().isInterrupted() && returnMessage!=null && returnMessage.length>3 &&
                returnMessage[2]!=0 && returnMessage[3]<1) {
            mCommander.sendMessageToRobot(message);
            returnMessage = mCommander.receiveMessageFromRobot();
        }

        // Now read the data using LSRead

        message = mTranslator.getLSReadMessage(port);
        mCommander.sendMessageToRobot(message);
        returnMessage = mCommander.receiveMessageFromRobot();
        if (mTranslator.validateStatusMessage(returnMessage, Robot.COMMAND_LS_READ)) {
            distanceValue = mTranslator.getDistanceReadingFromMessage(returnMessage);
        } else {
            isListening = false;
        }
        return distanceValue;
    }

    @Override
    public void endListening() {
        isListening = false;
    }

    /**
     * Main function to start listening to Ultrasonic port. Loops until target reading is met
     * Sends updates as they are sent by robot to Commander object
     */
    public void startListening() {
        isListening = true;
        boolean targetFound = false;
        long endTime = System.currentTimeMillis() + ((mDuration > 0) ? mDuration * 1000 : 600000);
        while (isListening && btComms.isConnected() && !Thread.currentThread().isInterrupted()
                && (System.currentTimeMillis() < endTime)) {

            int currentReading = getScaledReading();
            Log.d(TAG,"Inside listening loop " + currentReading);
            mCommander.onSensorReport(Sensor.ULTRASONIC, Commander.STATUS_OK, currentReading);
            if ((currentReading >= (mLowerLimit) && currentReading <= (mUpperLimit)) || ((mUpperLimit2 - mLowerLimit2 != 0) &&
                    (currentReading >= mLowerLimit2 && currentReading <= mUpperLimit2))) {
                mCommander.onSensorReport(Sensor.ULTRASONIC, Commander.STATUS_COMPLETE, currentReading);
                targetFound = true;
                break;
            }

        }
        if (!targetFound)
            mCommander.onSensorReport(Sensor.ULTRASONIC, Commander.STATUS_TIMEDOUT, 0);


    }


}
