/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.maskmedia.roboliterate.R;

/**
 * Sentence Builder Activity that launches SentenceBuilder Fragment and listens for the following:
 * Fragment tells activity when to display RECORD icon in ActionBar, and when an RLitSentence has been
 * fully built and accepted by user.
 *
 * The activity also controls the RecordSound and SaveSound dialogs, and listeners for when an audio file
 * has been saved and when it has been named in the respective dialogs
 *
 *
 */
public class SentenceBuilderActivity extends ActionBarActivity implements
        SentenceBuilderFragment.SentenceBuilderListener,
        RecordSoundDialogFragment.RecordSoundListener,
        SaveSoundDialogFragment.NameSoundListener {

    RecordSoundDialogFragment recordSoundDialogFragment;
    SaveSoundDialogFragment saveSoundDialogFragment;
    MenuItem mRecordMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_builder);
        ActionBar bar = getSupportActionBar();
        if (bar!=null) {
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * Called by fragment when a RLit Sentence has been built and accepted by user.
     * Opens StoryBuilder activity
     */
    @Override
    public void onSentenceBuilt() {
       StoryBuilderFragment fragment = (StoryBuilderFragment) getSupportFragmentManager()
               .findFragmentById(R.id.storyBuilderFragment);

       if (fragment == null || !fragment.isInLayout()) {
           Intent intent = new Intent(getApplicationContext(),
                   StoryBuilderActivity.class);
           intent.putExtra("sentenceBuilt",true);
           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
           startActivity(intent);
       }
    }

    /**
     * Called by fragment, and controls when the Record icon in the ActionBar is turned on and off
     *
     * @param flag  set Record Menu item to visible or invisible
     */
    @Override
    public void enableRecording(boolean flag) {
        if (mRecordMenuItem!=null){
           mRecordMenuItem.setVisible(flag);
        }
    }


    /**
     * Called by RecordSoundDialogFragment, indicates that a new temporary recording has been made.
     * Activity opens the SaveSoundDialog fragment, from where users name their recording
     */
    @Override
    public void onTempItemRecorded() {
        recordSoundDialogFragment.dismiss();
        saveSoundDialogFragment = new SaveSoundDialogFragment();
        saveSoundDialogFragment.show(getSupportFragmentManager(),"Save your recording");


    }

    /**
     * Called from SaveSoundDialogFragment, indicating that the sound has been successfully saved
     * Activity then tells fragment to update its UI, taking into account the new added sound
     *
     * @param dialog    calling fragment
     */
    @Override
    public void onSoundAdded(DialogFragment dialog) {
        SentenceBuilderFragment fragment = (SentenceBuilderFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sentenceBuilderFragment);
        if (fragment != null && fragment.isInLayout()) {
            fragment.updateUI();
        }

    }

    /**
     * Override Back button, so that user is always taken back to StoryBuilderActivity
     */
    @Override
    public void onBackPressed() {
            Intent intent = new Intent(getApplicationContext(), StoryBuilderActivity.class);
            startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.record_action, menu);
        mRecordMenuItem = menu.findItem(R.id.action_record);
        if (mRecordMenuItem!=null)
        mRecordMenuItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record:
                recordSoundDialogFragment = new RecordSoundDialogFragment();
                recordSoundDialogFragment.show(getSupportFragmentManager(),"Make your recording");
                break;
            default:
                break;
        }
        return true;
    }


}
