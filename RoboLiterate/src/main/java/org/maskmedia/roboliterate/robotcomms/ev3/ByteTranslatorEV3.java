/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 * Adapted from Lego's MINDdroid app, LCPMessage.class
 * Copyright 2010 Guenther Hoelzl, Shawn Brown
 * https://github.com/NXT/LEGO-MINDSTORMS-MINDdroid
 */



package org.maskmedia.roboliterate.robotcomms.ev3;

import android.util.Log;

import org.maskmedia.roboliterate.robotcomms.ByteTranslator;
import org.maskmedia.roboliterate.robotcomms.Robot;
import org.maskmedia.roboliterate.robotcomms.Robot.Sensor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class ByteTranslatorEV3 implements ByteTranslator {
    private static final String TAG = "ByteTranslatorEV3";
    private static final byte INPUT_SUCCESS = (byte) 2;
    private static final byte BYTE_LENGTH = -127;
    private static byte INT_LENGTH = -125;
    private static byte SHORT_LENGTH = -126;
    private static byte STRING_LENGTH = -124;
    private static byte RESPONSE_INDEX = -31;
    private static byte DIRECT_COMMAND_REPLY = 0x00;
    private static byte DIRECT_COMMAND_NOREPLY = -128;
    private static final byte OUTPUT_TEST = -87;
    private static final byte OUTPUT_STOP = -93;
    private static final byte GET_INPUT_VALUES = 2;
    private static final byte SOUND = (byte)0x94;
    private static final byte SOUND_TONE = 1;
    private static final byte SOUND_READY = (byte)0x96;
    private static final byte INPUT_DEVICE = -103;
    private static final byte INPUT_GET_NAME = 21;
    private static final byte READ_PCT = 27;
    private static final byte READ_SI = 29;
    private static final byte GET_MODE_NAME = 22;
    private static final byte GET_SYMBOL = 6;
    private static final byte GET_TYPEMODE = 5;
    private static final byte OUTPUT_STEP_POWER = -84;
    private static final byte OUTPUT_POWER = -92;
    private static final byte OUTPUT_RESET = -94;
    private static final byte OUTPUT_START = -90;
    private static final byte GET_OUTPUT_STATE = 0x06;
    private static final byte OUTPUT_SUCCESS = 12;


    private static ByteTranslator instance;

    private ByteTranslatorEV3() {
    }

    public static ByteTranslator getInstance() {
        if (instance == null) {
            instance = new ByteTranslatorEV3();
        }
        return instance;
    }




    @Override
    public boolean validateStatusMessage(byte[] message, int command) {
        boolean success = false;
        if (message!=null) {


                switch (command) {
                    case Robot.COMMAND_GET_INPUT_VALUES:
                        if (message[2] == INPUT_SUCCESS)
                            success=true;
                        break;
                    case Robot.COMMAND_GET_OUTPUT_STATE:
                        if (message[1] == OUTPUT_SUCCESS)
                            success=true;
                        break;
                    default:
                        success=false;
                }

        }
        return success;
    }

    @Override
    public int getTachoCountFromMessage(byte[] message) {
        return 0;
    }


    @Override
    public int getSensorReadingFromMessage(byte[] message, int readingType) {
        try {
            switch (readingType) {
                case Robot.SCALED_VALUE:
                    float f = ByteBuffer.wrap(Arrays.copyOfRange(message, 3, message.length)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    Log.d(TAG,"Scaled value: "+f);

                    return (int)f;
                case Robot.PERCENT_VALUE:
                    Log.d(TAG,"Percent value: "+message[3]);
                    return (message[3]);
                default:
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Cannot retrieve sensor reading from this message, which is too short");
        }
        return 0;
    }


    // Functions Specific to EV3
    // Based on EV3Drive




    public byte[] getInputNameMessage(int port) {


        byte[] message = new byte[15];


        message[0]=0;
        message[1]=15;
        message[2]=DIRECT_COMMAND_REPLY;
        message[3]=127;
        message[4]=0;
        message[5]=INPUT_DEVICE;
        message[6]=INPUT_GET_NAME;
        message[7]=BYTE_LENGTH;
        message[8]=0;
        message[9]=BYTE_LENGTH;
        message[10]=(byte)port;
        message[11]=BYTE_LENGTH;
        message[12]=127;
        message[13]=RESPONSE_INDEX;
        message[14]=0;


        return message;
    }



    public byte[] getInputValueMessage(Sensor sensor) {
        byte[] message = new byte[19];
        message[0]=0;
        message[1]=19;
        message[2]=DIRECT_COMMAND_REPLY;
        message[3]=4; // global buffer size
        message[4]=0; // local buffer size
        message[5]=INPUT_DEVICE;
        message[6]=READ_SI;
        message[7]=BYTE_LENGTH;
        message[8]=0;
        message[9]=BYTE_LENGTH;
        message[10]=(byte)sensor.getPort();
        message[11]=BYTE_LENGTH;
        message[12]=(byte)sensor.getType();
        message[13]=BYTE_LENGTH;
        message[14]=(byte)sensor.getMode();
        message[15]=BYTE_LENGTH;
        message[16]=1;   // flag = true
        message[17]=RESPONSE_INDEX;
        message[18]=0;

        return message;
    }

    @Override
    public byte[] getInputPercentMessage(Sensor sensor) {

        byte[] message = new byte[19];
        message[0]=0;
        message[1]=19;
        message[2]=DIRECT_COMMAND_REPLY;
        message[3]=1; // global buffer size
        message[4]=0; // local buffer size
        message[5]=INPUT_DEVICE;
        message[6]=READ_PCT;
        message[7]=BYTE_LENGTH;
        message[8]=0;
        message[9]=BYTE_LENGTH;
        message[10]=(byte)sensor.getPort();
        message[11]=BYTE_LENGTH;
        message[12]=(byte)sensor.getType();
        message[13]=BYTE_LENGTH;
        message[14]=(byte)sensor.getMode();
        message[15]=BYTE_LENGTH;
        message[16]=1;   // flag = true
        message[17]=RESPONSE_INDEX;
        message[18]=0;

        return message;
    }




    public byte[] getInputModeNameMessage(int port, int mode) {

        byte[] message = new byte[17];
        message[0]=0;
        // Length param2
        message[1]=17;
        message[2]=DIRECT_COMMAND_REPLY;
        message[3]=127; // global buffer size
        message[4]=0; // local buffer size
        message[5]=INPUT_DEVICE;
        message[6]=GET_MODE_NAME;
        message[7]=BYTE_LENGTH;
        message[8]=0;
        message[9]=BYTE_LENGTH;
        message[10]=(byte)port;
        message[11]=BYTE_LENGTH;
        message[12]=(byte)mode;
        message[13]=BYTE_LENGTH;
        message[14]=127;
        message[15]=RESPONSE_INDEX;
        message[16]=0;

        return message;

    }

    public byte[] getInputSymbolMessage(int port) {

        byte[] message = new byte[15];
        message[0]=0;
        // Length param2
        message[1]=15;
        message[2]=DIRECT_COMMAND_REPLY;
        message[3]=2; // global buffer size
        message[4]=0; // local buffer size
        message[5]=INPUT_DEVICE;
        message[6]=GET_SYMBOL;
        message[7]=BYTE_LENGTH;
        message[8]=0;
        message[9]=BYTE_LENGTH;
        message[10]=(byte)port;
        message[11]=BYTE_LENGTH;
        message[12]=16;
        message[13]=RESPONSE_INDEX;
        message[14]=0;

        return message;

    }

    public byte[] getInputTypeAndModeMessage(int port) {


        byte[] message = new byte[15];
        message[0]=0;
        message[1]=15;
        message[2]=DIRECT_COMMAND_REPLY;
        message[3]=2; // global buffer size
        message[4]=0; // local buffer size
        message[5]=INPUT_DEVICE;
        message[6]=GET_TYPEMODE;
        message[7]=BYTE_LENGTH;
        message[8]=0;
        message[9]=BYTE_LENGTH;
        message[10]=(byte)port;
        message[11]=RESPONSE_INDEX;
        message[12]=0;
        message[13]=RESPONSE_INDEX;
        message[14]=1;

        return message;
        
    }



    public byte[] getBeepMessage(int frequency, int duration) {

        byte[] message = new byte[17];
        message[0]=0;
        message[1]=17;
        message[2] = DIRECT_COMMAND_NOREPLY;
        message[3]=0; // global buffer size
        message[4]=0; // local buffer size
        message[5] = SOUND;
        message[6] = BYTE_LENGTH;
        message[7] = SOUND_TONE;
        message[8] = BYTE_LENGTH;
        message[9] = 80; // volume
        message[10] = SHORT_LENGTH;
        message[11]= (byte)frequency;
        message[12] = (byte)(frequency>>8);
        message[13] = SHORT_LENGTH;
        message[14] = (byte)duration;
        message[15] = (byte)(duration>>8);
        message[16] = SOUND_READY;


        return message;
    }

    public byte[] getBeepMessage(int volume, int frequency, int duration) {

        byte[] message = new byte[17];
        message[0]=0;
        message[1]=17;
        message[2] = DIRECT_COMMAND_NOREPLY;
        message[3]=0; // global buffer size
        message[4]=0; // local buffer size
        message[5] = SOUND;
        message[6] = BYTE_LENGTH;
        message[7] = SOUND_TONE;
        message[8] = BYTE_LENGTH;
        message[9] = (byte)volume;
        message[10] = SHORT_LENGTH;
        message[11]= (byte)frequency;
        message[12] = (byte)(frequency>>8);
        message[13] = SHORT_LENGTH;
        message[14] = (byte)duration;
        message[15] = (byte)(duration>>8);
        message[16] = SOUND_READY;


        return message;
    }


    public byte[] getMotorMessage(Robot.Motor motor, int speed, int end, boolean sync) {
byte[] message = null;

        if (speed==0) {
          // stop motor running
            message = new byte[12];

            message[0]=0;
            message[1]=12;
            message[2]=DIRECT_COMMAND_NOREPLY;
            message[3]=0; // buffer size
            message[4]=0; // buffer size 2
            message[5]=OUTPUT_STOP;
            message[6]=BYTE_LENGTH;
            message[7]=0;
            message[8]=BYTE_LENGTH;
            message[9]=(byte)motor.getPort();
            message[10]=BYTE_LENGTH;
            message[11]=(byte)motor.getMode();


        } else  {
            if (end==0) {

            // continuous movement required. OpCode = OUTPUT_POWER
            message = new byte[17];

            message[0]=0;
            message[1]=17;
            message[2]=DIRECT_COMMAND_NOREPLY;
            message[3]=0; // buffer size
            message[4]=0; // buffer size 2
            message[5]=OUTPUT_POWER;
            message[6]=BYTE_LENGTH;
            message[7]=0;
            message[8]=BYTE_LENGTH;
            message[9]=(byte)motor.getPort();
            message[10]=BYTE_LENGTH;
            message[11]=(byte)speed;
            message[12]=OUTPUT_START;
            message[13]=BYTE_LENGTH;
            message[14]=(byte)0;
            message[15]=BYTE_LENGTH;
            message[16]=(byte)motor.getPort();


        } else {

            // move robot a set distance. OpCode = OUTPUT_STEP_POWER
            ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            bb.putInt(end);
            byte[] distance = bb.array();

            message = new byte[28];

            message[0]=0;
            message[1]=28;
            message[2]=DIRECT_COMMAND_NOREPLY;
            message[3]=0; // buffer size
            message[4]=0; // buffer size 2
            message[5]=OUTPUT_STEP_POWER;
            message[6]=BYTE_LENGTH;
            message[7]=0;
            message[8]=BYTE_LENGTH;
            message[9]=(byte)motor.getPort();
            message[10]=BYTE_LENGTH;
            message[11]=(byte)speed;
            message[12]=BYTE_LENGTH;
            message[13]=(byte)0; //STEPS (motor turn degrees) USED TO RAMP UP
            message[14]=INT_LENGTH;
            message[15]=distance[3];
            message[16]=distance[2];
            message[17]=distance[1]; //STEPS USED FOR CONSTANT SPEED - last number is significant
            message[18]=distance[0];
            message[19]=BYTE_LENGTH;
            message[20]=(byte)0; //STEPS USED FOR RAMP DOWN
            message[21]=BYTE_LENGTH;
            message[22]=(byte)1; //COAST = 0, BRAKE=1
            message[23]=OUTPUT_START;
            message[24]=BYTE_LENGTH;
            message[25]=(byte)0;
            message[26]=BYTE_LENGTH;
            message[27]=(byte)motor.getPort();
        }
        }

        return message;
    }


    public byte[] getOutputStateMessage(Robot.Motor motor) {

        byte[] message = new byte[12];


        message[0]=0;
        message[1]=12;
        message[2] = DIRECT_COMMAND_REPLY;
        message[3] = 2; // global buffer size
        message[4] = 0; // local buffer size
        message[5] = OUTPUT_TEST;
        // Output port
        message[6] = BYTE_LENGTH;
        message[7] = 0;
        message[8] = BYTE_LENGTH;
        message[9] = (byte)motor.getPort();
        message[10] = RESPONSE_INDEX;
        message[11] = 0; // global index size



        return message;
    }



    public byte[] getResetMessage(Robot.Motor motor) {
        byte[] message = new byte[12];

        message[0]=0;
        message[1]=12;
        message[2]=DIRECT_COMMAND_NOREPLY;
        message[3]=0; // buffer size
        message[4]=0; // buffer size 2
        message[5]=OUTPUT_RESET;
        message[6]=BYTE_LENGTH;
        message[7]=0;
        message[8]=BYTE_LENGTH;
        message[9]=(byte)motor.getPort();
        message[10]=BYTE_LENGTH;
        message[11]=(byte)motor.getMode();

        return message;
    }
}