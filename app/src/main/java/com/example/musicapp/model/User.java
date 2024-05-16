package com.example.musicapp.model;


import java.util.ArrayList;

public class User {
    private String Name;
    private String Email;
    private String Password;
    private String Phone;
    private String Image;
    private String Bio;
    private String Following;
    private ArrayList likedAlbums;
    public ArrayList getLikedAlbums() {
        return likedAlbums;
    }

}
