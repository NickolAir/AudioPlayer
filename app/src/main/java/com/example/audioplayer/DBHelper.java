package com.example.audioplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    protected static final String DATABASE_NAME = "playlist_database";
    private static final int DATABASE_VERSION = 1;

    // Table
    protected static final String TABLE_COMPOSITIONS = "compositions";
    protected static final String COLUMN_ID = "id";
    protected static final String COLUMN_NAME = "name";
    protected static final String COLUMN_HASH = "hash";

    protected static final String CREATE_TABLE_COMPOSITIONS = "CREATE TABLE " + TABLE_COMPOSITIONS +
            "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_HASH + " TEXT UNIQUE)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PLAYLIST (COLUMN_NAME TEXT, COLUMN_HASH TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
