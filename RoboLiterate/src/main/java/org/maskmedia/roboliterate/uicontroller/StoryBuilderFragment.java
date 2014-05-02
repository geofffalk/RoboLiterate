/**
 * Copyright (C) 2013 Geoffrey Falk
 */
package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitSentence;
import org.maskmedia.roboliterate.rlit.RLitStory;

import java.util.ArrayList;


/**
 * Fragment class that contains interface allowing user to add RLitSentences and edit pre-existing
 * sentences
 */
public class StoryBuilderFragment extends ListFragment {

    private StoryBuilderListener mListener;
    private Button mAddSentenceButton;
    private ArrayList<Boolean> mIsCompoundSentenceList;

    /**
     * Callback interface implemented by parent activity,
     */
    public interface StoryBuilderListener {
        void runStory();

        void editSentence();

        void OnSaveButtonClicked();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof StoryBuilderListener) {
            mListener = (StoryBuilderListener) activity;

        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement StoryBuilderFragment.StoryBuilderListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_builder, container, false);
        if (view != null) {
            mAddSentenceButton = (Button) view.findViewById(R.id.addSentenceButton);
            mAddSentenceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // set RLitStory cursor to the end so a new sentence is added, then notify parent
                    // activity that user wants to edit this sentence

                    RLitStory.getStory().setCursorToEnd();
                    mListener.editSentence();
                }
            });
        }
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        RLitStory story = RLitStory.getStory();
        ArrayList<String> storyText = story.getStoryText();

        ArrayList<String> sentenceStringList = new ArrayList<String>();
        mIsCompoundSentenceList = new ArrayList<Boolean>();

        for (int i = 0; i < storyText.size(); i++) {

            int sentenceType = story.getSentenceAtPosition(i).getSentenceType();
            if (sentenceType == RLitSentence.TYPE_EVENTLISTENER_WHEN) {

                // 'When' sentence is detected, so 'glue' it to subsequent sentence so that it appears
                //  as one. Keep a record of which displayed sentences are like this ie. 'compound'

                sentenceStringList.add(storyText.get(i).trim() + storyText.get(++i));
                mIsCompoundSentenceList.add(true);

            } else {
                sentenceStringList.add(storyText.get(i));
                mIsCompoundSentenceList.add(false);
            }

        }
        Activity activity = getActivity();
        if (activity != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                    R.layout.story_builder_row, R.id.label, sentenceStringList);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        int compoundCounter = 0;

        // find selected sentence by first expanding out the sentence list so that the glued 'compound'
        // sentence rows are counted as two

        for (int i = position; i >= 0; i--) {
            if (mIsCompoundSentenceList.get(i)) compoundCounter++;
        }

        // set the current sentence to the selected sentence

        RLitStory.getStory().setCursorPosition(position + compoundCounter);
        mListener.editSentence();
    }

    public void makeActive() {
        mAddSentenceButton.setVisibility(Button.VISIBLE);
    }


}
