/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Helper class that loads RLitDictionary from text file in resources folder and creates phrases from
 * saved sound files.
 */
public class RLitDictionaryLoader implements Runnable {

    public static final int DATA_LOADED = 1;
    private Context mContext;
    private Resources mResources;
    private int mResourceFile;
    private LoadDataListener mListener;
    public static final String PREFS_SOUNDFILES = "MySoundFiles";



    public interface LoadDataListener {
        void OnDataLoaded(int state);
    }


    public RLitDictionaryLoader(LoadDataListener listener, Context context, int rlitfile) {

        mListener = listener;
        mResources = context.getResources();
        mContext = context;
        mResourceFile = rlitfile;
    }


    @Override
    public void run() {
        try {
            InputStream inputStream = mResources.openRawResource(mResourceFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {

                String line;

                while ((line = reader.readLine()) != null) {

                    String[] strings = TextUtils.split(line, ",");

                    if (strings.length < 9) continue;
                    String label = strings[0].trim();
                    int id = Integer.parseInt(strings[1].trim());
                    int pid = Integer.parseInt(strings[2].trim());
                    int instruction = Integer.parseInt(strings[3].trim());
                    int delay = Integer.parseInt(strings[4].trim());
                    int arg1 = Integer.parseInt(strings[5].trim());
                    int arg2 = Integer.parseInt(strings[6].trim());
                    int arg3 = Integer.parseInt(strings[7].trim());
                    String arg4 = strings[8].trim();
                    int waitFlag = Integer.parseInt(strings[9].trim());
                    int sortIndex = Integer.parseInt(strings[10].trim());


                    if (pid == RLitPhrase.COMMA_PID) {
                        label = ",";
                    }

                    // Create RLitPhrase from parsed line from resource file

                    RLitPhrase rsp = new RLitPhrase(label, id, pid, instruction, delay, arg1, arg2,
                            arg3, arg4, waitFlag, sortIndex);

                    RLitDictionary.addPhrase(rsp);


                }
            } finally {
                reader.close();
            }


            // add user sound files to dictionary

            String rootDirectory = Environment.getExternalStorageDirectory().getPath() + "/roboliterate";
            File root = new File(rootDirectory);
            boolean success = root.mkdirs();
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    String filePath = file.getAbsolutePath();
                    String filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf('.'));

                    // find user's name for file in Shared Preferences file

                    SharedPreferences userTitles = mContext.getSharedPreferences(PREFS_SOUNDFILES, 0);
                    String title = userTitles.getString(filePath.trim(), "default"+filename);

                    // Create RLitPhrase for file

                    RLitPhrase rsp = new RLitPhrase("'" + title + "'.", RLitPhrase.ANDROID_DIALOGFILE_ID,
                            RLitPhrase.ANDROID_DIALOGFILE_PID, 0, 0, 0, 0, 0, filePath, 0, 0);
                    RLitDictionary.addPhrase(rsp);

                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mListener.OnDataLoaded(DATA_LOADED);
    }



}
