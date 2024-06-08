package com.example.musicapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.PlaylistSimplified;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.service.SpotifyApiService;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class PlaylistDetailAPIViewModel extends AndroidViewModel {
    private MutableLiveData<PlaylistDetail> _playlistDetail = new MutableLiveData<>();
    public LiveData<PlaylistDetail> playlistDetail = _playlistDetail;

    public PlaylistDetailAPIViewModel(@NonNull Application application) {
        super(application);
    }

    public void fetchPlaylistDetails(String accessToken, String playlistId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<PlaylistSimplified> call = apiService.getSongs(authorization, playlistId);
        call.enqueue(new Callback<PlaylistSimplified>() {
            @Override
            public void onResponse(Call<PlaylistSimplified> call, Response<PlaylistSimplified> response) {
                if (response.isSuccessful()) {
                    PlaylistSimplified playlistSimplified = response.body();
                    if (playlistSimplified != null) {
                        PlaylistDetail playlistDetail = new PlaylistDetail(
                                playlistSimplified.getName(),
                                playlistSimplified.getDescription(),
                                playlistSimplified.images.get(0).getUrl(),
                                getSongsFromPlaylist(playlistSimplified)
                        );
                        _playlistDetail.setValue(playlistDetail);
                    }
                } else {
                    // Handle error
                }
            }

            @Override
            public void onFailure(Call<PlaylistSimplified> call, Throwable throwable) {
                // Handle failure
            }
        });
    }

    private List<Song> getSongsFromPlaylist(PlaylistSimplified playlist) {
        List<Song> songs = new ArrayList<>();
        for (PlaylistDetailAPI.ItemModel item : playlist.tracksContainer.tracks) {
            SimplifiedTrack track = item.track;
            if (track != null) {
                songs.add(Song.fromSimplifiedTrack(track));
            }
        }
        return songs;
    }


    public static class PlaylistDetail {
        private String name;
        private String description;
        private String imageUrl;
        private List<Song> songs;

        public PlaylistDetail(String name, String description, String imageUrl, List<Song> songs) {
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
            this.songs = songs;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public List<Song> getSongs() {
            return songs;
        }
    }
    public class PlaylistSimplified {
        @SerializedName("description")
        private String description;

        public String getDescription() {
            return description;
        }

        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        @SerializedName("images")
        public List<PlaylistDetailAPI.PlaylistSimplified.imageModel> images;

        public class imageModel {
            @SerializedName("url")
            public String url;

            public String getUrl() {
                return url;
            }
        }

        @SerializedName("tracks")
        public PlaylistDetailAPI.TracksModel tracksContainer;

    }

    public class TracksModel {
        @SerializedName("items")
        public List<PlaylistDetailAPI.ItemModel> tracks;
    }

    public class ItemModel {
        @SerializedName("track")
        public SimplifiedTrack track;
    }
}