/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.rlit.RLitDictionary;
import org.maskmedia.roboliterate.rlit.RLitPhrase;
import org.maskmedia.roboliterate.rlit.RLitSentence;
import org.maskmedia.roboliterate.rlit.RLitStory;
import org.maskmedia.roboliterate.rlit.instructions.Instruction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment UI class which provides sentence building interface to user. Successive lists of phrases
 * are displayed, allowing user to construct sentences according to RLit ontological rules.
 * <p/>
 * Sentences can be deleted phrase by phrase, and accepted once all possible phrases have been exhausted
 * in a sentence
 */
public class SentenceBuilderFragment extends ListFragment {

    private static final String TAG = "StoryBuilderFragment";
    private Button mExecuteButton;
    private TextView mConsole;
    private List<RLitPhrase> mCurrentPhraseList;
    private RLitSentence mEditSentence;
    private RLitSentence mWhenClauseSentence;
    private RLitStory mStory;
    private Activity mActivity;
    public int selectedItem = -1;
    private SentenceBuilderListener mListener;

    public interface SentenceBuilderListener {
        void onSentenceBuilt();

        void enableRecording(boolean flag);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        if (activity instanceof SentenceBuilderListener) {
            mListener = (SentenceBuilderListener) activity;

        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement SentenceBuilderFragment.SentenceBuilderListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sentence_builder, container, false);
        if (view != null) {
            mExecuteButton = (Button) view.findViewById(R.id.executeButton);
            mExecuteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSentenceBuilt();

                }
            });
            ImageButton mDeleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
            mDeleteButton.setOnClickListener(mDeleteClickListener);
            mConsole = (TextView) view.findViewById(R.id.programText);

            return view;
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mStory = RLitStory.getStory();
        mEditSentence = mStory.getSentenceAtCursor();
        if (mEditSentence.getSentenceType() == RLitSentence.TYPE_EVENTRESULT) {
            mWhenClauseSentence = mStory.getSentenceAtPosition(mStory.getCursorPosition() - 1);
        }
        updateUI();
    }

    public void updateUI() {

        // update the phrase list

        if (mStory.size() <= 1 && mEditSentence.length() == 0) {

            // First sentence of story, so get 'First' phrase

            mCurrentPhraseList = RLitDictionary.getListWithPid(RLitPhrase.FIRST_PID);


        } else if (mWhenClauseSentence != null && mEditSentence.length() == 0) {

            // This is a EventResult sentence, so needs to start with a comma phrase

            mCurrentPhraseList = RLitDictionary.getListWithPid(RLitPhrase.COMMA_PID);


        } else {

            // Show the next phrase list based on the PID of last added phrase


            mCurrentPhraseList = RLitDictionary.getListWithPid(mEditSentence.getLastId());
        }

        if (mCurrentPhraseList != null) {

            //   check to see if the first phrase in list is a COMMA, in which case label most recent Sentence as a
            // When clause sentence, and insert a new Sentence in RLitSentence

            if (mCurrentPhraseList.get(0).getPid() == RLitPhrase.COMMA_PID && mWhenClauseSentence == null) {
                mWhenClauseSentence = mStory.getSentenceAtCursor();
                mStory.setCursorPosition(mStory.getCursorPosition() + 1);
                mEditSentence = null;
                mEditSentence = mStory.insertSentenceAtCursor();
            }


            conformPhraseList();
            mListener.enableRecording((mCurrentPhraseList.get(0).getPid() == RLitPhrase.ANDROID_DIALOGFILE_PID));
            mExecuteButton.setVisibility(Button.INVISIBLE);
            RLitPhraseArrayAdapter adapter = new RLitPhraseArrayAdapter(mActivity, android.R.layout.simple_list_item_1, mCurrentPhraseList);
            setListAdapter(adapter);
            ListView view = getListView();
            if (view != null) {
                registerForContextMenu(view);
            }

        } else {

            mExecuteButton.setVisibility(Button.VISIBLE);
            setListAdapter(null);
        }
        String consoleText = (mWhenClauseSentence == null ? mEditSentence.toString() : mWhenClauseSentence.toString().trim() + mEditSentence.toString());
        mConsole.setText(consoleText);
    }


    /**
     * checks through current Phrase List for any exceptional cases that conflict with RLit ontology and remove
     * these phrases from the list
     */

    private void conformPhraseList() {

        if (mCurrentPhraseList.get(0).getPid() == RLitPhrase.DEFAULT_SEQUENCERS_PID && mStory.size() > 1) {

            // Standard sequencer phrases are detected

            RLitSentence lastSentence = mStory.getLastSentence();

            if (lastSentence.getSentenceType() == RLitSentence.TYPE_EVENTLISTENER_WAIT) {

                // Previous sentence is an Event Listener (Wait) sentence, so this sentence must be an Event Result
                // and avoid concurrent sequencing. Therefore remove Sequencer phrases such as 'At the
                // same time' which have a WaitFlag of 0

                for (int i = 0; i < mCurrentPhraseList.size(); i++) {
                    if (mCurrentPhraseList.get(i).getWaitFlag() == 0) {
                        mCurrentPhraseList.remove(i);
                    }
                }
            }

            if (lastSentence.getInstructionID() == Instruction.INSTRUCTION_REPEAT) {

                // As the last sentence contains a Phrase with REPEAT InstructionID, remove phrase 'Repeat' from the list.
                // This shields user from inadvertently creating infinite loops

                for (int i = 0; i < mCurrentPhraseList.size(); i++) {
                    if (mCurrentPhraseList.get(i).getId() == RLitPhrase.REPEAT_ID) {
                        mCurrentPhraseList.remove(i);
                    }
                }
            }
        }

        if (mCurrentPhraseList.get(0).getId() == RLitPhrase.REPEAT_MODIFIER_ID) {

            // Modifier phrases with Repeat PIDs are detected in list. Adjust the modifier phrase selection to show only those
            // phrases that allow repetition of up to the total number of preceding sentences since last Repeat instruction.
            // This will prevent overlapping loops in the final Program.

            List<RLitSentence> sentenceList = mStory.getSentenceList();
            int lastCursorPosition = mStory.getCursorPosition() - 1;
            int compoundSentenceCount = 0;

            @SuppressLint("UseSparseArrays")
            Map<Integer, Integer> repeatableSentenceMap = new HashMap<Integer, Integer>();
            int counter = 1;
            for (int i = lastCursorPosition; i >= 0; i--) {
                RLitSentence sentence = sentenceList.get(i);
                if (sentence.getInstructionID() == Instruction.INSTRUCTION_REPEAT)
                    break;
                if (sentence.getSentenceType() == RLitSentence.TYPE_EVENTRESULT) {
                    compoundSentenceCount++;
                    i--;
                }
                repeatableSentenceMap.put(counter, compoundSentenceCount + counter++);
            }

            for (int i = mCurrentPhraseList.size() - 1; i >= 0; i--) {
                RLitPhrase phrase = mCurrentPhraseList.get(i);
                int repeatArg = phrase.getArg1();
                if (repeatableSentenceMap.containsKey(repeatArg)) {
                    phrase.setTempVar(repeatableSentenceMap.get(repeatArg));
                } else {
                    mCurrentPhraseList.remove(phrase);
                }
            }

        }

    }

    /**
     * Delete the last phrase in the sentence
     */
    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean success=false;
            if (mWhenClauseSentence != null && mEditSentence.length() <= 0) {

                // If this is a compound sentence and the comma has just been deleted, delete last sentence
                // and relabel second last sentence as a simple sentence.

                RLitSentence oldSentence = mEditSentence;
                mStory.removeSentence(oldSentence);
                mStory.setCursorPosition(mStory.getCursorPosition() - 1);
                mEditSentence = mStory.getSentenceAtCursor();
                mWhenClauseSentence = null;
                success = mEditSentence.deleteLastPhrase();


            } else if (mEditSentence.length() > 0)

                // else just delete the last phrase
                success = mEditSentence.deleteLastPhrase();
            else if (mEditSentence.length() <= 0) {

                // if there are no more phrases left to delete, remove the sentence and notify activity listener that
                // editing is done.

                mStory.removeSentence(mEditSentence);
                mListener.onSentenceBuilt();
                return;
            }
            if (success) updateUI();

        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v instanceof Button) {
            Button b = (Button) v;
            CharSequence cs = b.getText();
            if (cs != null) {
                String phrase = cs.toString();
                menu.add(Menu.NONE, R.id.play_item, Menu.NONE, R.string.play_sound);
                menu.add(Menu.NONE, R.id.delete_item, Menu.NONE, "Delete " + phrase);
                for (int i = 0; i < mCurrentPhraseList.size(); i++) {
                    if (mCurrentPhraseList.get(i).getLabel().equals(phrase)) {
                        selectedItem = i;
                        break;
                    }

                }
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        RLitPhrase phrase = mCurrentPhraseList.get(selectedItem);
        String filename = phrase.getArg4();
        switch (item.getItemId()) {
            case R.id.delete_item:

                // delete the selected item iff it is a sound file saved on the SDCard

                if (phrase.getPid() == RLitPhrase.ANDROID_DIALOGFILE_PID && filename.indexOf('/') != -1) {
                    Log.d(TAG, "Deleting file " + phrase.getInstruction());
                    File file = new File(phrase.getArg4());
                    boolean success = file.delete();
                    if (success) {
                        Toast.makeText(mActivity, "Recording deleted.", Toast.LENGTH_SHORT).show();
                        RLitDictionary.removePhrase(phrase.getPid(), phrase.toString());
                    }
                }
                updateUI();
                return true;
            case R.id.play_item:

                // play the selected item iff it is a sound file saved on the SDCard

                if (phrase.getPid() == RLitPhrase.ANDROID_DIALOGFILE_PID) {
                    final MediaPlayer player = new MediaPlayer();
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            player.release();
                        }
                    });
                    Uri path;
                    if (filename.indexOf('/') == -1) {
                        int dotPosition = filename.lastIndexOf('.');
                        String resourceName = filename.substring(0, dotPosition).toLowerCase();
                        path = Uri.parse("android.resource://org.maskmedia.roboliterate/raw/" + resourceName);

                    } else {
                        path = Uri.parse(filename);
                    }
                    try {
                        player.setDataSource(mActivity.getApplicationContext(), path);
                        player.prepare();
                    } catch (IOException e) {
                        Log.e(TAG, "prepare() failed " + e.getMessage());
                    } catch (NullPointerException npe) {
                        Log.e(TAG, "File cannot be found");
                    }
                    player.start();
                }
                break;

            default:
                break;

        }
        return super.onContextItemSelected(item);
    }

    /**
     * Bespoke ArrayAdapter class for PhraseList, with customised row appearance and registering context menu
     * with phrases linked to locally saved audio files
     */
    private class RLitPhraseArrayAdapter extends ArrayAdapter<RLitPhrase> {

        HashMap<RLitPhrase, Integer> mIdMap = new HashMap<RLitPhrase, Integer>();

        public RLitPhraseArrayAdapter(Context context, int textViewResourceId,
                                      List<RLitPhrase> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            RLitPhrase item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int index, View view, final ViewGroup parent) {
            if (view == null) {
                Context parentContext = parent.getContext();
                if (parentContext != null) {
                    LayoutInflater inflater = LayoutInflater.from(parentContext);
                    view = inflater.inflate(R.layout.sentence_builder_row, parent, false);
                }
            }
            final RLitPhrase phrase = getItem(index);
            Button button = (Button) view.findViewById(R.id.button_phrase);
            if (phrase.getPid() == RLitPhrase.ANDROID_DIALOGFILE_PID && phrase.getArg4().indexOf('/') != -1) {
                // allow deletion of user generated phrases
                registerForContextMenu(button);
            }
            button.setText(phrase.getLabel());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEditSentence.addPhrase(phrase);
                    updateUI();
                }
            });

            return view;
        }

    }
}
