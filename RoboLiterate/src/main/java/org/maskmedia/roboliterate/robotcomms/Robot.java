/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.robotcomms;

import android.util.Log;

/**
 * Constants file containing default port mappings and status and command codes
 */
public class Robot {

    public enum Motor {
        LEFT("Left",1,-1),
        RIGHT("Right",1,-1),
        ARM("Arm",1,-1),
        A("A",1,-1),
        B("B",1,-1),
        C("C",1,-1),
        D("D",1,-1);

        private String name;
        private int port;
        private int mode;

        Motor(String name, int mode, int port) {
            this.name = name;
            this.port = port;
            this.mode = mode;
        }

        public int getPort() {
            return port;
        }

        public int getMode() {return mode;}

        public void setPort(int port) {
            this.port = port;
        }

        public static Motor getMotorForPort(int port) {
            for (Motor m : values()) {
                if (m.port==port) return m;
            }
            throw new IllegalArgumentException();
        }

        public static Motor getMotorForPortIndex(int index) {
            switch (index) {
                case 0:
                    return Motor.A;
                case 1:
                    return Motor.B;
                case 2:
                    return Motor.C;
                case 3:
                    return Motor.D;
                default:
                    return null;

            }
        }


    }

    public enum Sensor {
        SWITCH("Switch",16,0,-1,"bumps"),
        LIGHT("Light",29,0,-1,"%"),
        ULTRASONIC("Ultrasonic",30,0,-1,"cm"),
        SOUND("Sound",3,-1,-1,"dB"),
        INFRARED("Infrared",33,0,-1,"cm");
//        COLOR("Color",29,0,1,"");
        private static final String TAG = "Sensor";

        private String name;
        private int type;
        private int mode;
        private int port;
        private String unit;

        Sensor(String name, int type, int mode, int port, String unit) {
            this.name = name;
            this.type = type;
            this.mode = mode;
            this.port = port;
            this.unit = unit;

        }


        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
            Log.d(TAG, this.name + " sensor set to port " + port);
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;

        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public String getUnit() {
            return this.unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static final String MY_PREFS = "MyPrefs";


    public static final int MODEL_NXT = 50;
    public static final int MODEL_EV3 = 51;
    public static final int MODEL_UNKNOWN = 52;


    public static int getRobotType() {
        return RobotType;
    }

    public static void setRobotType(int robotType) {
        RobotType = robotType;
        int [] motorPorts = getMotorPortNumbers();
        Motor.A.setPort(motorPorts[0]);
        Motor.B.setPort(motorPorts[1]);
        Motor.C.setPort(motorPorts[2]);
        if (RobotType==MODEL_EV3)
            Motor.D.setPort(motorPorts[3]);
    }

    private static int RobotType = MODEL_UNKNOWN;
    public static final int COMMAND_WRITE = 100;
    public static final int COMMAND_OPEN_WRITE = 101;
    public static final int COMMAND_CLOSE = 102;
    public static final int COMMAND_GET_INPUT_VALUES = 103;
    public static final int COMMAND_GET_OUTPUT_STATE = 104;
    public static final int COMMAND_LS_READ = 105;
    public static final int COMMAND_LS_GET_STATUS = 106;

    public static final int SCALED_VALUE = 200;
    public static final int NORMALIZED_VALUE = 201;
    public static final int RAW_VALUE = 202;
    public static final int PERCENT_VALUE = 203;


    public static int[] getMotorPortNumbers() {
       switch (RobotType) {
           case MODEL_NXT:
               return new int[]{0,1,2};

           case MODEL_EV3:
               return new int[]{1,2,4,8};

           default:
               return null;

       }
    }



    static {

//        mappedPorts = new HashMap<Integer, Integer>();
//        mappedPorts.put(SENSOR_NONE, SENSOR_PORT_1);
//        mappedPorts.put(SENSOR_LIGHT, SENSOR_PORT_3);
//        mappedPorts.put(SENSOR_SOUND, SENSOR_PORT_2);
//        mappedPorts.put(SENSOR_ULTRASONIC, SENSOR_PORT_4);
//        mappedPorts.put(MOTOR_NONE, MOTOR_PORT_A);
//        mappedPorts.put(MOTOR_LEFT, MOTOR_PORT_B);
//        mappedPorts.put(MOTOR_RIGHT, MOTOR_PORT_C);
//        mappedPorts.put(MOTOR_NONE, MOTOR_PORT_D);

    }





}
