package com.example.musicapp.model;

import com.example.musicapp.model.Image;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlbumSimplified {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("artists")
    private List<Artist> artists;
    @SerializedName("images")
    private List<Image> images;

    // ... other fields and getters ...
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Artist> getArtist() {
        return artists;
    }

    public List<Image> getImages() {
        return images;
    }
}