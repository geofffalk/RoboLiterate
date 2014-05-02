/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;

import java.util.ArrayList;

/**
 * RLit Story entity class, which contains a List of RLitSentences and manages
 * cursor position. There is only ever one instance active at any time, making
 * this a Singleton class
 */
public class RLitStory {

    private ArrayList<RLitSentence> sentenceList;
    private int cursorPosition;
    private RLitSentence currentSentence;
    private long storyID;
    private boolean storyChanged;
    private String storyTitle;
    private static RLitStory story;

    private RLitStory() {
        sentenceList = new SentenceArrayList();
        cursorPosition = 0;
        storyChanged = false;
    }

    public static RLitStory getStory() {
        if (story == null) {
            story = new RLitStory();
        }
        return story;
    }

    public void setStoryID(long storyID) {
        this.storyID = storyID;
    }


    public long getStoryID() {
        return storyID;
    }
    public static void resetStory() {
        story = new RLitStory();
    }

    public boolean isStarted() {
        return (sentenceList.size() > 0);
    }
//
//    public void clearSentences() {
//        sentenceList.clear();
//    }


    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int position) {
        cursorPosition = position;
    }

    /**
     * Set cursor to the end of the Story, ready for insertion of new sentence
     */
    public void setCursorToEnd() {
        int storySize = sentenceList.size();
        if (storySize > 0) {

            // check to see if the last sentence is complete ie. contains at least a sequencer/first phrase, noun, and verb

            if (sentenceList.get(storySize - 1).isComplete()) {
                cursorPosition = sentenceList.size();
            } else {

                // last sentence is not yet complete, so set cursorPosition to this sentence

                cursorPosition = sentenceList.size() - 1;
            }
        } else {
            cursorPosition = 0;
        }
    }


    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public boolean isStoryChanged() {
        return storyChanged;
    }

    public ArrayList<RLitSentence> getSentenceList() {
        return sentenceList;
    }

//    public RLitSentence[] getSentences() {
//        return (RLitSentence[]) sentenceList.toArray();
//    }

    public RLitSentence getSentenceAtPosition(int index) {
        if (index <= sentenceList.size()) {
            return sentenceList.get(index);
        }
        return null;
    }

    /**
     * Insert a sentence at the position of the cursor
     * @return - RLitSentence
     */
    public RLitSentence insertSentenceAtCursor() {
        currentSentence = new RLitSentence();
        sentenceList.add(cursorPosition, currentSentence);
        return currentSentence;
    }

    /**
     * Return the sentence at the current cursor position, and if there isnt a sentence, i.e. the
     * cursor is beyond the end of the list, then insert a new sentence at the end and return list
     * @return  RLitSentence
     */
    public RLitSentence getSentenceAtCursor() {
        if (cursorPosition >= sentenceList.size()) {
            currentSentence = new RLitSentence();
            sentenceList.add(currentSentence);
        } else {
            currentSentence = sentenceList.get(cursorPosition);
        }
        return currentSentence;
    }

    /**
     * Returns the last sentence entered before the current cursor position
     * @return RLItSentence
     */
    public RLitSentence getLastSentence() {
        if (cursorPosition>0)
            return sentenceList.get(cursorPosition - 1);
        return null;

    }

    public void setSentenceList(ArrayList<RLitSentence> sentences) {
        sentenceList = sentences;
    }

    public void removeSentence(RLitSentence sentence) {
        storyChanged=true;
        if (cursorPosition > 0) {
            sentenceList.remove(sentence);
        }
    }



    public int size() {
        return sentenceList.size();
    }

    /**
     * Get all the sentences as a List of text Strings
     * @return - ArrayList<String>
     */
    public ArrayList<String> getStoryText() {
        ArrayList<String> story = new ArrayList<String>();
        for (RLitSentence sentence : sentenceList) {
            story.add(sentence.toString());
        }
        return story;
    }


    class SentenceArrayList extends ArrayList<RLitSentence> {
        @Override
        public boolean add(RLitSentence object) {
            storyChanged =true;
            return super.add(object);
        }

        @Override
        public RLitSentence set(int index, RLitSentence object) {
            storyChanged =true;
            return super.set(index, object);

        }

        @Override
        public boolean remove(Object object) {
            storyChanged =true;
            return super.remove(object);
        }

        @Override
        public RLitSentence remove(int index) {
            storyChanged =true;
            return super.remove(index);
        }
    }

}

