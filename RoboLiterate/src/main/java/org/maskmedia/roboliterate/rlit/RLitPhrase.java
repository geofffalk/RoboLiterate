/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;

import android.util.Log;

import java.io.Serializable;

/**
 * RLitPhrase entity class.
 */
public class RLitPhrase implements Comparable<RLitPhrase>, Cloneable, Serializable {


    private String label;
    private int id;
    private int pid;
    private int instruction;
    private int delay;
    private int arg1;
    private int arg2;
    private int arg3;
    private String arg4;
    private int waitFlag;
    private int sortIndex;
    private int tempVar;


    // Sequencers PIDs/IDs

    public final static int DEFAULT_SEQUENCERS_PID = 0;
    public final static int FIRST_PID = -1;
    public final static int WHEN_ID = 2;
    public final static int COMMA_PID = 2200;

    // Verbs IDs

    public final static int WAITS_ID = 104;
    public final static int REPEAT_ID = 116;

    // Modifier PIDs/IDs

    public final static int ROBOT_SOUNDFILE_PID = 112;
    public final static int ANDROID_DIALOGFILE_PID = 113;
    public final static int REPEAT_MODIFIER_ID = 1116;
    public final static int ANDROID_DIALOGFILE_ID = 1113;


    private static final String TAG = "RLitPhrase";


    public RLitPhrase(String label, int id, int phid, int instruction, int delay, int arg1,
                      int arg2, int arg3, String arg4, int waitFlag, int sortIndex) {
        setLabel(label);
        setId(id);
        setPid(phid);
        setInstruction(instruction);
        setDelay(delay);
        setArg1(arg1);
        setArg2(arg2);
        setArg3(arg3);
        setArg4(arg4);
        setWaitFlag(waitFlag);
        setSortIndex(sortIndex);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        Log.d(TAG, "Phrase added " + this.label);
    }

    public int getInstruction() {
        return instruction;
    }



    public void setInstruction(int instruction) {
        this.instruction = instruction;
        Log.d(TAG, "Instruction added " + this.instruction);
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getArg1() {
        return arg1;
    }

    public void setArg1(int arg1) {
        this.arg1 = arg1;
    }

    public int getArg2() {
        return arg2;
    }

    public void setArg2(int arg2) {
        this.arg2 = arg2;
    }

    public int getArg3() {
        return arg3;
    }

    public void setArg3(int arg3) {
        this.arg3 = arg3;
    }

    public int getWaitFlag() {
        return waitFlag;
    }

    public void setWaitFlag(int waitFlag) {
        this.waitFlag = waitFlag;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return label;
    }


    public void setArg4(String arg4) {
        this.arg4 = arg4;
    }

    public String getArg4() {
        return arg4;
    }

    public void setTempVar(int temp) {
        tempVar = temp;
    }

    public int getTempVar() {
        return tempVar;
    }



    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Order phrases according to sortIndex field, or if that is empty, then alphabetically according to
     * label
     * @param other int
     * @return int
     */
    @Override
    public int compareTo(RLitPhrase other) {
        int order = this.getSortIndex()-other.getSortIndex();
        if (order==0) {
            return (label.compareTo(other.getLabel()));
        }
        return order;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}


