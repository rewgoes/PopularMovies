package com.wolfgoes.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rafael on 23/07/2016.
 */

//TODO: check if Parcelable should be used
//TODO: check if GSON should be used @Expose @SerializedName
public class Movie implements Parcelable{

    private String mTitle;
    //TODO: change it to date
    private String mReleaseDate;
    private String mPosterPath;
    private double mVoteAverage;
    private String mOverview;

    public Movie () { }

    @Override
    public String toString() {
        return mTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.mReleaseDate = releaseDate;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        this.mPosterPath = posterPath;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.mVoteAverage = voteAverage;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        this.mOverview = overview;
    }

    // Parcelling part
    public Movie(Parcel in){
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mPosterPath = in.readString();
        mVoteAverage = in.readDouble();
        mOverview = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
        dest.writeString(mPosterPath);
        dest.writeDouble(mVoteAverage);
        dest.writeString(mOverview);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
