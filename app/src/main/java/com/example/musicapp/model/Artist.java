package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

public class Artist {
    @SerializedName("name")
    private String name;
    private String imageUrl;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}