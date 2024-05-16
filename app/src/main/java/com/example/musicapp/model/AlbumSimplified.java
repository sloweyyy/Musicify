package com.example.musicapp.model;

import com.example.musicapp.model.Image;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlbumSimplified {
    @SerializedName("images")
    private List<Image> images;

    // ... other fields and getters ...

    public List<Image> getImages() {
        return images;
    }
}