package com.example.musicapp.model;

import com.example.musicapp.R;
public class Album {
    private String id;
    private String userId;
    private String name;
    private String artistName;
    private int thumbnail;

    public Album() {}

    public Album(String userId, String name, String artistName, int thumbnail) {
        this.userId = userId;
        this.name = name;
        this.artistName = artistName;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getThumbnail() {
        return thumbnail;
    }



    public int getImageResource() {
        return thumbnail;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}



