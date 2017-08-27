package com.wolfgoes.popularmovies.model;

import com.google.gson.annotations.SerializedName;

public class Review {

    public static final String JKEY_REVIEW_LIST = "results";

    @SerializedName("id")
    private String mId;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    @SerializedName("author")
    private String mAuthor;
    @SerializedName("content")
    private String mContent;

}
