package com.wolfgoes.popularmovies.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.wolfgoes.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_VIDEO = "video";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_RELEASE = "release";

        public static final String COLUMN_RATING = "rating";

        public static final String COLUMN_SYNOPSIS = "synopsis";

        public static final String COLUMN_POSTER_URL = "poster_url";

        public static final String COLUMN_BACKDROP_URL = "backdrop_url";

        public static Uri buildMovieWithIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEW)
                .build();

        public static final String TABLE_NAME = "review";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "release";

        public static final String COLUMN_MOVIE_ID = MovieEntry.TABLE_NAME + MovieEntry._ID;

        public static Uri buildReviewsFromMovieIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class VideoEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_VIDEO)
                .build();

        public static final String TABLE_NAME = "video";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_SITE = "site";

        public static final String COLUMN_KEY = "key";

        public static final String COLUMN_MOVIE_ID = MovieEntry.TABLE_NAME + MovieEntry._ID;

        public static Uri buildVideosFromMovieIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
