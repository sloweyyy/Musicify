package com.example.musicapp.model;

public class Song {
    private String title;
    private String artist;
    private String imageUrl;

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public Song(String title, String artist, String imageUrl) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public static Song fromSimplifiedTrack(SimplifiedTrack track) {
        String artistName = "";
        String imageUrl = "";

        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            artistName = track.getArtists().get(0).getName();
        }

        if (track.getAlbum() != null && track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
            imageUrl = track.getAlbum().getImages().get(0).getUrl();
        }

        return new Song(track.getName(), artistName, imageUrl);
    }
}
