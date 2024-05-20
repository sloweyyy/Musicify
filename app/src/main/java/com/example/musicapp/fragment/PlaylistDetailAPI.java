package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
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

public class PlaylistDetailAPI extends Fragment implements FetchAccessToken.AccessTokenCallback {
    View view;
    RecyclerView recyclerView;
    private FetchAccessToken fetchAccessToken;
    private PlaylistSimplified playlistSimplified;
    private String playlistId;
    private TextView playlistName;
    private TextView playlistDescription;
    private ImageView imageView;

    private SongAdapter songAdapter;


    @Override
    public void onTokenReceived(String accessToken) {
        getSongs(accessToken);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewSongDetail);
        playlistName = view.findViewById(R.id.playlistName);
        playlistDescription = view.findViewById(R.id.playlistDescription);
        imageView = view.findViewById(R.id.playlistBanner);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            playlistId = getArguments().getString("playlistId");
        }
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public void getSongs(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(PlaylistDetailAPI.SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<PlaylistSimplified> call = apiService.getSongs(authorization, playlistId);
        call.enqueue(new Callback<PlaylistSimplified>() {
            @Override
            public void onResponse(Call<PlaylistSimplified> call, Response<PlaylistSimplified> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    PlaylistSimplified playlist = response.body();
                    playlistName.setText(playlist.getName());
                    playlistDescription.setText(playlist.getDescription());
                    Glide.with(requireContext()).load(playlist.images.get(0).getUrl()).into(imageView);
                    List<Song> songs = new ArrayList<>();
                    for (ItemModel item : playlist.tracksContainer.tracks) {
                        SimplifiedTrack track = item.track;
                        songs.add(Song.fromSimplifiedTrack(track));
                    }
                    songAdapter = new SongAdapter(getContext(), songs);
                    recyclerView.setAdapter(songAdapter);
                    recyclerView.setVisibility(View.VISIBLE);

                } else {
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.body().getName());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<PlaylistSimplified> call, Throwable throwable) {

            }
        });

    }

    public interface SpotifyApiService {
        @GET("v1/playlists/{playlistId}")
        Call<PlaylistSimplified> getSongs(@Header("Authorization") String authorization, @Path("playlistId") String playlistId);

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
        public List<imageModel> images;

        public class imageModel {
            @SerializedName("url")
            public String url;

            public String getUrl() {
                return url;
            }
        }

        @SerializedName("tracks")
        public TracksModel tracksContainer;

    }

    public class TracksModel {
        @SerializedName("items")
        public List<ItemModel> tracks;
    }

    public class ItemModel {
        @SerializedName("track")
        public SimplifiedTrack track;
    }

}
