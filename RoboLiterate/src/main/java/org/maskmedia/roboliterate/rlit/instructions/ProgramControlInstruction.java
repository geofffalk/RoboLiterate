/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

/**
 * Contract for Instructions that influence program execution flow, namely the REPEAT instruction
 */
public interface ProgramControlInstruction {

    public int getRepetitions();

    public int getTotalInstructionsToRepeat();

    public int getTimePeriod();

    public boolean isBlockInstruction();
}
