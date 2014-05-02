/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

/**
 * Beep Instruction entity class
 */
public class Beep extends AbstractInstruction {

    private static String instructionName = "BEEP";
    private int duration;
    private int tone;

    private Beep() {
        setInstructionID(INSTRUCTION_ROBOT_BEEP);
    }

    /**
     * Returns instance of Beep instruction
     *
     * @param delay            int - delay before executing instruction
     * @param tone             int - frequency of beep
     * @param duration         int - duration of beep
     * @param blockInstruction boolean - whether or not instruction is synchronous
     * @return Beep Instruction
     */
    public static Beep getInstance(int delay, int tone, int duration, boolean blockInstruction) {
        Beep sound = new Beep();
        sound.setDelay(delay);
        sound.setDuration(duration);
        sound.setTone(tone);
        sound.setBlockInstruction(blockInstruction);
        return sound;
    }


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTone() {
        return tone;
    }

    public void setTone(int tone) {
        this.tone = tone;
    }

    @Override
    public void executeInstruction(InstructionExecutor executor) {
        executor.doRobotBeep(getTone(), getDuration(), isBlockInstruction());
    }
}
