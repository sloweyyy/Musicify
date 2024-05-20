package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Artist {
    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    @SerializedName("images")
    private List<Image> images;

    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public List<Image> getImages() {
        return images;
    }


}