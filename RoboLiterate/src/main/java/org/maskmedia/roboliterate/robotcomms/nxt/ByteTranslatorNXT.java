/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 * Adapted from Lego's MINDdroid app, LCPMessage.class
 * Copyright 2010 Guenther Hoelzl, Shawn Brown
 * https://github.com/NXT/LEGO-MINDSTORMS-MINDdroid
 */



package org.maskmedia.roboliterate.robotcomms.nxt;

import android.util.Log;

import org.maskmedia.roboliterate.robotcomms.ByteTranslator;
import org.maskmedia.roboliterate.robotcomms.Commander;
import org.maskmedia.roboliterate.robotcomms.Robot;
import org.maskmedia.roboliterate.robotcomms.Robot.Sensor;

public class ByteTranslatorNXT implements ByteTranslator {
    private static final String TAG = "ByteTranslatorNXT";

    // the following constants were taken from the leJOS project (http://www.lejos.org)


    //Sensor constants
    private static byte SENSOR_TYPE_SWITCH = 0x01;
    private static byte SENSOR_TYPE_LOWSPEED_9V = 0x0B;
    private static byte SENSOR_TYPE_LIGHT = 0x05;
    private static byte SENSOR_TYPE_LIGHT_INACTIVE = 0x06;
    private static byte SENSOR_TYPE_SOUND = 0x07;
    private static byte SENSOR_MODE_RAW = 0x00;
    private static byte SENSOR_MODE_BOOLEAN = 0x20;
    private static byte SENSOR_MODE_PCTFULLSCALEMODE = (byte)0x80;

    // Command types constants. Indicates type of packet being sent or received.
    private static byte DIRECT_COMMAND_REPLY = 0x00;
    private static byte SYSTEM_COMMAND_REPLY = 0x01;
    private static byte REPLY_COMMAND = 0x02;
    private static byte DIRECT_COMMAND_NOREPLY = (byte) 0x80; // Avoids ~100ms latency
    private static byte SYSTEM_COMMAND_NOREPLY = (byte) 0x81; // Avoids ~100ms latency

    // Direct Commands
    private static final byte PLAY_SOUND_FILE = 0x02;
    private static final byte PLAY_TONE = 0x03;
    private static final byte SET_OUTPUT_STATE = 0x04;
    private static final byte SET_INPUT_MODE = 0x05;
    private static final byte GET_OUTPUT_STATE = 0x06;
    private static final byte GET_INPUT_VALUES = 0x07;
    private static final byte RESET_SCALED_INPUT_VALUE = 0x08;
    private static final byte RESET_MOTOR_POSITION = 0x0A;
    private static final byte STOP_SOUND_PLAYBACK = 0x0C;
    private static final byte LS_GET_STATUS = 0x0E;
    private static final byte LS_WRITE = 0x0F;
    private static final byte LS_READ = 0x10;


    // System Commands:
    private static final byte OPEN_WRITE = (byte) 0x81;
    private static final byte WRITE = (byte) 0x83;
    private static final byte CLOSE = (byte) 0x84;
    private static final byte DELETE = (byte) 0x85;

    //Reply status codes
    private static final byte STATUS_OK = (byte) 0x00;
    private static final byte STATUS_UNKNOWN = (byte) 0x03;
    private static final byte STATUS_INSUFFICIENT_MEMORY = (byte) 0xFB;
    private static final byte STATUS_ILLEGAL_FILENAME = (byte) 0x92;

    private static ByteTranslator instance;

    private ByteTranslatorNXT() {
    }

    public static ByteTranslator getInstance() {
        if (instance == null) {
            instance = new ByteTranslatorNXT();
        }
        return instance;
    }


    public byte[] getBeepMessage(int frequency, int duration) {
        byte[] message = new byte[6];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = PLAY_TONE;
        // Frequency for the tone, Hz (UWORD); Range: 200-14000 Hz
        message[2] = (byte) frequency;
        message[3] = (byte) (frequency >> 8);
        // Duration of the tone, ms (UWORD)
        message[4] = (byte) duration;
        message[5] = (byte) (duration >> 8);

        return message;
    }

    @Override
    public byte[] getBeepMessage(int volume, int frequency, int duration) {
        return new byte[0];
    }

