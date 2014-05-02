/**
 * Copyright (C) 2013 Geoffrey Falk
 */


package org.maskmedia.roboliterate.robotcomms;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Singleton class that manages bluetooth communication stream with robot
 */
public class BluetoothCommunicator {


    private static final String TAG = "BluetoothCommunicator";
    private InputStream robotInputStream;
    private OutputStream robotOutputStream;
    private static final BluetoothCommunicator instance = new BluetoothCommunicator();


    private BluetoothCommunicator() {
    }

    public static BluetoothCommunicator getInstance(InputStream is, OutputStream os) {
        instance.robotInputStream = is;
        instance.robotOutputStream = os;
        return instance;

    }

    public static BluetoothCommunicator getInstance() {
        if (instance.robotInputStream != null & instance.robotOutputStream != null) {
            return instance;
        } else {
            return null;
        }

    }



    /**
     * Sends a message on the opened OutputStream
     *
     * @param message, the message as a byte array
     */

  /* ORIGINAL NXT CODE

    public void sendByteMessageToRobot(byte[] message) throws IOException {
        if (robotOutputStream == null)
            throw new IOException();

        // send message length
        int messageLength = message.length;
        robotOutputStream.write(messageLength);
        robotOutputStream.write(messageLength >> 8);
        robotOutputStream.write(message, 0, message.length);
    }

    */

    public void sendByteMessageToRobot(byte[] message) throws IOException {
        if (robotOutputStream == null)
            throw new IOException();

//        int i = message.length;
//        byte abyte1[] = new byte[2];
//        abyte1[0] = (byte)(i&0xff);
//        abyte1[1] = (byte)(0xff&i>>>8);
//
//            robotOutputStream.write(abyte1);
//            robotOutputStream.write(message);
//

        // send message length
        int messageLength = message.length;
        robotOutputStream.write(messageLength);
        robotOutputStream.write(messageLength >> 8);
        robotOutputStream.write(message, 0, message.length);
    }



    /**
     * Receives a message on the opened InputStream
     *
     * @return the message
     */

//
//        // original nxt communicator code
//        public byte[] receiveByteMessageFromRobot() throws IOException {
//        if (robotInputStream == null)
//            throw new IOException();
//
//            byte abyte0[] = new byte[2];
//            byte abyte1[];
//
//                robotInputStream.read(abyte0, 0, abyte0.length);
//                int i = 0xff & abyte0[0] | abyte0[1] << 8;
//                abyte1 = new byte[i];
//                robotInputStream.read(abyte1, 0, i);
//            return abyte1;
//    }



        // original nxt code


    public byte[] receiveByteMessageFromRobot() throws IOException {
        if (robotInputStream == null)
            throw new IOException();

        int length = robotInputStream.read();
        length = (robotInputStream.read() << 8) + length;
        byte[] returnMessage = new byte[length];
        robotInputStream.read(returnMessage);
        return returnMessage;
    }

    public boolean isConnected() {
        if (robotInputStream != null && robotOutputStream != null) {
            try {
                robotInputStream.available();
            } catch (IOException ioe) {
                Log.e(TAG, "Communication problem with robot: " + ioe.getMessage());
            }
            return true;
        }
        return false;
    }


}