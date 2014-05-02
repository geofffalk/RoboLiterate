/**
 * Copyright (C)  2013 Geoffrey Falk
 *
 * Use of GSON with generics adapted from examples provided by Google:
 * https://sites.google.com/site/gson/gson-user-guide#TOC-Object-Examples
 */

package org.maskmedia.roboliterate.rlit.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.maskmedia.roboliterate.rlit.RLitSentence;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.maskmedia.roboliterate.rlit.storage.RLitStoryDbContract.*;

/**
 * Database helper class that provides UI classes with access to RLitStory database.
 * Stories can be loaded via ID and saved by Title, and lists of all available Story titles can be
 * returned.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "RLitStories.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);

    }

    /**
     * @return a List of String arrays containing Story titles and IDs. Note there may be more than
     * one occurrence of a Story title, necessitating the use of IDs.
     */
    public List<String[]> getStories() {
        List<String[]> list = new ArrayList<String[]>();
        SQLiteDatabase db = getReadableDatabase();
        if (db!=null) {
        Cursor cursor = db.query(RLitStoryTable.TABLE_NAME,
                new String[]{RLitStoryTable._ID, RLitStoryTable.COLUMN_NAME_TITLE},
                null, null,
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String[] string = new String[]{cursor.getString(0), cursor.getString(1)};
            list.add(string);
            cursor.moveToNext();

        }
        }
        return list;
    }

    /**
     *
     * @param id    The ID of the Story
     * @return      an ArrayList of Sentences contained in the Story with the provided ID
     */
    public ArrayList<RLitSentence> getSentencesForStoryID(String id) {
        SQLiteDatabase db = getReadableDatabase();
        if (db!=null) {
        Cursor cursor = db.query(RLitStoryTable.TABLE_NAME,
                new String[]{RLitStoryTable.COLUMN_NAME_SENTENCES},
                RLitStoryTable._ID + " = '" + id + "'",
                null, null, null, null);
        cursor.moveToFirst();
        Gson gson = new Gson();
        Type sentenceListType = new TypeToken<ArrayList<RLitSentence>>() {}.getType();
        return gson.fromJson(cursor.getString(0), sentenceListType);
        }
        return null;
    }

    /**
     *
     * @param id    ID of story to delete
     * @return      success of deletion
     */
    public boolean deleteStoryWithID(String id) {
        SQLiteDatabase db = getReadableDatabase();
        if (db!=null) {
        int result = db.delete(RLitStoryTable.TABLE_NAME, RLitStoryTable._ID + " = '" + id + "'", null);
        return (result == 1);
        }
        return false;
    }

    /**
     *
     * @param storyName         the title of the story to be saved
     * @param sentenceList      the list of RLitSentences to be saved
     * @return                  the story ID
     */
    public long saveStory(String storyName, ArrayList<RLitSentence> sentenceList) {

        ContentValues values = new ContentValues();
        values.put(RLitStoryTable.COLUMN_NAME_TITLE, storyName);

       // convert RLitSentence list to JSON and insert as ContentValue pair

        Gson gson = new Gson();
        Type sentenceListType = new TypeToken<ArrayList<RLitSentence>>() {
        }.getType();
      // String sentences = gson.toJson(sentenceList, sentenceListType);
        values.put(RLitStoryTable.COLUMN_NAME_SENTENCES, gson.toJson(sentenceList));
      // values.put(RLitStoryTable.COLUMN_NAME_SENTENCES, gson.toJson(sentences));

      // Insert the new row, returning the primary key value of the new row

        long newRowId;
        SQLiteDatabase db = getWritableDatabase();
        if (db!=null) {
        newRowId = db.insert(
                RLitStoryTable.TABLE_NAME, null, values);
        Cursor cursor = db.query(RLitStoryTable.TABLE_NAME,
                new String[]{RLitStoryTable.COLUMN_NAME_TITLE, RLitStoryTable.COLUMN_NAME_SENTENCES},
                RLitStoryTable._ID + " = " + newRowId, null,
                null, null, null);
        cursor.moveToFirst();
        return newRowId;
        }
        return 0;
    }

    /**
     *
     * @param storyID       ID of story to be updated
     * @param sentenceList  List of sentences to update saved story with.
     */
    public void updateStory(long storyID, ArrayList<RLitSentence> sentenceList) {
        ContentValues values = new ContentValues();
        Gson gson = new Gson();
        Type sentenceListType = new TypeToken<ArrayList<RLitSentence>>() {
        }.getType();
        String sentences = gson.toJson(sentenceList, sentenceListType);
       values.put(RLitStoryTable.COLUMN_NAME_SENTENCES, gson.toJson(sentenceList));
    //    values.put(RLitStoryTable.COLUMN_NAME_SENTENCES, gson.toJson(sentences));
        SQLiteDatabase db = getWritableDatabase();
        if (db!=null) {
        db.update(RLitStoryTable.TABLE_NAME, values, RLitStoryTable._ID + " = " + storyID, null);
        }
        }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