    public byte[] getMotorMessage(Robot.Motor motor, int speed, boolean sync) {
        byte[] message = new byte[12];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = SET_OUTPUT_STATE;
        // Output port
        message[2] = (byte) motor.getPort();

        if (speed == 0) {
            message[3] = 0;
            message[4] = 0;
            message[5] = 0;
            message[6] = 0;
            message[7] = 0;

        } else {
            // Power set option (Range: -100 - 100)
            message[3] = (byte) speed;
            // Mode byte (Bit-field): MOTORON + BREAK
            message[4] = 0x03;
            // Regulation mode: REGULATION_MODE_MOTOR_SPEED
            if (sync)
                message[5] = 0x02;
            else
                message[5] = 0x01;
            // Turn Ratio (SBYTE; -100 - 100)
            message[6] = 0x00;
            // RunState: MOTOR_RUN_STATE_RUNNING
            message[7] = 0x20;
        }

        // TachoLimit: run forever
        message[8] = 0;
        message[9] = 0;
        message[10] = 0;
        message[11] = 0;

        return message;

    }

    public byte[] getPlaySoundFileMessage(String filename, boolean loop) {
        byte[] message = new byte[22];
        message[0] = DIRECT_COMMAND_NOREPLY;
        //   message[0] = DIRECT_COMMAND_REPLY;
        message[1] = PLAY_SOUND_FILE;
        message[2] = (loop) ? (byte) 0x01 : (byte) 0x00;
        // copy programName and end with 0 delimiters
        for (int pos = 0; pos < filename.length(); pos++)
            message[3 + pos] = (byte) filename.charAt(pos);
        for (int pos = filename.length() + 3; pos < 22; pos++) {
            message[pos] = 0x00;
        }

        return message;
    }

    public byte[] getMotorMessage(Robot.Motor motor, int speed, int end, boolean sync) {
        byte[] message = getMotorMessage(motor, speed, sync);

        // TachoLimit
        message[8] = (byte) end;
        message[9] = (byte) (end >> 8);
        message[10] = (byte) (end >> 16);
        message[11] = (byte) (end >> 24);

        return message;
    }

    public byte[] getResetMessage(Robot.Motor motor) {
        byte[] message = new byte[4];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = RESET_MOTOR_POSITION;
        // Output port
        message[2] = (byte) motor.getPort();
        // absolute position
        //message[3] = 0;
        // relative position
        message[3] = 0;

        return message;
    }


    public byte[] getStopSoundMessage() {
        byte[] message = new byte[2];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = STOP_SOUND_PLAYBACK;

        return message;
    }

    public byte[] getOutputStateMessage(Robot.Motor motor) {
        byte[] message = new byte[3];

        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = GET_OUTPUT_STATE;
        // Output port
        message[2] = (byte) motor.getPort();

        return message;
    }


    public byte[] getInputValueMessage(Sensor sensor) {
        byte[] message = new byte[3];
        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = GET_INPUT_VALUES;
        message[2] = (byte) sensor.getPort();

        return message;
    }

    public byte[] getOpenWriteMessage(String fileName, int fileLength) {
        byte[] message = new byte[26];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = OPEN_WRITE;

        // copy programName and end with 0 delimiter
        for (int pos = 0; pos < fileName.length(); pos++)
            message[2 + pos] = (byte) fileName.charAt(pos);

        message[fileName.length() + 2] = 0;
        // copy file size
        message[22] = (byte) fileLength;
        message[23] = (byte) (fileLength >>> 8);
        message[24] = (byte) (fileLength >>> 16);
        message[25] = (byte) (fileLength >>> 24);
        return message;
    }

    public byte[] getDeleteMessage(String fileName) {
        byte[] message = new byte[22];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = DELETE;

        // copy programName and end with 0 delimiter
        for (int pos = 0; pos < fileName.length(); pos++)
            message[2 + pos] = (byte) fileName.charAt(pos);

        message[fileName.length() + 2] = 0;
        return message;
    }


    public byte[] getWriteMessage(int handle, byte[] data, int dataLength) {
        byte[] message = new byte[dataLength + 3];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = WRITE;

        // copy handle
        message[2] = (byte) handle;
        // copy data
        System.arraycopy(data, 0, message, 3, dataLength);

        return message;
    }

    public byte[] getCloseMessage(int handle) {
        byte[] message = new byte[3];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = CLOSE;

        // copy handle
        message[2] = (byte) handle;

        return message;
    }

    public byte[] getSensorModeMessage(Sensor sensor) {

        byte[] message = new byte[5];
        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = SET_INPUT_MODE;
        message[2] = (byte) sensor.getPort();
        switch (sensor) {
            case SOUND:
                message[3] = SENSOR_TYPE_SOUND;
                message[4] = SENSOR_MODE_PCTFULLSCALEMODE;
                break;
            case SWITCH:
                message[3] = SENSOR_TYPE_SWITCH;
                message[4] = SENSOR_MODE_BOOLEAN;
                break;
            case ULTRASONIC:
                message[3] = SENSOR_TYPE_LOWSPEED_9V;
                message[4] = SENSOR_MODE_RAW;
                break;
            case LIGHT:
                message[3] = SENSOR_TYPE_LIGHT;
                message[4] = SENSOR_MODE_PCTFULLSCALEMODE;
                break;
            default:
                message[3] = SENSOR_TYPE_SOUND;
                message[4] = SENSOR_MODE_RAW;
                break;
        }
        return message;
    }

