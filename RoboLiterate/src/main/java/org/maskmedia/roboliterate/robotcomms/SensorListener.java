/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.robotcomms;

import android.util.Log;

import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_COMPLETE;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_NO_SENSOR;
import static org.maskmedia.roboliterate.robotcomms.Commander.STATUS_OK;
import static org.maskmedia.roboliterate.robotcomms.Robot.SCALED_VALUE;
import static org.maskmedia.roboliterate.robotcomms.Robot.Sensor;

/**
 * Listens to sensor and reports values to Commander object
 */
public class SensorListener implements RobotListener {


    private static final String TAG = "SensorListenerEV3";
    private Commander mCommander;
    private BluetoothCommunicator btComms;
    private ByteTranslator mTranslator;
    private Sensor mSensor;
    private int mUpperLimit;
    private int mLowerLimit;
    private int mUpperLimit2;
    private int mLowerLimit2;
    private int mDuration;
    private boolean isListening;



    public SensorListener(Commander rc, Sensor sensor, int duration, int lowerLimit, int upperLimit) {
        mCommander = rc;
        mTranslator = mCommander.getTranslator();
        mUpperLimit = upperLimit;
        mLowerLimit = lowerLimit;
        mSensor = sensor;
        mDuration = duration;
        btComms = BluetoothCommunicator.getInstance();
    }

    public SensorListener(Commander rc, Sensor sensor, int duration, int lowerLimit, int upperLimit, int lowerLimit2, int upperLimit2) {
        this(rc, sensor, duration, lowerLimit, upperLimit);
        mUpperLimit2 = upperLimit2;
        mLowerLimit2 = lowerLimit2;
    }

    /**
     * Get scaled value from robot via Translator object and using Commander object as communication
     * channel
     *
     * @return int - scaled reading
     */
    public int getScaledReading() {
        int scaledValue = 0;
        byte[] message;
//        if (mSensor == Sensor.LIGHT) {
//        message = mTranslator.getInputPercentMessage(mSensor);
//            mCommander.sendMessageToRobot(message);
//            byte[] returnMessage = mCommander.receiveMessageFromRobot();
//            //     if (mTranslator.validateStatusMessage(returnMessage, COMMAND_GET_INPUT_VALUES)) {
//            scaledValue = mTranslator.getSensorReadingFromMessage(returnMessage, PERCENT_VALUE);
//
//        } else {
            message = mTranslator.getInputValueMessage(mSensor);
            mCommander.sendMessageToRobot(message);
            byte[] returnMessage = mCommander.receiveMessageFromRobot();
         if (mTranslator.validateStatusMessage(returnMessage, Robot.COMMAND_GET_INPUT_VALUES)) {
            scaledValue = mTranslator.getSensorReadingFromMessage(returnMessage, SCALED_VALUE);

   } else {
               isListening = false;
           }

        return scaledValue;
     //   }

    }




    @Override
    public void endListening() {
        isListening = false;
    }


    /**
     * Called by Commander to begin listening to sensor. Only exits once target reading has been met.
     */
    public void startListening() {
        if (mSensor.getPort()==-1) {
            mCommander.onSensorReport(mSensor, STATUS_NO_SENSOR, 0);
        } else {
        isListening = true;
        Log.d(TAG, "starting to listen ");
        boolean targetFound = false;
        long endTime = System.currentTimeMillis() + ((mDuration > 0) ? mDuration * 1000 : 600000);
        while (isListening && btComms.isConnected() && !Thread.currentThread().isInterrupted()
                && (System.currentTimeMillis() < endTime)) {

            int scaledValue = getScaledReading();
            mCommander.onSensorReport(mSensor, STATUS_OK, scaledValue);
            if (scaledValue <= (mUpperLimit) && scaledValue >= (mLowerLimit) || ((mUpperLimit2 - mLowerLimit2 != 0) &&
                    (scaledValue >= mLowerLimit2 && scaledValue <= mUpperLimit2))) {
                mCommander.onSensorReport(mSensor, STATUS_COMPLETE, scaledValue);
                targetFound = true;
                break;
            }
        }
        if (!targetFound) {
            mCommander.onSensorReport(mSensor, Commander.STATUS_SENSOR_ERROR, 0);

        }
    }
    }




}
