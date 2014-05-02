/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

/**
 * Wait on Sensor Instruction entity class
 */
public class WaitOnSensor extends AbstractInstruction {

    private int lowerTarget;
    private int upperTarget;

    private WaitOnSensor() {

    }

    /**
     * Returns an instance of WaitOnSensor
     *
     * @param lowerTarget int - the lower end of the target range to wait for
     * @param upperTarget int - the higher end of the target range to wait for
     * @return WaitOnSensor Instruction
     */
    public static WaitOnSensor getInstance(int instructionID, int lowerTarget, int upperTarget) {
        WaitOnSensor wos = new WaitOnSensor();
        wos.setInstructionID(instructionID);
        wos.setLowerTarget(lowerTarget);
        wos.setUpperTarget(upperTarget);
        return wos;
    }


    public int getLowerTarget() {
        return lowerTarget;
    }

    public void setLowerTarget(int lowerTarget) {
        this.lowerTarget = lowerTarget;
    }

    public int getUpperTarget() {
        return upperTarget;
    }

    public void setUpperTarget(int upperTarget) {
        this.upperTarget = upperTarget;
    }

    @Override
    public void executeInstruction(InstructionExecutor executor) {

        int listenerType = InstructionExecutor.LISTENER_MOTORS_MOVE;
        switch (getInstructionID()) {
            case INSTRUCTION_ROBOT_TURN_ARM:
                listenerType=InstructionExecutor.LISTENER_MOTORS_ARM;
                break;
            case INSTRUCTION_ROBOT_WAIT_SWITCH_SENSOR:
                listenerType = InstructionExecutor.LISTENER_SWITCH;
                break;
            case INSTRUCTION_ROBOT_WAIT_ULTRASONIC_SENSOR:
                listenerType = InstructionExecutor.LISTENER_ULTRASONIC;
                break;
            case INSTRUCTION_ROBOT_WAIT_LIGHT_SENSOR:
                listenerType = InstructionExecutor.LISTENER_LIGHT;
                break;
            case INSTRUCTION_ROBOT_WAIT_SOUND_SENSOR:
                listenerType = InstructionExecutor.LISTENER_SOUND;
                break;
            case INSTRUCTION_ROBOT_WAIT_INFRARED_SENSOR:
                listenerType = InstructionExecutor.LISTENER_INFRARED;
            default:
                break;
        }
        executor.waitForRobot(listenerType, 0, getLowerTarget(), getUpperTarget(), 0, 0);

    }
}