    public byte[] getLSWriteMessage(int port) {
// This tells the sensor you want 1 byte from register 0x42 which according
// to the data sheet is ‘Measurement Byte 0’
        byte[] message = new byte[7];
        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = LS_WRITE;
        message[2] = (byte) port;
        message[3] = (byte) 0x02;
        message[4] = (byte) 0x01;
        message[5] = (byte) 0x02;
        message[6] = (byte) 0x42;
        return message;
    }

    public byte[] getLSGetStatusMessage(int port) {
        byte[] message = new byte[3];
        message[0] = REPLY_COMMAND;
        message[1] = LS_GET_STATUS;
        message[2] = (byte) port;
        return message;
    }

    public byte[] getLSReadMessage(int port) {
// This tells the sensor you want 1 byte from register 0x42 which according
// to the data sheet is ‘Measurement Byte 0’
        byte[] message = new byte[3];
        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = LS_READ;
        message[2] = (byte) port;
        return message;
    }

    public byte getFileHandleFromMessage(byte[] message) {
        return message[3];
    }


    public int translateStatusMessage(byte[] message, int commandName) {
        byte command = 0x00;
        switch (commandName) {
            case Robot.COMMAND_WRITE:
                command = WRITE;
                break;
            case Robot.COMMAND_CLOSE:
                command = CLOSE;
                break;
            case Robot.COMMAND_OPEN_WRITE:
                command = OPEN_WRITE;
                break;
            default:
                break;
        }
        if (message != null) {
            if (message[0] == REPLY_COMMAND && message[1] == command) {
                switch (message[2]) {
                    case STATUS_OK:
                        return Commander.STATUS_OK;
                    case STATUS_INSUFFICIENT_MEMORY:
                        return Commander.STATUS_INSUFFICIENT_MEMORY;
                    case STATUS_UNKNOWN:
                        return Commander.STATUS_UNKNOWN;
                    default:
                        break;
                }
            }
        }
        return Commander.STATUS_NULL_MESSAGE;
    }


    @Override
    public boolean validateStatusMessage(byte[] message, int command) {
        boolean success = false;
        if (message!=null) {
        if (message[0] == REPLY_COMMAND) {

            switch (command) {
                case Robot.COMMAND_GET_INPUT_VALUES:
                    if ((message.length >= 15) && (message[1] == GET_INPUT_VALUES))
                        success=true;
                    break;
                case Robot.COMMAND_GET_OUTPUT_STATE:
                    if ((message.length >= 24) && (message[1] == GET_OUTPUT_STATE))
                        success=true;
                    break;
                case Robot.COMMAND_LS_READ:
                    if (message.length >= 20 && message[1] == LS_READ && message[2] == 0)
                        success=true;
                    break;
                case Robot.COMMAND_LS_GET_STATUS:
                    if (message.length >= 4 && message[1] == LS_GET_STATUS && message[2] == 0 && message[3] >= 1)
                        success=true;
                    break;
                default:
                    success=false;
            }
        }
        }
        return success;
    }

    @Override
    public byte[] getInputTypeAndModeMessage(int i) {
        // EV3 ONLY
        return new byte[0];
    }

    @Override
    public byte[] getInputPercentMessage(Sensor sensor) {
        return new byte[0];
    }

    @Override
    public int getSensorReadingFromMessage(byte[] message, int readingType) {
        try {
            switch (readingType) {
                case Robot.SCALED_VALUE:
                    return (message[12] + (message[13] << 8));
                case Robot.NORMALIZED_VALUE:
                    return (message[10] + (message[11] << 8));
                case Robot.RAW_VALUE:
                    return (message[8] + (message[9] << 8));
                default:
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Cannot retrieve sensor reading from this message, which is too short");
        }
        return 0;
    }

    public int getDistanceReadingFromMessage(byte[] message) {
        try {
            return message[4];
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Cannot retrieve distance reading from this message, which is too short");
        }
        return 0;
    }

    @Override
    public int getTachoCountFromMessage(byte[] message) {
        int count = 0;
        try {
            count = Math.abs(message[13] + (message[14] << 8) + (message[15] << 16) + (message[16] << 24));
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Cannot retrieve tacho count from this message, which is too short");
        }
        return count;
    }
}