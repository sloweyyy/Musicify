package com.example.musicapp.model;

public class Song {
    private String title;

    private boolean isLiked;

    private String id;
    private String artist;
    private String imageUrl;

    public Song(String title, String artist, String id) {
        this.title = title;
        this.artist = artist;
        this.id = id;
        this.isLiked = false;
    }

    public Song(String title, String artist, String imageUrl, String id) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.id = id;
        this.isLiked = false;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getId() {
        return id;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public static Song fromSimplifiedTrack(SimplifiedTrack track) {
        String artistName = "";
        String imageUrl = "";
        String id = "";

        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            artistName = track.getArtists().get(0).getName();
        }

        if (track.getAlbum() != null && track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
            imageUrl = track.getAlbum().getImages().get(0).getUrl();
        }

        if (track.getId() != null) {
            id = track.getId();
        }

        return new Song(track.getName(), artistName, imageUrl, id);
    }
}
