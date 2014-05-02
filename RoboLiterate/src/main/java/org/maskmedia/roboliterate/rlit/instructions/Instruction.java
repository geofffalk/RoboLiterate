/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

/**
 *
 * Interface defining constants and methods for all Instructions created from RLit sentences.
 * Each instruction must have an ID (InstructionType)
 *
 */
public interface Instruction {
    /**
     * Constants mapped to RLit Instruction IDs. DO NOT ALTER!
     *
     */
    public static final int INSTRUCTION_NO_ACTION = 0;
    public static final int INSTRUCTION_ROBOT_MOVE = 1;
    public static final int INSTRUCTION_ROBOT_TURN = 2;
    public static final int INSTRUCTION_ROBOT_WAIT_ULTRASONIC_SENSOR = 3;
    public static final int INSTRUCTION_ROBOT_WAIT_LIGHT_SENSOR = 4;
    public static final int INSTRUCTION_ROBOT_WAIT_SOUND_SENSOR = 5;
    public static final int INSTRUCTION_ROBOT_BEEP = 6;
    public static final int INSTRUCTION_ROBOT_PLAYSOUND = 7;
    public static final int INSTRUCTION_ANDROID_PLAYDIALOG = 8;
    public static final int INSTRUCTION_ANDROID_PLAYMUSIC = 9;
    public static final int INSTRUCTION_ANDROID_STOPALLSOUND = 10;
    public static final int INSTRUCTION_REPEAT = 11;
    public static final int INSTRUCTION_ROBOT_WAIT_SWITCH_SENSOR = 12;
    public static final int INSTRUCTION_ROBOT_TURN_ARM = 13;
    public static final int INSTRUCTION_ROBOT_WAIT_INFRARED_SENSOR = 14;
    public static final int INSTRUCTION_ROBOT_SEES = 20;
    public static final int INSTRUCTION_ROBOT_HEARS = 21;
    public static final int INSTRUCTION_ROBOT_SCANS = 22;
    public static final int INSTRUCTION_ROBOT_TOUCHES = 23;


    public void executeInstruction(InstructionExecutor executor);

    public int getInstructionID();

    public void setInstructionID(int id);

    public int getDelay();

    public boolean isBlockInstruction();

    public void waitSomeTime(int millis);
}
