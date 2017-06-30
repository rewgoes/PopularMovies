package com.wolfgoes.popularmovies.utils;

import android.text.TextUtils;

/**
 * Created by rafael on 23/07/2016.
 */
public final class Utility {

    final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";

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

}
