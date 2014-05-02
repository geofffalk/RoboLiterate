/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;


import android.content.Context;
import android.util.Log;

import org.maskmedia.roboliterate.rlit.instructions.AndroidMusic;
import org.maskmedia.roboliterate.rlit.instructions.AndroidNarrative;
import org.maskmedia.roboliterate.rlit.instructions.AndroidStopSound;
import org.maskmedia.roboliterate.rlit.instructions.Beep;
import org.maskmedia.roboliterate.rlit.instructions.Instruction;
import org.maskmedia.roboliterate.rlit.instructions.Move;
import org.maskmedia.roboliterate.rlit.instructions.Program;
import org.maskmedia.roboliterate.rlit.instructions.RLitProgram;
import org.maskmedia.roboliterate.rlit.instructions.Repeat;
import org.maskmedia.roboliterate.rlit.instructions.Turn;
import org.maskmedia.roboliterate.rlit.instructions.TurnArm;
import org.maskmedia.roboliterate.rlit.instructions.WaitOnSensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class RLitInterpreterImpl - translates a list of RLit sentences into an array of Instructions
 * that make up a 'Program'
 */
public class RLitInterpreterImpl implements RLitInterpreter {

// for debugging

    private static final String TAG = "RLitInterpreterImpl";
    private static final boolean D = false;

    private static RLitInterpreter instance = null;

    private RLitInterpreterImpl() {

    }


    public static RLitInterpreter getTranslator() {
        if (instance == null) {
            instance = new RLitInterpreterImpl();
        }
        return instance;
    }


    /**
     * Converts a list of RLitSentences into a 'Program', an ordered list of Instructions. The RLitSentence list
     * is inspected in reverse order as sentences can contain sequencing RLitPhrases ('Then',
     * 'After five seconds' etc.) that affect whether they run as block or concurrently with
     * or subsequent sentences.
     *
     * @param context   - the activity context
     * @param sentences - a list of RLitSentences to be convered into Instructions
     * @return a Program, a collection of Instructions
     */
    @Override
    public Program toProgram(Context context, List<RLitSentence> sentences) {

        int totalSentences = sentences.size();
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        boolean previousInstructionIsBlockInstruction = false;
        for (int i = totalSentences - 1; i >= 0; i--) {
            RLitSentence sentence = sentences.get(i);
            instructions.add(translateToInstruction(context, sentence, previousInstructionIsBlockInstruction));
            previousInstructionIsBlockInstruction = sentence.isConsecutive();
        }
        Collections.reverse(instructions);
        RLitProgram rp = RLitProgram.getInstance();

        for (Instruction ins : instructions) {
            rp.addInstruction(ins);
        }
        return rp;
    }

