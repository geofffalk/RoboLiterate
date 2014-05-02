/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

/**
 * Stops all sounds on Android, useful for when exiting from Program execution activity and we have
 * looping audio being played in a background thread
 */
public class AndroidStopSound extends AbstractInstruction {

    private AndroidStopSound() {
        setInstructionID(INSTRUCTION_ANDROID_PLAYMUSIC);
    }

    public static Instruction getInstance() {
        return new AndroidStopSound();
    }

    @Override
    public void executeInstruction(InstructionExecutor executor) {
        if (AndroidNarrative.Player.isPlaying()) {
            synchronized (AndroidNarrative.Player) {
                AndroidNarrative.Player.stop();
            }
        }

        if (AndroidMusic.Player.isPlaying()) {
            synchronized (AndroidMusic.Player) {
                AndroidMusic.Player.stop();
            }


        }
    }

}
