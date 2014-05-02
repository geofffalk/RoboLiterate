package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitStory;

/**
 * Created by jeff on 10/08/2013.
 */
public class SaveStoryDialogFragment extends DialogFragment {

    EditText mStoryName;
    SaveStoryListener mListener;

    public interface SaveStoryListener {
        void saveNewStory(String storyName);
        void cancelSaveStory();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SaveStoryListener) {
            mListener = (SaveStoryListener) activity;
        } else {
            throw new ClassCastException(activity.toString()+" must implement SaveStoryListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        View view = inflater.inflate(R.layout.save_story_dialog, null);
        mStoryName = (EditText) view.findViewById(R.id.storyName);
        String storyTitle = RLitStory.getStory().getStoryTitle();
        if (storyTitle!=null)
            mStoryName.setText(storyTitle);
        mStoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        Button doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = mStoryName.getText().toString();
                if (text!="") {
                mListener.saveNewStory(text);
                SaveStoryDialogFragment.this.getDialog().dismiss();
                }
                }
        });
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveStoryDialogFragment.this.getDialog().cancel();
                mListener.cancelSaveStory();
            }
        });


        builder.setView(view).setTitle("Name your program");


        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        mStoryName.requestFocus();
    }
}
