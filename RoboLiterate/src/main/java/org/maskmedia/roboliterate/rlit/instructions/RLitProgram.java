/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Program implementation. Entity class that contains the list of Instructions to be executed.
 * Parcelable in order to allow UI classes to pass Program as a message to Robot Service
 */
public class RLitProgram implements Parcelable, Program {

    private ArrayList<Instruction> mProgram;

    private RLitProgram() {
        mProgram = new ArrayList<Instruction>();
    }

    public static RLitProgram getInstance() {

        return new RLitProgram();
    }

    @Override
    public Instruction getInstructionAtLine(int i) {
        return mProgram.get(i);
    }

    public void addInstruction(Instruction rb) {
        mProgram.add(rb);

    }

    @Override
    public int length() {
        return mProgram.size();
    }

    private RLitProgram(Parcel in) {
        mProgram = new ArrayList<Instruction>();
        in.readList(mProgram, null);
    }

    public static final Creator<Program> CREATOR
            = new Creator<Program>() {
        public Program createFromParcel(Parcel in) {
            return new RLitProgram(in);
        }

        public Program[] newArray(int size) {
            return new Program[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Instruction ri : mProgram) {
            buffer.append("Instruction ID: ").append(ri.getInstructionID()).append("; ");
        }
        return buffer.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(mProgram);
    }
}
