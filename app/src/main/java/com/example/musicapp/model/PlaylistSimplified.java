package com.example.musicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistSimplified {
  @SerializedName("images")
  public List<imageModel> images;
  @SerializedName("tracks")
  public TracksModel tracksContainer;
  @SerializedName("description")
  private String description;
  @SerializedName("id")
  private String id;
  @SerializedName("name")
  private String name;

  public String getDescription() {
    return description;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public static class imageModel {
    @SerializedName("url")
    public String url;

    public String getUrl() {
      return url;
    }
  }

  public static class TracksModel {
    @SerializedName("items")
    public List<ItemModel> tracks;

    public static class ItemModel {
      @SerializedName("track")
      public SimplifiedTrack track;
    }
  }
}
