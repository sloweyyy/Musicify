package com.example.musicapp.fragment;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList;
    private Button backButton;


    public PlaylistDetailFragment() {
        // Required empty public constructor
    }

    private static final String ARG_PLAYLIST_NAME = "playlistName";
    private static final String ARG_PLAYLIST_DESCRIPTION = "playlistDescription";
    private static final String ARG_PLAYLIST_THUMBNAIL = "playlistThumbnail";

    private String mPlaylistName;
    private String mPlaylistDescription;
    private int mPlaylistThumbnail;

    public static PlaylistDetailFragment newInstance(String playlistName, int playlistThumbnail, String playlistDescription) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYLIST_NAME, playlistName);
        args.putInt(ARG_PLAYLIST_THUMBNAIL, playlistThumbnail);
        args.putString(ARG_PLAYLIST_DESCRIPTION, playlistDescription);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_playlist_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();


        if (getArguments() != null) {
            mPlaylistName = getArguments().getString(ARG_PLAYLIST_NAME);
            mPlaylistDescription = getArguments().getString(ARG_PLAYLIST_DESCRIPTION);
            mPlaylistThumbnail = getArguments().getInt(ARG_PLAYLIST_THUMBNAIL);
        }
        ImageView thumbnailImageView = view.findViewById(R.id.playlistBanner);
        TextView nameTextView = view.findViewById(R.id.playlistName);
        TextView descriptionTextView = view.findViewById(R.id.playlistDescription);
        thumbnailImageView.setImageResource(mPlaylistThumbnail);
        nameTextView.setText(mPlaylistName);
        descriptionTextView.setText(mPlaylistDescription);


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
        songList.add(new Song("Song 1", "Artist 1"));
        songList.add(new Song("Song 2", "Artist 2"));
        songList.add(new Song("Song 3", "Artist 3"));
        adapter.notifyDataSetChanged();
    }

}
