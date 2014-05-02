/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.maskmedia.roboliterate.R;

import java.io.File;
import java.io.IOException;

/**
 * RecordSound dialog fragment, allowing user to make, preview and accept a new sound recording using the
 * device's microphone
 */
public class RecordSoundDialogFragment extends DialogFragment {

    private static final String TAG = "RecordSoundDialogFragment";
    private RecordSoundListener mListener;
    private boolean mIsRecording;
    private Button mRecordButton;
    private Dialog mDialog;
    private Activity mActivity;
    private Button mPlayButton;
    private Button mDoneButton;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String mTempFilename;


    public interface RecordSoundListener {
        public void onTempItemRecorded();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        if (activity instanceof RecordSoundListener) {
            mListener = (RecordSoundListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement RecordSoundListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        // Get the layout inflater

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.record_sound_fragment, null);
        if (view!=null) {
        mRecordButton = (Button) view.findViewById(R.id.recordButton);
        mRecordButton.setOnClickListener(mRecordOnClickListener);
        mRecordButton.setText("RECORD");
        mPlayButton = (Button) view.findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(mPlayOnClickListener);
        mPlayButton.setEnabled(false);
        mDoneButton = (Button) view.findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(mDoneOnClickListener);
        mDoneButton.setEnabled(false);
        builder.setView(view).setTitle("Press record to begin");
        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        return mDialog;
        }
        return null;

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * Record a new sound
     */
    private View.OnClickListener mRecordOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            if (mIsRecording) {
                stopRecording();
                mPlayButton.setEnabled(true);
                mDoneButton.setEnabled(true);
                mRecordButton.setText(R.string.record);
                mDialog.setTitle(R.string.press_ok_when_done);
            } else {if (mPlayer!=null) {
                mPlayer.stop();
                mPlayer.release();
            }
                startRecording();

                mPlayButton.setEnabled(false);
                mDoneButton.setEnabled(false);
                mRecordButton.setText(R.string.stop);
                mDialog.setTitle(R.string.now_recording);
            }
            mIsRecording = !mIsRecording;
        }


    };


    private View.OnClickListener mPlayOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            playSound();

        }
    };
    /**
     * Release the MediaPlayer and inform listening activity that the recording is done and user has clicked OK
     */
    private View.OnClickListener mDoneOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mPlayer!=null) {
            mPlayer.release();
            }
            mListener.onTempItemRecorded();
        }
    };

    /**
     * Preview the sound
     */
    private void playSound() {
       mPlayer = new MediaPlayer();
        try {
            Log.d(TAG,"Trying to preview "+mTempFilename);
            mPlayer.setDataSource(mTempFilename);
            mPlayer.prepare();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mDialog.setTitle(R.string.press_ok_when_done);
                }
            });
            mPlayer.start();
            mDialog.setTitle(R.string.now_playing);
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    /**
     * Start recording, saving file as a temporary file on the external storage device
     */
    private void startRecording() {
        mRecorder = new MediaRecorder();
        String fileRoot = Environment.getExternalStorageDirectory().getPath() + "/roboliterate";
        File directory = new File(fileRoot);
        if (directory.canWrite()) {
        mTempFilename = fileRoot + "/tempRecording.3gp";
        Log.d(TAG, "Trying to record " + mTempFilename);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mTempFilename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
        }
    }

    private void stopRecording() {
        mPlayButton.setEnabled(true);
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;


    }




}
