package com.example.musicapp.model;

import com.example.musicapp.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Playlist {
    private String id;
    private String userId;
    private String name;
    private String description;
    private int thumbnail;
    private String imageURL;
    private List<String> songs;

    private Date createdAt;
    private String privacy;

    private int privacyIcon;
    private int songCount;

    public Playlist() {

    }

    public Playlist(String id, String userId, String name, String description, String imageURL) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;

    }


    public Playlist(String userId, String name, String description, String imageURL) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
    }

    public Playlist(String userId, String name, String imageURL, Date createdAt) {
        this.userId = userId;
        this.name = name;
        this.imageURL = imageURL;
        this.createdAt = createdAt;
    }


    public List<String> getSongs() {
        if (songs == null) {
            songs = new ArrayList<>(); // Initialize if null
        }
        return songs;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getSongCount() {
        return 0;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public int getImageResource() {
        return thumbnail;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getPrivacyIcon() {
        if ("Private".equals(privacy)) {
            return R.drawable.ic_private;
        } else {
            return R.drawable.ic_public;
        }
    }

    public void setPrivacyIcon(int privacyIcon) {
        this.privacyIcon = privacyIcon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }


}
