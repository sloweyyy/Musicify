package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Artist {
    @SerializedName("name")
    private String name;
    @SerializedName("id")
    private String id;

    @SerializedName("followers")
    private Followers followers;

    @SerializedName("images")
    private List<Image> images;
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public Followers getFollowers() {
        return followers;
    }

    public List<Image> getImages() {
        return images;
    }
    public static class Followers {
        @SerializedName("total")
        private int total;

        public int getTotal() {
            return total;
        }
    }

    public static class Image {
        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }

}