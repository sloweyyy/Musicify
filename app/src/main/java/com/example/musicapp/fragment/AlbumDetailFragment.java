package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
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
import com.example.musicapp.model.AlbumSimplified;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import com.example.musicapp.adapter.FetchAccessToken;

public class AlbumDetailFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList;
    private AlbumSimplified albumSimplified;
    private Button backButton;
    private View view;
    private String albumId;
    private String accessToken;
    private FetchAccessToken fetchAccessToken;
    private TextView albumName;

    private ImageView imageView;

    public AlbumDetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_detail, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        albumName = view.findViewById(R.id.albumName);
        imageView = view.findViewById(R.id.albumBanner);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            albumId = getArguments().getString("albumId");
        }
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void getSongs(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(AlbumDetailFragment.SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<AlbumSimplified> call = apiService.getSongs(authorization, albumId);
        call.enqueue(new Callback<AlbumSimplified>() {

            @Override
            public void onResponse(Call<AlbumSimplified> call, Response<AlbumSimplified> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    AlbumSimplified albumSimplified = response.body();
                    albumName.setText(albumSimplified.getName());
                    Glide.with(requireContext()).load(albumSimplified.getImages().get(0).getUrl()).into(imageView);
                    List<Song> songs = new ArrayList<>();
                    for (SimplifiedTrack simplifiedTrack : albumSimplified.getTracksContainer().tracks) {
                        songs.add(Song.fromSimplifiedTrack(simplifiedTrack));
                    }
                    songAdapter = new SongAdapter(getContext(), songs);
                    recyclerView.setAdapter(songAdapter);
                    recyclerView.setVisibility(View.VISIBLE);

                } else {
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.message());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<AlbumSimplified> call, Throwable throwable) {

            }
        });

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
    }

    private void setupBackButton() {
        backButton = getView().findViewById(R.id.iconBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    public interface SpotifyApiService {
        @GET("v1/albums/{albumId}")
        Call<AlbumSimplified> getSongs(@Header("Authorization") String authorization, @Path("albumId") String albumId);
    }

    private void setupRecyclerView() {
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
    }
    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        getSongs(accessToken);
    }
}
