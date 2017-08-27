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
        @SerializedName("page")
        private int mPage;

        @SerializedName("total_pages")
        private int mTotalPages;

        @SerializedName("results")
        private ArrayList<Movie> mMovies;

        public int getTotalPages() {
            return mTotalPages;
        }

        public ArrayList<Movie> getMovies() {
            return mMovies;
        }

        public int getPage() {
            return mPage;
        }
    }

}
