/**
 * Copyright (C) 2013 Geoffrey Falk
 *
 * Adapted from example used in Android training module 'Saving Data in SQL Databases'
 * http://developer.android.com/training/basics/data-storage/databases.html
 *
 * Copyright (C) 2010 The Android Open Source Project
 */

package org.maskmedia.roboliterate.rlit.storage;

import android.provider.BaseColumns;

/**
 *
 * Contract class defining the schema for RLitStory SQLite database.
 */

public class RLitStoryDbContract {

    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RLitStoryTable.TABLE_NAME + " (" +
                    RLitStoryTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RLitStoryTable.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    RLitStoryTable.COLUMN_NAME_SENTENCES + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RLitStoryTable.TABLE_NAME;


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RLitStoryDbContract() {}

    /* Inner class that defines the table contents */
    public static abstract class RLitStoryTable implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SENTENCES = "sentences";
    }
}
