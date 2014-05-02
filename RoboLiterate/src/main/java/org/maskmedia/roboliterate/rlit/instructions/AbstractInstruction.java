/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

import android.content.Context;
import android.util.Log;

/**
 * Generic getters and setters common to all Instruction classes
 */
abstract class AbstractInstruction implements Instruction {
    private static final String TAG = "AbstractInstruction";
    private int mDelay;
    protected int mInstructionID;
    private boolean mBlockInstruction;
    protected String mFilename;
    protected Context mContext;

    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        this.mDelay = delay;
    }

    public boolean isBlockInstruction() {
        return mBlockInstruction;
    }

    public void setBlockInstruction(boolean blockInstruction) {
        this.mBlockInstruction = blockInstruction;
    }

    public int getInstructionID() {
        return mInstructionID;
    }

    public void setInstructionID(int id) {
        this.mInstructionID = id;
    }


    public void setFilename(String filename) {
        this.mFilename = filename;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void waitSomeTime(int millis) {

        long endTime = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {

                    Log.e(TAG,"Wait failed "+e.getMessage());
                }
            }
        }
    }


}
