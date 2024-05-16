package com.example.musicapp.model;

import com.example.musicapp.fragment.List_Playlist;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistAPI {
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
        private String url;

        public String getUrl() {
            return url;
        }
    }

    @SerializedName("tracks")
    public tracksModel tracks;

    public static class tracksModel {
        @SerializedName("total")
        private String total;

        public String getTotal() {
            return total;
        }
    }
}
