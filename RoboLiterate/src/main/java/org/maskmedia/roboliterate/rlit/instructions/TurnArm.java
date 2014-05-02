/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;


/**
 * Turn Instruction entity class
 */
public class TurnArm extends AbstractInstruction {
    private int degrees;
    private int throttle;
    private int port;

    private TurnArm() {
        setInstructionID(INSTRUCTION_ROBOT_TURN);
    }

    public static TurnArm getInstance(int delay, int throttle, int degrees, int port, boolean blockInstruction) {
        TurnArm turn = new TurnArm();
        turn.setPort(port);
        turn.setDegrees(degrees);
        turn.setDelay(delay);
        turn.setThrottle(throttle);
        turn.setBlockInstruction(blockInstruction);
        return turn;
    }


    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
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
        executor.doRobotMove(getPort(), getThrottle(), getDegrees(), isBlockInstruction());
    }
}
