package com.example.musicapp.model;

import android.util.Log;
import java.util.List;

public class Song {
    private String title;

    private boolean isLiked;

    private final String id;
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

    public static Song fromSimplifiedTrack(SimplifiedTrack track) {
        String artistName = "";
        String imageUrl = "";
        String id = "";

        if (track == null) {
            Log.e("Song", "SimplifiedTrack object is null!");
            return new Song("Unknown Title", "Unknown Artist", "default_song_id");
        }

        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
            List<Artist> artists = track.getArtists();
            StringBuilder artistNames = new StringBuilder();
            for (int i = 0; i < artists.size(); i++) {
                Artist artist = artists.get(i);
                artistNames.append(artist.getName());
                if (i < artists.size() - 1) {
                    artistNames.append(", ");
                }
            }
            artistName= artistNames.toString();
        }

        if (track.getAlbum() != null && track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
            imageUrl = track.getAlbum().getImages().get(0).getUrl();
        }

        if (track.getId() != null) {
            id = track.getId();
        }

        return new Song(track.getName(), artistName, imageUrl, id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
