/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */

package org.maskmedia.roboliterate.robotcomms;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.maskmedia.roboliterate.rlit.instructions.Instruction;
import org.maskmedia.roboliterate.rlit.instructions.Program;
import org.maskmedia.roboliterate.rlit.instructions.ProgramControlInstruction;
import org.maskmedia.roboliterate.rlit.instructions.RLitProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Loops through program Instructions and executes them sequentially and/or concurrently
 * using Commander as a Visitor object.
 */
public class RobotProgramLooper implements ProgramLooper {

    private static final String TAG = "RobotProgramLooper";
    private static final int MSG_SUBROUTINE_OVER = 2;
    private static final int MSG_SUBROUTINE_BEGUN = 1;
    private static final int MSG_SUBROUTINE_EXECUTING_LINE = 3;
    public static final int MSG_PROGRAM_QUIT = 4;
    private Program program;
    private int programCursor;
    private boolean mainLoopOver;
    public Handler programExecutorHandler;
    private Handler mCommanderHandler;
    private Commander mCommander;
    private boolean mainLoopRunning;
    private boolean subroutineRunning;
    private Thread subroutineThread;


    public RobotProgramLooper(Handler handler, Commander commander, Program rp) {
        setProgram(rp);
        mCommanderHandler = handler;
        mCommander = commander;
        mainLoopOver = false;
    }

    public boolean isRunning() {
        if  (subroutineRunning || mainLoopRunning) {
            return true;
        } else return false;
    }

    @Override
    public void run() {

        Looper.prepare();
        /**
         * Handler for incoming messages from subroutine running on concurrent thread
         */
        programExecutorHandler = new Handler() {

            @Override
            public void handleMessage(Message message) {

                switch (message.what) {
                    case MSG_SUBROUTINE_BEGUN:
                        subroutineRunning = true;
                        Log.d(TAG, "Subroutine has begun");
                        break;
                    case MSG_SUBROUTINE_EXECUTING_LINE:
                        int programLine = message.arg1;
                        sendUIMessage(Commander.STATUS_RUNNING, programLine);
                        break;
                    case MSG_SUBROUTINE_OVER:

                        Log.d(TAG, "Subroutine is over");
                        if (mainLoopOver) {
                            sendUIMessage(Commander.STATUS_COMPLETE, 0);
                        } else if (!mainLoopRunning) {
                            //   programCursor++;
                            executeMainRoutineFromCurrentLine();
                        }
                        break;
                    default:
                        break;
                }
            }
        };



        programCursor = 0;
        executeMainRoutineFromCurrentLine();
        Looper.loop();
    }

    public void endProgram() {
        mainLoopRunning = false;
        subroutineRunning = false;
    }

    /**
     * Main program routine
     */
    @Override
    public void executeMainRoutineFromCurrentLine() {
        mainLoopRunning = true;
        subroutineRunning = false;
        while (programCursor < program.length() && mainLoopRunning) {
            Instruction ri = program.getInstructionAtLine(programCursor);
            if (ri != null) {
                // in case a null instruction has been entered through UI issue
                int delay;
                if ((delay = ri.getDelay()) > 0) {
                    waitSomeTime(delay * 1000);
                }

                // Update UI with message that program line is being executed

                sendUIMessage(Commander.STATUS_RUNNING, programCursor);

                if (ri instanceof ProgramControlInstruction) {

                    // Instruction is related to program flow, not robot and Android operations

                    ProgramControlInstruction repeatInstruction = (ProgramControlInstruction) ri;
                    Log.d(TAG, "Repeat Routine called with " + repeatInstruction.getTotalInstructionsToRepeat() + " instructions to repeat " + repeatInstruction.getRepetitions() + " times over " + repeatInstruction.getTimePeriod() + " time period.");
                    final int timePeriod = repeatInstruction.getTimePeriod();

                    // Create program based on repeat instruction

                    Program subProgram = RLitProgram.getInstance();
                    List<Integer> storyLineIds = new ArrayList<Integer>();
                    for (int i = 0; i < repeatInstruction.getRepetitions(); i++) {
                        int repeatCursor = programCursor - repeatInstruction.getTotalInstructionsToRepeat();

                        // in case user has deleted earlier sentences after inserting the repeat command

                        if (repeatCursor < 0) repeatCursor = 0;

                        while (repeatCursor < programCursor) {
                            subProgram.addInstruction(program.getInstructionAtLine(repeatCursor));
                            storyLineIds.add(repeatCursor);
                            repeatCursor++;
                        }
                    }

                    if (repeatInstruction.isBlockInstruction()) {

                        // pause main loop until subroutine has completed execution. Otherwise, run subroutine
                        // concurrently

                        mainLoopRunning = false;
                    }
                    final Program subroutineProgram = subProgram;
                    final Integer[] storyLines = storyLineIds.toArray(new Integer[storyLineIds.size()]);

                    Log.d(TAG, "Subroutine program is as follows: " + subroutineProgram.toString());

                    // start thread to run subroutine

                    subroutineThread = new Thread(new Runnable() {

                        int subroutineCursor = 0;


                        @Override
                        public void run() {

                            boolean measuringTime = (timePeriod > 0);
                            long endTime = System.currentTimeMillis() + timePeriod * 1000;
                            while ((measuringTime && System.currentTimeMillis() < endTime) || !measuringTime) {
                                synchronized (this) {
                                    Log.d(TAG, "Inside 1st loop");
                                    while (subroutineCursor < subroutineProgram.length() && subroutineRunning) {
                                        Log.d(TAG, "Inside 2nd loop");
                                        Instruction ri = subroutineProgram.getInstructionAtLine(subroutineCursor);
                                        int delay;
                                        if ((delay = ri.getDelay()) > 0) {
                                            waitSomeTime(delay * 1000);
                                        }

                                        Message msg = Message.obtain();
                                        msg.what = MSG_SUBROUTINE_EXECUTING_LINE;
                                        msg.arg1 = storyLines[subroutineCursor];
                                        programExecutorHandler.sendMessage(msg);
                                        ri.executeInstruction(mCommander);
                                        subroutineCursor++;
                                        Log.d(TAG, "End of 2nd loop");
                                    }

                                    if (!measuringTime) break;
                                    else subroutineCursor = 0;
                                    Log.d(TAG, "End of 1st loop");
                                }
                            }
                            subroutineRunning = false;
                            Message msg = Message.obtain();
                            msg.what = MSG_SUBROUTINE_OVER;
                            programExecutorHandler.sendMessage(msg);
                        }
                    });

                    subroutineThread.start();
                    subroutineRunning = true;
                } else {

                    // Instruction is related to robot or Android device operations, so visit instruction
                    // with Commander object as parameter

                    ri.executeInstruction(mCommander);

                }

            }
            programCursor++;


        }
        if (!subroutineRunning) {
            sendUIMessage(Commander.STATUS_COMPLETE, 0);
        } else if (programCursor >= program.length()) {
            mainLoopOver = true;
        }

    }

    @Override
    public void sendUIMessage(int status, int value) {
        Message message = Message.obtain(mCommanderHandler, Commander.MSG_PROGRAM_THREAD, status, value);
        if (message!=null)
        message.sendToTarget();
    }


    @Override
    public void setProgram(Program program) {
        this.program = program;
    }


    private void waitSomeTime(int millis) {

        long endTime = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                    Log.d(TAG, "Wait failed: "+e.getMessage());
                }
            }
        }
    }


}

