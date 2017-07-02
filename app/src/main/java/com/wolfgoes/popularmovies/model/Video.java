package com.wolfgoes.popularmovies.model;

import com.google.gson.annotations.SerializedName;

public class Video {

    public static final String JKEY_VIDEO_LIST = "results";

    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("site")
    private String mSite;
    @SerializedName("key")
    private String mKey;

}
