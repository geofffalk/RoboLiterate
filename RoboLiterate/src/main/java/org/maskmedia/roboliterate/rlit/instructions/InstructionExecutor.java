/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

import org.maskmedia.roboliterate.robotcomms.Robot;

/**
 * Contract for executors of instructions. Executors must be able to carry out all robot related
 * Instructions
 *
 */
public interface InstructionExecutor {

    public static final int LISTENER_MOTORS_MOVE = 0;
    public static final int LISTENER_ULTRASONIC = 1;
    public static final int LISTENER_SOUND = 2;
    public static final int LISTENER_LIGHT = 3;
    public static final int LISTENER_SWITCH = 4;
    public static final int LISTENER_MOTORS_ARM = 5;
    public static final int LISTENER_MOTOR_A = 6;
    public static final int LISTENER_MOTOR_B = 7;
    public static final int LISTENER_MOTOR_C = 8;
    public static final int LISTENER_MOTOR_D = 9;
    public static final int LISTENER_INFRARED = 10;

    public void doRobotMove(int throttle, int distance, boolean waitUntilComplete);
    public void doRobotMove(Robot.Motor motor, int throttle, int distance, boolean waitUntilComplete);
    void doRobotMove(int portIndex, int throttle, int degrees, boolean blockInstruction);
    public void doRobotRotate(int throttle, int degrees, boolean waitUntilComplete);
    public void doRobotBeep(int tone, int duration, boolean waitUntilComplete);
    public void doResetMotors();
    public void doRobotStopMotors();
    public void onSensorReport(Robot.Sensor sensor, int status, int value);
    public void onMotorReport(Robot.Motor motor, int status, int value);
    public void waitForRobot(int listenerType, int duration, int lowerTarget, int upperTarget, int lowerTarget2, int upperTarget2);


    public void doRobotTurnArm(int throttle, int degrees, boolean blockInstruction);


}
