package com.example.musicapp.model;

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

    @SerializedName("tracks")
    private Tracks tracksContainer;

    // No need to create a separate Track class if it's only used for deserialization
    public static class Tracks {
        @SerializedName("items")
        public List<SimplifiedTrack> tracks;

        public List<SimplifiedTrack> getTracks() {
            return tracks;
        }
    }

    // Getters
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

    public Tracks getTracksContainer() {
        return tracksContainer;
    }
}