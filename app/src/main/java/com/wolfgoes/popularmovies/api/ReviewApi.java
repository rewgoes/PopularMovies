package com.wolfgoes.popularmovies.api;

import com.google.gson.annotations.SerializedName;
import com.wolfgoes.popularmovies.model.Review;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ReviewApi {

    @GET("movie/{id}/reviews")
    Call<ReviewResult> loadReviews(@Path("id") long id);

    class ReviewResult {
        @SerializedName("results")
        private List<Review> mReviews;

        public List<Review> getReviews() {
            return mReviews;
        }
    }

}
