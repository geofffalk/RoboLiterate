/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitSentence;
import org.maskmedia.roboliterate.rlit.RLitStory;
import org.maskmedia.roboliterate.rlit.storage.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The first activity that allows users to load previously saved RLitStories and create new ones.
 */

public class LaunchActivity extends ActionBarActivity {
    private ArrayAdapter<String> mSavedStoryListAdapter;
   // private DatabaseHelper mDbHelper;
    private List<String[]> mSavedStories;
    private List<String> mStoryTitleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        mSavedStories = dbHelper.getStories();
        setContentView(R.layout.launch_activity);
        Button remoteControlButton = (Button) findViewById(R.id.b_remoteControl);
        remoteControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RemoteControlActivity.class);
                startActivity(intent);
            }
        });
        Button newStoryButton = (Button) findViewById(R.id.b_newStory);
        newStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // start a new RLitStory

                if (RLitStory.getStory().isStarted()) {
                    openNewStoryDialog();
                } else {

                openStoryBuilder(true);
                }
            }
        });
        ListView mListView = (ListView) findViewById(R.id.savedStoryList);
        mStoryTitleList = new ArrayList<String>();
        for (String[] strings : mSavedStories) {
            mStoryTitleList.add(strings[1]);
        }
        mSavedStoryListAdapter = new ArrayAdapter<String>(this, R.layout.savedstories_row, R.id.savedStoriesList_label, mStoryTitleList);
        mListView.setAdapter(mSavedStoryListAdapter);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // set static RLitStory to this saved story from the list

                RLitStory story = RLitStory.getStory();
                String storyTitle = mSavedStories.get(position)[1];
                String storyID = mSavedStories.get(position)[0];
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                ArrayList<RLitSentence> sentenceList = dbHelper.getSentencesForStoryID(storyID);
                story.setSentenceList(sentenceList);
                story.setStoryTitle(storyTitle);
                openStoryBuilder(false);
            }
        });
        /*
        Button continueStoryButton = (Button) findViewById(R.id.b_continueStory);
        continueStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openStoryBuilder(false);
            }
        });

        continueStoryButton.setVisibility(RLitStory.getStory().isStarted() ? Button.VISIBLE : Button.INVISIBLE);

        */
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
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

    private void openNewStoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_start_new_program)
                .setMessage(R.string.message_start_new_program);
        builder.setNegativeButton(R.string.continue_with_current,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openStoryBuilder(false);
            }
        });
        builder.setPositiveButton(R.string.new_program, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openStoryBuilder(true);
            }
        });

        builder.create().show();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, R.id.delete_item, Menu.NONE, "Delete story");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            switch (item.getItemId()) {
                case R.id.delete_item:

                    // Delete selected story from database

                    DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                    dbHelper.deleteStoryWithID(mSavedStories.get((int) info.id)[0]);
                    mSavedStories.remove((int) info.id);
                    mStoryTitleList.remove((int) info.id);
                    mSavedStoryListAdapter.notifyDataSetChanged();
                    return true;

            }
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Start StoryBuilder activity
     * @param reset     set to true if a new story is being started, thus resetting static RLitStory instance
     */
    private void openStoryBuilder(boolean reset) {
        if (reset) RLitStory.resetStory();
        Intent intent = new Intent(getApplicationContext(), StoryBuilderActivity.class);
        startActivity(intent);
    }


}
