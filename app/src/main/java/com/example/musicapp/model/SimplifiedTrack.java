package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SimplifiedTrack {
    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    @SerializedName("artists")
    private List<Artist> artists;
    @SerializedName("album")
    private AlbumSimplified album;

    public String getName() {
        return name;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public AlbumSimplified getAlbum() {
        return album;
    }

    public String getId() {
        return id;
    }
}