package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResult {
  @SerializedName("tracks")
  private Tracks tracks;

  public Tracks getTracks() {
    return tracks;
  }

  public static class Tracks {
    @SerializedName("items")
    private List<SimplifiedTrack> items;

    public List<SimplifiedTrack> getItems() {
      return items;
    }
  }
}
