package com.example.musicapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.Song;
import com.example.musicapp.viewmodel.AlbumDetailViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private RecyclerView recyclerView;
    private AlbumDetailViewModel viewModel;
    private SongAdapter songAdapter;
    private List<Song> songList;
    private AlbumSimplified albumSimplified;
    private Button backButton;
    private View view;
    private String albumId;
    private String accessToken;
    private FetchAccessToken fetchAccessToken;
    private TextView albumName;

    private TextView albumArtist;
    private ImageView imageView;
    HomeFragment homeFragment;

    public AlbumDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_detail, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        viewModel = new ViewModelProvider(this).get(AlbumDetailViewModel.class);
        albumName = view.findViewById(R.id.albumName);
        albumArtist = view.findViewById(R.id.albumArtistName);
        imageView = view.findViewById(R.id.albumBanner);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            albumId = getArguments().getString("albumId");
        }
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        homeFragment = new HomeFragment();
        return view;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
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

    private void setupRecyclerView() {
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        viewModel.getAlbumSongs(accessToken, albumId);

        viewModel.albumSongs.observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                songAdapter = new SongAdapter(getContext(), songs, new SongAdapter.OnSongSelectedListener() {
                    @Override
                    public void onSongSelected(Song song) {
                        // Handle song selection
                    }
                });
                recyclerView.setAdapter(songAdapter);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.albumDetails.observe(getViewLifecycleOwner(), albumSimplified -> {
            if (albumSimplified != null) {
                albumName.setText(albumSimplified.getName());
                albumArtist.setText(albumSimplified.getArtists().get(0).getName());
                Glide.with(requireContext())
                        .load(albumSimplified.getImages().get(0).getUrl())
                        .into(imageView);
            }
        });
    }
}
