package com.example.musicapp.model;

public class Song {
    private String title;
    private String artist;
    // You can add more properties here like thumbnail, duration, etc.

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
