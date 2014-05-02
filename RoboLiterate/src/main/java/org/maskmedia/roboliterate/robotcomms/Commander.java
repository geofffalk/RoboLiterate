/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.robotcomms;

import org.maskmedia.roboliterate.rlit.instructions.InstructionExecutor;
import org.maskmedia.roboliterate.rlit.instructions.Program;

/**
 * Contract for Commander interface, which extends InstructionExecutor, which in turn implements Visitor
 * pattern so that multiple Commanders can interpret RLit Instructions.
 *
 * Commander interface provides all methods for commanding robot and listening for sensor readings
 */
public interface Commander extends InstructionExecutor {

    public final static int MSG_PROGRAM_THREAD = 0;
    public final static int MSG_UPLOAD_THREAD = 1;

    public static final int STATUS_OK = 1000;
    public static final int STATUS_UNKNOWN = 1001;
    public static final int STATUS_NULL_MESSAGE = 1002;
    public static final int STATUS_INSUFFICIENT_MEMORY = 1003;
    //    public static final int STATUS_ERROR = 1004;
    public static final int STATUS_COMPLETE = 1005;
    //    public static final int STATUS_IDLE = 1006;
    public static final int STATUS_MOTORS_STOPPED = 1007;
    public static final int STATUS_TIMEDOUT = 1008;
    public static final int STATUS_CONNECTIONERROR = 1009;
    public static final int STATUS_CONNECTIONERROR_PAIRING = 1010;
    public static final int STATUS_INITIATING = 1011;
    public static final int STATUS_RUNNING = 1012;
    public static final int STATUS_FILE_UPLOADED = 1013;
    public static final int STATUS_FILE_UPLOADING = 1014;

    public static final int STATUS_NO_SENSOR = 1015;
    public static final int STATUS_NO_MOTOR = 1016;
    public static final int STATUS_SENSOR_ERROR = 1017;




    /**
     * Contract for Connector class to receive status updates from Commander
     */
    interface CommanderListener {
        void onCommunicationFailure(int status);
        void onPortsConfigured(int status);
        void onProgramLineExecuted(int status);
        void onProgramComplete(int status);
        void onPortsDetected(int status);
        void updateMotorReading(int status, int value);
        void updateSensorReading(Robot.Sensor sensor, int status, int value);

        void onRobotTypeDetected(int robotType);
    }

    ByteTranslator getTranslator();
    void waitSomeTime(int millis);
    void configurePorts();
    void detectRobotType();

    void registerSensor(Robot.Sensor sensor);

    void doRobotBeep(int volume, int frequency, int duration, boolean waitUntilComplete);

    void runRobotProgram(Program program);
    void stopRobotProgram();
    void detectSensorPorts();
    boolean sendMessageToRobot(byte[] message);
    byte[] receiveMessageFromRobot();


}
