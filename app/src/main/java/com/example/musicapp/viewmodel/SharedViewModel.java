package com.example.musicapp.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.musicapp.model.Song;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private List<Song> songList;

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }
}