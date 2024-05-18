package com.example.musicapp.fragment;

import android.util.Log;
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
import com.example.musicapp.model.Song;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.musicapp.adapter.FetchAccessToken;

public class LikedAlbumDetailFragment extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList;
    private Button backButton;
    private View view;
    private FetchAccessToken fetchAccessToken;

    public LikedAlbumDetailFragment () {}

    private static final String ARG_ALBUM_NAME = "albumName";
    private static final String ARG_ALBUM_ARTISTNAME = "albumAristName";
    private static final String ARG_ALBUM_THUMBNAIL = "albumThumbnail";

    private String mAlbumName;
    private String mAlbumAristName;
    private int mAlbumThumbnail;

    public static LikedAlbumDetailFragment newInstance(String albumName, int albumThumbnail, String albumAristName) {
        LikedAlbumDetailFragment fragment = new LikedAlbumDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM_NAME, albumName);
        args.putInt(ARG_ALBUM_THUMBNAIL, albumThumbnail);
        args.putString(ARG_ALBUM_ARTISTNAME, albumAristName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_detail, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();

        if (getArguments() != null) {
            mAlbumName = getArguments().getString(ARG_ALBUM_NAME);
            mAlbumAristName = getArguments().getString(ARG_ALBUM_ARTISTNAME);
            mAlbumThumbnail = getArguments().getInt(ARG_ALBUM_THUMBNAIL);
        }
        ImageView thumbnailImageView = view.findViewById(R.id.albumBanner);
        TextView nameTextView = view.findViewById(R.id.albumName);
        TextView artistNameTextView = view.findViewById(R.id.albumArtistName);
        thumbnailImageView.setImageResource(mAlbumThumbnail);
        nameTextView.setText(mAlbumName);
        artistNameTextView.setText(mAlbumAristName);

        recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView();
        loadSongsFromSpotify();
    }

    private void setupBackButton() {
        backButton = getView().findViewById(R.id.iconBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setupRecyclerView() {
        songList = new ArrayList<>();
        adapter = new SongAdapter(getContext(), songList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadSongsFromSpotify() {
        // Call Spotify API here to fetch songs and populate songList
        // For example:

    }

//    @Override
//    public void onTokenReceived(String accessToken) {
//        getAlbum(accessToken);
//    }
//
//    private void getAlbum (String accessToken){
//        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();
//
//        PlaySongFragment.SpotifyApi apiService = retrofit.create(PlaySongFragment.SpotifyApi.class);
//        String authorization = "Bearer " + accessToken;
//
//        Call<PlaySongFragment.TrackModel> call = apiService.getTrack(authorization, songId);
//        call.enqueue(new Callback<PlaySongFragment.TrackModel>() {
//            @Override
//            public void onResponse(@NonNull Call<PlaySongFragment.TrackModel> call, @NonNull Response<PlaySongFragment.TrackModel> response) {
//                if (response.isSuccessful()) {
//                    PlaySongFragment.TrackModel track = response.body();
//                    if (track != null) {
//                        setupTrack(track);
//                    }
//                } else {
//                    showError(response);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<PlaySongFragment.TrackModel> call, Throwable throwable) {
//                Log.e("Error fetching track", throwable.getMessage());
//            }
//        });
//    }
//    public void setupTrack(PlaySongFragment.TrackModel track) {
//        String songName = track.getName();
//        String artistName = track.artists.get(0).getName();
//        String imageUrl = track.album.images.get(0).getUrl();
//        String playUrl = track.getPreview_url();
//
//        songname.setText(songName);
//        artistname.setText(artistName);
//        Glide.with(getActivity()).load(imageUrl).into(cover_art);
//
////        setupMediaPlayer(playUrl);
//        if (spotifyAppRemote != null) {
//            spotifyAppRemote.getPlayerApi().play("spotify:track:7ouMYWpwJ422jRcDASZB7P");
//        }
//        setupSeekBar();
//        setupPauseButton();
//    }
}


