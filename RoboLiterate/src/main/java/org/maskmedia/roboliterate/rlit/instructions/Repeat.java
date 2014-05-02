/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;


/**
 * Repeat Instruction that tells Program executor class to repeat previous instructions a set number of
 * times
 */
public class Repeat extends AbstractInstruction implements ProgramControlInstruction {

    private int timePeriod;
    private int repetitions;
    private int numberOfInstructions;

    private Repeat() {
        setInstructionID(INSTRUCTION_REPEAT);
    }

    /**
     * Utility class that returns a Repeat Instruction
     *
     * @param delay                int - wait before Instruction is executed
     * @param numberOfInstructions int - number of previous instructions to repeat
     * @param repetitions          int - number of repetitions
     * @param timePeriod           int - alternatively, the time period in which to repeat the instructions
     * @param blockInstruction     boolean - whether this instruction is synchronous or not
     * @return
     */
    public static Repeat getInstance(int delay, int numberOfInstructions, int timePeriod, int repetitions, boolean blockInstruction) {
        Repeat repeat = new Repeat();
        repeat.setDelay(delay);
        repeat.setNumberOfInstructions(numberOfInstructions);
        repeat.setTimePeriod(timePeriod);
        repeat.setRepetitions(repetitions);
        repeat.setRepetitions(repetitions);
        repeat.setBlockInstruction(blockInstruction);
        return repeat;
    }

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getTotalInstructionsToRepeat() {
        return numberOfInstructions;
    }

    public void setNumberOfInstructions(int numberOfInstructions) {
        this.numberOfInstructions = numberOfInstructions;
    }

    @Override
    public void executeInstruction(InstructionExecutor executor) {

    }

    @Override
    public int getInstructionID() {
        return 0;
    }

    @Override
    public void setInstructionID(int id) {

    }

    @Override
    public int getDelay() {
        return 0;
    }

//    @Override
//    public boolean isBlockInstruction() {
//
//    }

    @Override
    public void waitSomeTime(int millis) {

    }
}
