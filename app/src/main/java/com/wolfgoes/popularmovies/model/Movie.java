package com.wolfgoes.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

//TODO: check if Parcelable should be used
//TODO: check if GSON should be used @Expose @SerializedName
public class Movie implements Parcelable{

    private long mId;
    private String mTitle;
    //TODO: change it to date
    private String mReleaseDate;
    private String mPosterPath;
    private String mBackdropPath;
    private double mVoteAverage;
    private String mSynopsis;

    public Movie () { }

    @Override
    public String toString() {
        return mTitle;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
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

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        mBackdropPath = backdropPath;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.mVoteAverage = voteAverage;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public void setSynopsis(String overview) {
        this.mSynopsis = overview;
    }

    // Parcelling part
    public Movie(Parcel in){
        mId = in.readLong();
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mPosterPath = in.readString();
        mBackdropPath = in.readString();
        mVoteAverage = in.readDouble();
        mSynopsis = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
        dest.writeString(mPosterPath);
        dest.writeString(mBackdropPath);
        dest.writeDouble(mVoteAverage);
        dest.writeString(mSynopsis);
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