    /**
     * Translates a sentence into an Instruction. Each sentence contains an array of phrases which each
     * hold a set of properties. These properties are checked and aggregrated together to form one Instruction.
     *
     * @param context            The activity context
     * @param sentence           an RLitSentence containing an array of RLitPhrases
     * @param isBlockInstruction if the next RLitSentence runs consecutive to (i.e. not concurrent with)
     *                           this sentence, make the instruction a block instruction
     * @return Instruction
     */
    private Instruction translateToInstruction(Context context, RLitSentence sentence, boolean isBlockInstruction) {
        int delay = 0;
        int arg1 = 0;
        int arg2 = 0;
        int arg3 = 0;
        String arg4 = "";
        int instructionID = 0;
        int tempVar = 0;
        Instruction rInstruction = null;
        for (RLitPhrase phrase : sentence.getPhrases()) {
            if (phrase.getInstruction() != 0) {
                instructionID = phrase.getInstruction();
            }

            if (phrase.getArg1() != 0) {
                arg1 = (arg1 == 0) ? phrase.getArg1() : arg1 * phrase.getArg1();
            }
            if (phrase.getArg2() != 0) {
                arg2 = (arg2 == 0) ? phrase.getArg2() : arg2 * phrase.getArg2();
            }
            if (phrase.getArg3() != 0) {
                arg3 = (arg3 == 0) ? phrase.getArg3() : arg3 * phrase.getArg3();
            }

            if (phrase.getDelay() != 0) {
                delay = phrase.getDelay();
            }
            if (phrase.getWaitFlag() != 0) {
                // instruction before this needs to be a block instruction
                sentence.setIsConsecutive(true);
            }
            if (!phrase.getArg4().equals("")) {
                arg4 = phrase.getArg4();
            }
            if (phrase.getTempVar() != 0) {
                tempVar = phrase.getTempVar();
            }

        }

        switch (instructionID) {

            case Instruction.INSTRUCTION_ROBOT_MOVE:
                rInstruction = Move.getInstance(delay, arg1, arg2, isBlockInstruction);
                break;
            case Instruction.INSTRUCTION_ROBOT_TURN:
                rInstruction = Turn.getInstance(delay, arg1, arg2, isBlockInstruction);
                break;
            case Instruction.INSTRUCTION_ROBOT_BEEP:
                rInstruction = Beep.getInstance(delay, arg1, arg2, isBlockInstruction);
                break;
            case Instruction.INSTRUCTION_ANDROID_PLAYDIALOG:
                rInstruction = AndroidNarrative.getInstance(context, delay, arg4, isBlockInstruction);
                break;
            case Instruction.INSTRUCTION_ANDROID_PLAYMUSIC:
                rInstruction = AndroidMusic.getInstance(context, delay, arg2, arg4, isBlockInstruction);
                break;
            case Instruction.INSTRUCTION_ROBOT_WAIT_ULTRASONIC_SENSOR:
            case Instruction.INSTRUCTION_ROBOT_WAIT_SOUND_SENSOR:
            case Instruction.INSTRUCTION_ROBOT_WAIT_LIGHT_SENSOR:
            case Instruction.INSTRUCTION_ROBOT_WAIT_SWITCH_SENSOR:
            case Instruction.INSTRUCTION_ROBOT_WAIT_INFRARED_SENSOR:
                rInstruction = WaitOnSensor.getInstance(instructionID, arg1, arg2);
                break;
            case Instruction.INSTRUCTION_ANDROID_STOPALLSOUND:
                rInstruction = AndroidStopSound.getInstance();
                break;
            case Instruction.INSTRUCTION_REPEAT:
                //        rInstruction = Repeat.getInstance(delay, arg1, arg2, arg3, isBlockInstruction);
                rInstruction = Repeat.getInstance(delay, tempVar, arg2, arg3, isBlockInstruction);
                break;
            case Instruction.INSTRUCTION_ROBOT_TURN_ARM:
                rInstruction = TurnArm.getInstance(delay, arg1, arg2, arg3, isBlockInstruction);
            default:
                break;
        }
        if (rInstruction != null) {
            return rInstruction;
        } else return null;
    }


    @Override
    public Map<String, Integer> extractRobotSoundFiles(List<RLitSentence> sentences) {
        Map<String, Integer> soundFiles = new LinkedHashMap<String, Integer>();
        int counter = 1;
        for (RLitSentence sentence : sentences) {
            for (RLitPhrase phrase : sentence.getPhrases()) {
                String arg4 = phrase.getArg4();
                if (arg4.length() > 0 && (phrase.getPid() == RLitPhrase.ROBOT_SOUNDFILE_PID)) {
                    if (D) Log.d(TAG, "Sound file detected: " + arg4);
                    boolean previouslyUsedFile = false;
                    if (soundFiles.containsKey(arg4)) {
                        phrase.setTempVar(soundFiles.get(arg4));
                        previouslyUsedFile = true;
                        continue;
                    }
                    if (!previouslyUsedFile) {
                        soundFiles.put(arg4, counter);
                        phrase.setTempVar(counter);
                        counter++;
                    }
                }
            }

        }
        return soundFiles;
    }
}
