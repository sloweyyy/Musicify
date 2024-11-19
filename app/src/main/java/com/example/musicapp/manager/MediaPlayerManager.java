package com.example.musicapp.manager;

import android.media.MediaPlayer;
import java.io.IOException;

public class MediaPlayerManager {
    private static MediaPlayerManager instance;

    private static boolean isPlaying;
    private static boolean isRepeat;
    private static boolean isShuffle;
    private MediaPlayer mediaPlayer;
    private int currentPosition;
    private int lastPlaybackPosition = 0;

    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
    }

    public static MediaPlayerManager getInstance() {
        if (instance == null) {
            isRepeat = false;
            isShuffle = false;
            isPlaying = true;
            instance = new MediaPlayerManager();
        }
        return instance;
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

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean sth) {
        isPlaying = sth;
    }

    public boolean getIsRepeat() {return isRepeat;}

    public void setIsRepeat(boolean sth) {isRepeat = sth;}

    public boolean getIsShuffle() {
        return isShuffle;
    }

    public void setIsShuffle(boolean sth) {isShuffle = sth;}

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
