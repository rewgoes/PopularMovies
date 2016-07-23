package com.wolfgoes.popularmovies.data;

/**
 * Created by rafael on 23/07/2016.
 */

//TODO: check if Parcelable should be used
//TODO: check if GSON should be used @Expose @SerializedName
public class Movie {

    private String title;
    //TODO: change it to date
    private String releaseDate;
    private String posterPath;
    private double voteAverage;
    private String overview;

    @Override
    public String toString() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

}
