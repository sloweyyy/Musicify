package com.example.musicapp.manager;

import android.media.MediaPlayer;

import java.io.IOException;

public class MediaPlayerManager {
    private static MediaPlayerManager instance;

    private static boolean isPlaying;
    private MediaPlayer mediaPlayer;

    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
    }

    public boolean getIsPlaying(){
        return isPlaying;
    }
    public void setIsPlaying(boolean sth){
        isPlaying = sth;
    }



    public static MediaPlayerManager getInstance() {
        if (instance == null) {
            instance = new MediaPlayerManager();
        }
        return instance;
    }

    public void setMediaSource(String previewUrl) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(previewUrl);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
