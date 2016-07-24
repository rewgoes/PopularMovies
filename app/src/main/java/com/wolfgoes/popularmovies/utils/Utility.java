package com.wolfgoes.popularmovies.utils;

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

    //from Start Gliding video
    public static String getPosterUrlForMovie(String posterName) {

        //TODO: check size possibilities later
        String size = "w185";

        return POSTER_BASE_URL + size + posterName;
    }

}
