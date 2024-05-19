package com.example.musicapp.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

public class AlbumSimplified {
    @SerializedName("images")
    private List<Image> images;

    @SerializedName("id")
    private String id;

//    private LocalDateTime timeCreate;
//
//    private Map<AlbumSimplified, LocalDateTime> likedAlbums ;


    @SerializedName("name")
    private String name;

    @SerializedName("artists")
    private List<Artist> artists;

    @SerializedName("tracks")
    private Tracks tracksContainer;

    public class Tracks {
        @SerializedName("items")
        public List<SimplifiedTrack> tracks;
        public List<SimplifiedTrack> getTrack() {return tracks;}
    }

    public String getName() {
        return name;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public String getId() {
        return id;
    }

//    public LocalDateTime getTimeCreate() {
//        return timeCreate;
//    }

//    public Map<AlbumSimplified, LocalDateTime> getLikedAlbums() {
//        return likedAlbums;
//    }

    public List<Image> getImages() {
        return images;
    }
    public Tracks getTracksContainer(){return tracksContainer;}


}