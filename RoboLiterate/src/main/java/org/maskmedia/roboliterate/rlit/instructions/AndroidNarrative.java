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
 * Bespoke instruction for controlling narrative files played on the Android device
 * TODO : extract an abstract AndroidSound class to reduce replication of code with AndroidMusic class
 */
public class AndroidNarrative extends AbstractInstruction {
    private static final String TAG = "AndroidNarrative";
    public static boolean isPlaying = false;
    public static final MediaPlayer Player  = new MediaPlayer();

    private AndroidNarrative() {
        setInstructionID(INSTRUCTION_ANDROID_PLAYMUSIC);
    }

    public static AndroidNarrative getInstance(Context context, int delay, String filename, boolean blockInstruction) {
        AndroidNarrative sound = new AndroidNarrative();
        sound.setContext(context);
        sound.setDelay(delay);
        sound.setFilename(filename);
        sound.setBlockInstruction(blockInstruction);
        return sound;
    }



    @Override
    public void executeInstruction(InstructionExecutor executor) {
        if (!isPlaying && mFilename!=null && mFilename.length()>4) {
           Player.reset();
            Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Music audio over");
                    isPlaying=false;
                }
            });
            Uri path;

            // check if file is internal prepared file (ie. in raw folder). If so, find path

            if (mFilename.indexOf('/') == -1) {
                int dotPosition = mFilename.lastIndexOf('.');
                String resourceName = mFilename.substring(0, dotPosition).toLowerCase();
     //           int id = mContext.getResources().getIdentifier(resourceName, "raw", "org.maskmedia.roboliterate");
                path = Uri.parse("android.resource://org.maskmedia.roboliterate/raw/" + resourceName);

            } else {

                // file is generated and resides on local storage

                path = Uri.parse(mFilename);
            }
            try {
                Player.setDataSource(mContext, path);
                Player.prepare();
                Log.d(TAG, "Trying to play " + path.toString());

            } catch (IOException e) {
                Log.e(TAG, "prepare() failed "+e.getMessage());
            } catch (NullPointerException npe) {
                Log.e(TAG, "File cannot be found");
            }
            final int audioDuration = Player.getDuration();

            Player.start();
            isPlaying=true;

                // if this is a block instruction, simply wait until the file has played once or the duration has passed

                if (isBlockInstruction()) {
                    waitSomeTime(audioDuration);
                    isPlaying=false;
                    Player.stop();

                } else {
                    synchronized (this) {

                        Thread waitThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                waitSomeTime(audioDuration);

                                isPlaying=false;
                                    synchronized (Player) {
                                        Player.stop();
                                    }
                                }

                        });
                        waitThread.start();
                    }
                }

        }
    }

}