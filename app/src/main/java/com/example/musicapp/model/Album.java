package com.example.musicapp.model;

import com.example.musicapp.R;
public class Album {
    private String id;
    private String name;
    private String artistName;
    private int thumbnail;

    public Album() {}

    public Album(String name, String artistName, int thumbnail) {
        this.name = name;
        this.artistName = artistName;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
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



