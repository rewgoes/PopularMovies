package com.wolfgoes.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.wolfgoes.popularmovies.model.Movie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TestMovieContentProvider {

    private final Context mContext = InstrumentationRegistry.getTargetContext();
    private SQLiteDatabase database;

    @Before
    public void setUp() {
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        database = dbHelper.getWritableDatabase();
        database.delete(MoviesContract.MovieEntry.TABLE_NAME, null, null);
    }

    @Test
    public void testProviderRegistry() {
        String packageName = mContext.getPackageName();
        String movieProviderClassName = MoviesContentProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, movieProviderClassName);

        try {
            PackageManager pm = mContext.getPackageManager();

            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            /* Make sure that the registered authority matches the authority from the Contract */
            String incorrectAuthority =
                    "Error: MoviesContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e) {
            String providerNotRegisteredAtAll =
                    "Error: MoviesContentProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);
        }
    }

    private static final Uri TEST_MOVIES = MoviesContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIES_WITH_ID = TEST_MOVIES.buildUpon().appendPath("1").build();

    @Test
    public void testUriMatcher() {

        UriMatcher testMatcher = MoviesContentProvider.buildUriMatcher();

        String moviesUriDoesNotMatch = "Error: The MOVIE URI was matched incorrectly.";
        int actualMovieMatchCode = testMatcher.match(TEST_MOVIES);
        int expectedMovieMatchCode = MoviesContentProvider.MOVIE;
        assertEquals(moviesUriDoesNotMatch,
                actualMovieMatchCode,
                expectedMovieMatchCode);

        String movieWithIdDoesNotMatch =
                "Error: The MOVIE_WITH_ID URI was matched incorrectly.";
        int actualMovieWithIdCode = testMatcher.match(TEST_MOVIES_WITH_ID);
        int expectedMovieWithIdCode = MoviesContentProvider.MOVIE_WITH_ID;
        assertEquals(movieWithIdDoesNotMatch,
                actualMovieWithIdCode,
                expectedMovieWithIdCode);
    }

    @Test
    public void testInsert() {

        /* Create values to insert */
        ContentValues testMovieValues = new ContentValues();
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Test title");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, "Test poster url");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RATING, 0.0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "Test synopsis");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 0.0);

        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (movie) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                MoviesContract.MovieEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                movieObserver);


        Uri uri = contentResolver.insert(MoviesContract.MovieEntry.CONTENT_URI, testMovieValues);

        Uri expectedUri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, 1);

        String insertProviderFailed = "Unable to insert item through Provider";
        assertEquals(insertProviderFailed, uri, expectedUri);

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        movieObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(movieObserver);
    }

    @Test
    public void testQuery() {

        /* Create values to insert */
        ContentValues testMovieValues = new ContentValues();
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Test title");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, "Test poster url");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RATING, 0.0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "Test synopsis");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 0.0);

        /* Insert ContentValues into database and get a row ID back */
        long movieRowId = database.insert(
                /* Table to insert values into */
                MoviesContract.MovieEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testMovieValues);

        String insertFailed = "Unable to insert directly into the database";
        assertTrue(insertFailed, movieRowId != -1);

        /* Perform the ContentProvider query */
        Cursor movieCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, movieCursor != null && movieCursor.moveToFirst());

        /* We are done with the cursor, close it now. */
        movieCursor.close();
    }

    @Test
    public void testDelete() {
        /* Create values to insert */
        ContentValues testMovieValues = new ContentValues();
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Test title");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, "Test poster url");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RATING, 0.0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "Test synopsis");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 0.0);

        /* Insert ContentValues into database and get a row ID back */
        long movieRowId = database.insert(
                /* Table to insert values into */
                MoviesContract.MovieEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testMovieValues);

        /* Always close the database when you're through with it */
        database.close();

        String insertFailed = "Unable to insert into the database";
        assertTrue(insertFailed, movieRowId != -1);


        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (movie) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                MoviesContract.MovieEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                movieObserver);



        /* The delete method deletes the previously inserted row with id = 1 */
        Uri uriToDelete = MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("1").build();
        int movieDeleted = contentResolver.delete(uriToDelete, null, null);

        String deleteFailed = "Unable to delete item in the database";
        assertTrue(deleteFailed, movieDeleted != 0);

        /*
         * If this fails, it's likely you didn't call notifyChange in your delete method from
         * your ContentProvider.
         */
        movieObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(movieObserver);
    }

    @Test
    public void testUpdate() {
        /* Create values to insert */
        ContentValues testMovieValues = new ContentValues();
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Test title");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, "Test poster url");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RATING, 0.0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, "Test synopsis");
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
        testMovieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 0.0);

        /* Insert ContentValues into database and get a row ID back */
        long movieRowId = database.insert(
                /* Table to insert values into */
                MoviesContract.MovieEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testMovieValues);

        /* Always close the database when you're through with it */
        database.close();

        String insertFailed = "Unable to insert into the database";
        assertTrue(insertFailed, movieRowId != -1);


        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (movie) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                MoviesContract.MovieEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                movieObserver);


        String titleUpdate = "Test title updated";

        ContentValues testMovieValuesUpdate = new ContentValues();
        testMovieValuesUpdate.put(MoviesContract.MovieEntry.COLUMN_TITLE, titleUpdate);

        /* The delete method deletes the previously inserted row with id = 1 */
        Uri uriToUpdate = MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("1").build();
        int movieUpdate = contentResolver.update(uriToUpdate, testMovieValuesUpdate, null, null);

        String deleteFailed = "Unable to delete item in the database";
        assertTrue(deleteFailed, movieUpdate != 0);

        String[] projection = new String[] {
                MoviesContract.MovieEntry.COLUMN_TITLE
        };

        Cursor movieCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                projection,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, movieCursor != null && movieCursor.moveToFirst());

        String titleAfterUpdate = movieCursor.getString(0);

        String messageUpdate = "Title after update is not the same";

        assertEquals(messageUpdate, titleUpdate, titleAfterUpdate);

        /*
         * If this fails, it's likely you didn't call notifyChange in your delete method from
         * your ContentProvider.
         */
        movieObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(movieObserver);
    }

    @After
    public void tearDown() {
        database.close();
    }

}