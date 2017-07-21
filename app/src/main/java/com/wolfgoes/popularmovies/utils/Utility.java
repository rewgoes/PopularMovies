package com.wolfgoes.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.wolfgoes.popularmovies.R;

public final class Utility {

    final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    final static String VIDEO_BASE_URL = "http://img.youtube.com/vi/%s/mqdefault.jpg";

    //Create a non instantiable class
    //http://stackoverflow.com/questions/8848107/how-to-construct-a-non-instantiable-and-non-inheritable-class-in-java
    private Utility() {
        throw new RuntimeException("No not try to instantiate this");
    }

    public static String getPosterUrlForMovie(String posterName, String size) {

        //TODO: check size possibilities later
        if (TextUtils.isEmpty(size)) {
            size = "w185";
        }

        return POSTER_BASE_URL + size + posterName;
    }

    public static String getVideoThumbnail(String posterName) {
        return String.format(VIDEO_BASE_URL, posterName);
    }

    public static String getOrderPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_order_key), context.getString(R.string.pref_order_popular));
    }

}
