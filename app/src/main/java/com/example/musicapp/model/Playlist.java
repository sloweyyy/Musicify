package com.example.musicapp.model;

import com.example.musicapp.R;

public class Playlist {
    private String id;
    private String userId;
    private String name;
    private String description;
    private int thumbnail;

    private String privacy;

    private int privacyIcon;

    public Playlist() {

    }

    public Playlist(String userId, String name, String description, String privacy, int thumbnail) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.privacy = privacy;
        this.thumbnail = thumbnail;
        if (privacy.equals("Private")) {
            setPrivacyIcon(R.drawable.ic_private);
        } else {
            setPrivacyIcon(R.drawable.ic_public);
        }
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public int getSongCount() {
        return 0;
    }

    public int getImageResource() {
        return thumbnail;
    }

    public void setPrivacyIcon(int privacyIcon) {
        this.privacyIcon = privacyIcon;
    }

    public int getPrivacyIcon() {
        return privacyIcon;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }


}
