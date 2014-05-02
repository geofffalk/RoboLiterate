/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;

import android.os.Parcel;
import android.os.Parcelable;

import org.maskmedia.roboliterate.rlit.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * RLitSentence entity class that contains the list of phrases that make up the sentence, extending
 * parcelable so it can be easily saved as object to SQLiteDatabase
 */
public class RLitSentence implements Parcelable {

    public static final int TYPE_DIRECTION = 0;
    public static final int TYPE_EVENTLISTENER_WAIT = 1;
    public static final int TYPE_EVENTLISTENER_WHEN = 2;
    public static final int TYPE_EVENTRESULT = 3;


    private List<RLitPhrase> mSentence;
    private int lastId;
    private int mInstructionID;
    private boolean mIsConsecutive;
    private int mSentenceType;

    public RLitSentence() {
        mSentence = new ArrayList<RLitPhrase>();
        lastId = 0;
    }

    private RLitSentence(Parcel in) {
        mSentence = new ArrayList<RLitPhrase>();
        in.readList(mSentence, getClass().getClassLoader());
    }

    public static final Creator<RLitSentence> CREATOR
            = new Creator<RLitSentence>() {
        public RLitSentence createFromParcel(Parcel in) {
            return new RLitSentence(in);
        }

        public RLitSentence[] newArray(int size) {
            return new RLitSentence[size];
        }
    };

    /**
     * Add a phrase to the sentence. Check phrase to see whether it changes the overall type of the Setntence
     * If the phrase is a Verb, then InstructionID of sentence is set also
     *
     * @param phrase - RLitPhrase to be added
     */
    public void addPhrase(RLitPhrase phrase) {
        lastId = phrase.getId();
        mSentence.add(phrase);
        if (phrase.getInstruction() != 0) {
            setInstructionID(phrase.getInstruction());
        }
        if (phrase.getId() == RLitPhrase.WAITS_ID) {

            setSentenceType(TYPE_EVENTLISTENER_WAIT);

        } else if (phrase.getId() == RLitPhrase.WHEN_ID) {

            setSentenceType(TYPE_EVENTLISTENER_WHEN);

        } else if (phrase.getPid() == RLitPhrase.COMMA_PID) {

            setSentenceType(TYPE_EVENTRESULT);

        }

    }

    /**
     * Delete the last phrase to be added to the Sentence.
     *
     * @return boolean - phrase successfully deleted or not
     */
    public boolean deleteLastPhrase() {
        int id = mSentence.get(0).getPid();
        if (mSentence.size() >= (mSentence.get(0).getPid() == RLitPhrase.FIRST_PID ? 2 : 1)) {
            RLitPhrase phraseToRemove = mSentence.get(mSentence.size() - 1);
            if (phraseToRemove.getInstruction() != 0) {
                setInstructionID(0);
            }
            if (phraseToRemove.getId() == RLitPhrase.WAITS_ID) {
                setSentenceType(TYPE_DIRECTION);
            }
            mSentence.remove(phraseToRemove);
            if (mSentence.size() > 0) {
                lastId = mSentence.get(mSentence.size() - 1).getId();
            } else lastId = 0;
            return true;
        }
        return false;
    }

    public boolean isComplete() {
        return getInstructionID() != Instruction.INSTRUCTION_NO_ACTION;
    }

    public int getLastId() {
        return lastId;
    }

    public int length() {
        return mSentence.size();
    }

    public List<RLitPhrase> getPhrases() {
        return mSentence;
    }
//
//    public RLitPhrase getPhrase(int position) {
//        return mSentence.get(position);
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(mSentence);

    }

    public boolean isConsecutive() {
        return mIsConsecutive;
    }

    public void setIsConsecutive(boolean value) {
        mIsConsecutive = value;
    }

    public int getInstructionID() {
        return mInstructionID;
    }

    public void setInstructionID(int instruction) {
        this.mInstructionID = instruction;
    }

    public int getSentenceType() {
        return mSentenceType;
    }

    public void setSentenceType(int type) {
        this.mSentenceType = type;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RLitPhrase rsp : mSentence) {
            sb.append(rsp.toString()).append(" ");
        }
        return sb.toString();
    }


}


