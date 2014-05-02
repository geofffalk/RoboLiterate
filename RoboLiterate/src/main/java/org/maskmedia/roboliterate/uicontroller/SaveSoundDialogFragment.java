/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitDictionary;
import org.maskmedia.roboliterate.rlit.RLitPhrase;

import java.io.File;

/**
 * SaveSound dialog fragment, allowing user to name their sound before it is saved locally.
 */

public class SaveSoundDialogFragment extends DialogFragment {

    private EditText mSoundName;
    private NameSoundListener mListener;
    public static final String PREFS_SOUNDFILES = "MySoundFiles";
    private Activity mActivity;

    public interface NameSoundListener {
        void onSoundAdded(DialogFragment dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        if (activity instanceof NameSoundListener) {
            mListener = (NameSoundListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement NameSoundListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        // Get the layout inflater

        LayoutInflater inflater = mActivity.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        View view = inflater.inflate(R.layout.rename_soundfile_dialog, null);
        if (view != null) {
            Button mCancelButton = (Button) view.findViewById(R.id.cancelButton);
            Button mDoneButton = (Button) view.findViewById(R.id.doneButton);
            mSoundName = (EditText) view.findViewById(R.id.userSoundName);
            builder.setView(view).setTitle(R.string.name_your_recording);
            final Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);

            // save soundfile with current timestamp, to ensure all soundfiles are unique. Saved as a Shared
            // Preference the title that the user has given it. This will persist until the application is
            // deleted and allows application to remember which soundfile names are mapped to which title.
            // This also allows users to name sounds whatever they want, including dots and punctuation

            mDoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CharSequence cs = mSoundName.getText();
                    if (cs != null) {
                        String title = cs.toString().trim();
                        long timestamp = System.currentTimeMillis();
                        File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/roboliterate");
                        File oldFile = new File(directory, "tempRecording.3gp");
                        File newFile = new File(directory, timestamp + ".3gp");
                        boolean success = oldFile.renameTo(newFile);
                        if (success) {

                            // add phrase to currently loaded dictionary

                            RLitDictionary.addPhrase(new RLitPhrase("'" + title + "'.", RLitPhrase.ANDROID_DIALOGFILE_ID,
                                    RLitPhrase.ANDROID_DIALOGFILE_PID, 0, 0, 0, 0, 0, newFile.getPath(), 0, -1));

                            // save as SharedPreference the user's filename mapped to filename of sound

                            SharedPreferences soundfilePrefs = mActivity.getSharedPreferences(PREFS_SOUNDFILES, 0);
                            SharedPreferences.Editor editor = soundfilePrefs.edit();
                            editor.putString(newFile.getPath(), title);
                            editor.commit();

                            // inform listening Activity that the sound has been added

                            mListener.onSoundAdded(SaveSoundDialogFragment.this);
                            dialog.cancel();
                        }
                    }
                }
            });
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // remove temporary file from the memory, as user does not want to save the sound

                    File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/roboliterate");
                    File oldFile = new File(directory, "tempRecording.3gp");
                    boolean success = oldFile.delete();
                    if (success)
                        Toast.makeText(mActivity, R.string.file_not_saved, Toast.LENGTH_SHORT).show();
                    dialog.cancel();


                }
            });

            // make sure the text edit field is in focus when the fragment opens, meaning the keyboard will automatically appear

            mSoundName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Dialog dialog = getDialog();
                        if (dialog != null) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                }
            });


            return dialog;
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSoundName.requestFocus();
    }
}
