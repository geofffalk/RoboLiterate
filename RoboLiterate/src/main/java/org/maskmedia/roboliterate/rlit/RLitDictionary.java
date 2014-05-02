/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dictionary holding all RLitPhrases, sorted in a Map, grouped under their Parent IDs as keys
 * This is because the Parent IDs are used to build phrase lists when user is constructing sentences
 * There is only one Dictionary, so this is a Singleton class
 */
public class RLitDictionary {

    private static final String TAG = "RLitDictionary";
    @SuppressLint("UseSparseArrays")
    private static Map<Integer, List<RLitPhrase>> contents = new HashMap<Integer, List<RLitPhrase>>();

    private RLitDictionary() {
    }

    public static void setContents(Map<Integer, List<RLitPhrase>> phraseMap) {
        contents = phraseMap;
    }

    public static Map<Integer, List<RLitPhrase>> getContents() {
        return contents;
    }

    /**
     * Returns list of phrases with given PID
     * @param i - PID number
     * @return List - list of RLitPhrases with given PID
     */
    public static List<RLitPhrase> getListWithPid(int i) {
        if (contents.get(i) != null) {
            List<RLitPhrase> list = new ArrayList<RLitPhrase>();
            for (RLitPhrase phrase : contents.get(i)) {
                try {
                    list.add((RLitPhrase) phrase.clone());
                } catch (CloneNotSupportedException e) {
                    Log.e(TAG, "Cannot clone phrase " + phrase.toString());
                }
            }
            Collections.sort(list);
            return list;
        }
        return null;
    }

    public static boolean hasContents() {
        return contents.size() > 0;
    }

//    public static RLitPhrase getFirstPhrase() {
//        return getListWithPid(-1).get(0);
//    }

    /**
     * Adds phrase to Dictionary
     * @param phrase - phrase to add
     */
    public static void addPhrase(RLitPhrase phrase) {
        int pid = phrase.getPid();
        if (!contents.containsKey(pid)) {
            ArrayList<RLitPhrase> list = new ArrayList<RLitPhrase>();
            list.add(phrase);
            contents.put(pid, list);

        } else {
            contents.get(pid).add(0, phrase);
        }
    }

    /**
     * Removes phrase from Dictionary
     * @param pid - key for phrase
     * @param label - the label of the phrase
     * @return boolean - deletion success or failure
     */
    public static boolean removePhrase(int pid, String label) {
        List<RLitPhrase> list = contents.get(pid);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).toString().equals(label)) {
                list.remove(i);
                return true;

            }
        }
        return false;
    }


}
