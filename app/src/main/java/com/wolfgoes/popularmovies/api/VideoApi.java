package com.wolfgoes.popularmovies.api;

import com.google.gson.annotations.SerializedName;
import com.wolfgoes.popularmovies.model.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface VideoApi {

    @GET("movie/{id}/videos")
    Call<VideoResult> loadVideos(@Path("id") long id);

    class VideoResult {
        @SerializedName("results")
        private List<Video> mVideos;

        public List<Video> getVideos() {
            return mVideos;
        }
    }

}
