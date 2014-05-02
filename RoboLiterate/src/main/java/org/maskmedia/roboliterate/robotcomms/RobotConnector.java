/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.robotcomms;

/**
 * Interface defining Service methods for notifying clients, setting up robot and connecting to Robot
 */
public interface RobotConnector {


    /**
     * Message IDs
     */
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_CONNECT_TO_ROBOT = 3;
    public static final int MSG_CANCEL_CONNECT_TO_ROBOT = 4;
    public static final int MSG_CONFIGURE_PORTS = 5;
    public static final int MSG_UPLOAD_FILES = 6;
//    public static final int MSG_REGISTER_SENSOR = 7;
    //   public static final int MSG_ROBOT_RESET_MOTORS = 8;
    public static final int MSG_CHECK_CONNECTION_STATUS = 9;
    public static final int MSG_EXECUTE_PROGRAM = 10;
    public static final int MSG_STOP_PROGRAM = 11;
    public static final int MSG_SENSOR_READING = 12;
    public static final int MSG_DETECT_SENSORPORTS = 13;
    public static final int MSG_MOTOR_READING = 14;
    public static final int MSG_STATE_CHANGE=15;

    public static final int STATE_NONE = 0;
    public static final int STATE_PAIRING= 1;
    public static final int STATE_CONNECTED = 2;


    void connectToRobot(String robotType, String robotAddress);

    // setup robot as either NXT or EV3
//    void initRobot();

    void cancelConnectToRobot();



}
