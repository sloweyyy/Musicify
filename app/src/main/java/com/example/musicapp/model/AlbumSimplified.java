package com.example.musicapp.model;

import com.example.musicapp.model.Image;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlbumSimplified {
    @SerializedName("images")
    private List<Image> images;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("artists")
    private List<Artist> artists;

    public String getName() {
        return name;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public String getId() {
        return id;
    }

    public List<Image> getImages() {
        return images;
    }
}