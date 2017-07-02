package com.wolfgoes.popularmovies.model;

import com.google.gson.annotations.SerializedName;

public class Review {

    public static final String JKEY_REVIEW_LIST = "results";

    @SerializedName("id")
    private String mId;
    @SerializedName("author")
    private String mAuthor;
    @SerializedName("content")
    private String mContent;

}
