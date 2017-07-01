package com.wolfgoes.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wolfgoes.popularmovies.data.MoviesContract.MovieEntry;
import com.wolfgoes.popularmovies.data.MoviesContract.ReviewEntry;
import com.wolfgoes.popularmovies.data.MoviesContract.VideoEntry;

public class MoviesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";

    private static final int DATABASE_VERSION = 6;

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_MOVIE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, " +
                MovieEntry.COLUMN_TITLE + " TEXT, " +
                MovieEntry.COLUMN_POSTER_URL + " TEXT, " +
                MovieEntry.COLUMN_BACKDROP_URL + " TEXT, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT, " +
                MovieEntry.COLUMN_RELEASE + " INTEGER, " +
                MovieEntry.COLUMN_RATING + " REAL);";

        final String CREATE_TABLE_REVIEW = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " STRING PRIMARY KEY ON CONFLICT REPLACE, " +
                MovieEntry.TABLE_NAME + MovieEntry._ID + " INTEGER, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT, " +
                "FOREIGN KEY (" + MovieEntry.TABLE_NAME + MovieEntry._ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "(" + MovieEntry._ID + ") ON DELETE CASCADE);";

        final String CREATE_TABLE_VIDEO = "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                VideoEntry._ID + " STRING PRIMARY KEY ON CONFLICT REPLACE, " +
                MovieEntry.TABLE_NAME + MovieEntry._ID + " INTEGER, " +
                VideoEntry.COLUMN_NAME + " TEXT, " +
                VideoEntry.COLUMN_SITE + " TEXT, " +
                VideoEntry.COLUMN_KEY + " TEXT, " +
                "FOREIGN KEY (" + MovieEntry.TABLE_NAME + MovieEntry._ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "(" + MovieEntry._ID + ") ON DELETE CASCADE);";

        db.execSQL(CREATE_TABLE_MOVIE);
        db.execSQL(CREATE_TABLE_REVIEW);
        db.execSQL(CREATE_TABLE_VIDEO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
