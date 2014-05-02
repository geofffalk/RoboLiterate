/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */

package org.maskmedia.roboliterate.robotcomms;

import org.maskmedia.roboliterate.rlit.instructions.Program;

/**
 * Executes list of Instructions contained in RLit Program
 */
public interface ProgramLooper extends Runnable {

    void executeMainRoutineFromCurrentLine();

    void sendUIMessage(int status, int value);

    void setProgram(Program program);

}
