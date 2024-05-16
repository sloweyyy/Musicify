package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistSimplified {
    @SerializedName("description")
    private String description;

    public String getDescription() {
        return description;
    }

    @SerializedName("id")
    private String id;

    public String getId() {
        return id;
    }

    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    @SerializedName("images")
    public List<imageModel> images;

    public static class imageModel{
        @SerializedName("url")
        public String url;

        public String getUrl() {
            return url;
        }
    }

    @SerializedName("tracks")
    public TracksModel tracksContainer;

    public static class TracksModel {
        @SerializedName("items")
        public List<SimplifiedTrack> tracks;
        @SerializedName("href")
        public String href;

    }
}
