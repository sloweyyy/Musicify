package com.example.musicapp.fragment;

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

import com.example.musicapp.R;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.Song;

import java.util.ArrayList;
import java.util.List;

public class LikedAlbumDetailFragment extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList;
    private Button backButton;

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
        return inflater.inflate(R.layout.fragment_album_detail, container, false);
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
        songList.add(new Song("Song 1", "Artist 1"));
        songList.add(new Song("Song 2", "Artist 2"));
        songList.add(new Song("Song 3", "Artist 3"));
        adapter.notifyDataSetChanged();
    }

}


