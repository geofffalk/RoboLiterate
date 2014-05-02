/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;


import android.util.Log;

/**
 * Move Instruction entity class
 */
public class Move extends AbstractInstruction {

    private static final String TAG = "Move";
    private int throttle;
    private int distance;

    private Move() {
        setInstructionID(INSTRUCTION_ROBOT_MOVE);
    }

    /**
     * @param delay            int -  wait time before completing instruction
     * @param distance         int - distance of move
     * @param throttle         int - speed of move
     * @param blockInstruction boolean - perform action synchronously
     * @return self
     */
    public static Move getInstance(int delay, int throttle, int distance, boolean blockInstruction) {
        Log.d(TAG, "MOVE called with throttle:" + throttle + " distance " + distance);
        Move move = new Move();
        move.setDelay(delay);
        move.setDistance(distance);
        move.setThrottle(throttle);
        move.setBlockInstruction(blockInstruction);
        return move;
    }


    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public void executeInstruction(InstructionExecutor executor) {
        executor.doRobotMove(getThrottle(), getDistance(), isBlockInstruction());
    }
}
