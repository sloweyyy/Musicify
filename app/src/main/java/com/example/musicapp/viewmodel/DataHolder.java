package com.example.musicapp.viewmodel;

import com.example.musicapp.model.Song;

import java.util.List;

public class DataHolder {
    private static DataHolder instance;
    private String previewUrl;
    private int currentPosition;
    private int lastPlaybackPosition = 0;
    private List<Song> songList;

    public static void setInstance(DataHolder instance) {
        DataHolder.instance = instance;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    public int getLastPlaybackPosition() {
        return lastPlaybackPosition;
    }

    public void setLastPlaybackPosition(int position) {
        this.lastPlaybackPosition = position;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    private DataHolder() {
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }
}
