package com.example.musicapp.manager;

import android.media.MediaPlayer;

import java.io.IOException;

public class MediaPlayerManager {
    private static MediaPlayerManager instance;

    private static boolean isPlaying;
    private MediaPlayer mediaPlayer;
    private int currentPosition;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean sth) {
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

    public void stopAndRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
