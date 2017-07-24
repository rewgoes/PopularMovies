package com.wolfgoes.popularmovies.api;

import com.google.gson.annotations.SerializedName;
import com.wolfgoes.popularmovies.model.Movie;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApi {

    @GET("movie/{sortBy}")
    Call<MovieResult> loadMovies(@Path("sortBy") String sortBy, @Query("language") String language, @Query("page") int page);

    class MovieResult {
        @SerializedName("results")
        private ArrayList<Movie> mMovies;

        public ArrayList<Movie> getMovies() {
            return mMovies;
        }
    }

}
