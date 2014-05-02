/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;


/**
 * Turn Instruction entity class
 */
public class Turn extends AbstractInstruction {
    private int degrees;
    private int throttle;

    private Turn() {
        setInstructionID(INSTRUCTION_ROBOT_TURN);
    }

    public static Turn getInstance(int delay, int degrees, int throttle, boolean blockInstruction) {
        Turn turn = new Turn();
        turn.setDegrees(degrees);
        turn.setDelay(delay);
        turn.setThrottle(throttle);
        turn.setBlockInstruction(blockInstruction);
        return turn;
    }

    public int getDegrees() {
        return degrees;
    }

    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }

    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

    @Override
    public void executeInstruction(InstructionExecutor executor) {
        executor.doRobotRotate(getThrottle(), getDegrees(), isBlockInstruction());
    }
}
