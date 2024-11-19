package com.example.musicapp.model;

import com.example.musicapp.fragment.List_Playlist;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistAPI {
  @SerializedName("images")
  public List<imageModel> images;
  @SerializedName("tracks")
  public tracksModel tracks;
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
    private String url;

    public String getUrl() {
      return url;
    }
  }

  public static class tracksModel {
    @SerializedName("total")
    private String total;

    public String getTotal() {
      return total;
    }
  }
}
