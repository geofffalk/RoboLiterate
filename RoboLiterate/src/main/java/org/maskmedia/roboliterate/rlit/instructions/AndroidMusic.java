/**
 *
 *  Copyright (c) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit.instructions;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * Bespoke instruction for controlling Music files played on the Android device
 *  TODO : extract an abstract AndroidSound class to reduce replication of code with AndroidNarrative class
 */
public class AndroidMusic extends AbstractInstruction {
    private static final String TAG = "AndroidMusic";
    public static boolean isPlaying = false;
    public static final MediaPlayer Player = new MediaPlayer();
    private final int mTimePeriod;

    private AndroidMusic(int timePeriod) {
        setInstructionID(INSTRUCTION_ANDROID_PLAYMUSIC);
        mTimePeriod = timePeriod;
    }

    public static AndroidMusic getInstance(Context context, int delay, int timePeriod, String filename, boolean blockInstruction) {
        AndroidMusic sound = new AndroidMusic(timePeriod);
        sound.setContext(context);
        sound.setDelay(delay);
        sound.setFilename(filename);
        sound.setBlockInstruction(blockInstruction);

        return sound;
    }


    @Override
    public void executeInstruction(InstructionExecutor executor) {
            if (Player.isPlaying())
            Player.stop();
            Player.reset();


            Uri path;

            // check if file is pre-prepared file (ie. in raw folder). If so, find path

            if (mFilename.indexOf('/') == -1) {
                int dotPosition = mFilename.lastIndexOf('.');
                String resourceName = mFilename.substring(0, dotPosition).toLowerCase();
              // int id = mContext.getResources().getIdentifier(resourceName, "raw", "org.maskmedia.roboliterate");
                path = Uri.parse("android.resource://org.maskmedia.roboliterate/raw/" + resourceName);

            } else {

                // file is user generated, saved on local storage

                path = Uri.parse(mFilename);
            }
            try {
                Player.setDataSource(mContext, path);
                Player.prepare();

            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            } catch (NullPointerException npe) {
                Log.e(TAG, "File cannot be found");
            }


            Player.setLooping(true);

            Player.start();
            isPlaying = true;

            // if mTimePeriod is 0, then the instruction is to play continuously, no need to stop automatically. Otherwise set
            // stop point

            if (mTimePeriod != 0) {

                // if this is a block instruction, simply wait until the file has played once or the duration has passed

                if (isBlockInstruction()) {
                    Log.d(TAG, "waiting some time");
                    waitSomeTime(mTimePeriod * 1000);
                    isPlaying = false;
                    if (Player.isPlaying()) {
                    Player.stop();
                    }
                } else {

                    synchronized (this) {

                        // play audio in background thread
                        Thread waitThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                waitSomeTime(mTimePeriod * 1000);
                                isPlaying = false;
                                    synchronized (Player) {
                                        if (Player.isPlaying()) {
                                        Player.stop();
                                        }
                                    }
                                }


                        });
                        waitThread.start();
                    }
                }
            }
        }

}