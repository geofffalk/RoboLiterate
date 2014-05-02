/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitDictionary;
import org.maskmedia.roboliterate.rlit.RLitDictionaryLoader;
import org.maskmedia.roboliterate.rlit.RLitStory;
import org.maskmedia.roboliterate.rlit.storage.DatabaseHelper;

/**
 * Story Builder activity that launches StoryBuilder fragment and listens for events from this fragment,
 * such as user pressing Save and Run ActionBar icons.
 * Also listens for users entering the title of their saved story.
 * Activity only launches StoryBuilder Fragment once the RLitDictionary has been loaded
 *
 */


public class StoryBuilderActivity extends ActionBarActivity implements
        RLitDictionaryLoader.LoadDataListener,
        StoryBuilderFragment.StoryBuilderListener,
        SaveStoryDialogFragment.SaveStoryListener

{

    private static final String TAG = "StoryBuilderActivity";
    private boolean playPressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_builder);
        // if (savedInstanceState==null) {
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if (bar!=null) {
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        }
        if (!RLitDictionary.hasContents()) {
            Log.d(TAG, "Loading dictionary");
            RLitDictionaryLoader dataLoader = new RLitDictionaryLoader(this, getApplicationContext(), R.raw.rlit_dictionary);
            dataLoader.run();

        } else {

            init();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        MenuItem item = menu.findItem(R.id.action_edit);
        if (item!=null)
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play:
                if (RLitStory.getStory().getStoryID()!=0 || !RLitStory.getStory().isStoryChanged()) {
                runStory();
                } else {
                    playPressed = true;
                    OnSaveButtonClicked();
                }
                break;
            case R.id.action_save:
                OnSaveButtonClicked();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
            default:
                break;
        }
        return true;
    }

    /**
     * Called once the RLitDictionary has been loaded, and launches StoryBuilder fragment
     */
    private void init() {
        StoryBuilderFragment fragment = (StoryBuilderFragment) getSupportFragmentManager()
                .findFragmentById(R.id.storyBuilderFragment);
        if (fragment != null && fragment.isInLayout()) {
            fragment.makeActive();

        }
    }


    @Override
    public void OnDataLoaded(int state) {
        switch (state) {
            case RLitDictionaryLoader.DATA_LOADED:
                init();
                break;


        }
    }

    @Override
    public void runStory() {
        RLitStory story = RLitStory.getStory();

        // auto-save the story before running

        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        helper.updateStory(story.getStoryID(),story.getSentenceList());

        // launch the ExecuteStory activity

        Intent intent = new Intent(getApplicationContext(), ExecuteStoryActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the Save Story dialog fragment to allow users to input their story title. Only opens if the
     * story hasn't been saved already, otherwise save story using same title as before
     */
    @Override
    public void OnSaveButtonClicked() {
        RLitStory story =RLitStory.getStory();
        if (story.getStoryID()==0) {
        SaveStoryDialogFragment fragment = new SaveStoryDialogFragment();
        fragment.show(getSupportFragmentManager(), "Type the name of your program");
        } else
        {
            saveStory();
        }
    }

    /**
     * Called from SentenceBuilderFragment, a sentence has been selected by user, or the Add Sentence button
     * has been pressed, so start SentenceBuilderActivity if the SentenceBuilder fragment isnt already onscreen
     */
    @Override
    public void editSentence() {
        SentenceBuilderFragment fragment = (SentenceBuilderFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sentenceBuilderFragment);
        if (fragment == null || !fragment.isInLayout()) {
            Intent intent = new Intent(getApplicationContext(),
                    SentenceBuilderActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Called from SaveStoryDialogFragment, saves a new story under the title inputted by user
     */
    @Override
    public void saveNewStory(String storyName) {
        RLitStory story = RLitStory.getStory();
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        long storyID = helper.saveStory(storyName,story.getSentenceList());
        story.setStoryID(storyID);
        story.setStoryTitle(storyName);
        if (playPressed) {
            playPressed = false;
            runStory();
        }
    }

    @Override
    public void cancelSaveStory() {
        playPressed = false;
    }

    /**
     * resaves a previously saved story
     */
    private void saveStory() {
        RLitStory story = RLitStory.getStory();
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        helper.updateStory(story.getStoryID(),story.getSentenceList());
        Toast.makeText(this, R.string.story_saved,Toast.LENGTH_SHORT).show();
    }
}
