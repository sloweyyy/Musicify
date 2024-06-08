package com.example.musicapp.model;

public class UserProfile {
    private String name;
    private String email;
    private String backgroundImageUrl;
    private String avatarUrl;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
