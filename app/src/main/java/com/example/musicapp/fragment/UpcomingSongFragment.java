package com.example.musicapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.Song;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class UpcomingSongFragment extends BottomSheetDialogFragment {

    private RecyclerView upcomingSongRecyclerView;
    private SongAdapter upcomingSongAdapter;
    private List<Song> upcomingSongList;
    private Button backButton;
    private PlaySongFragment playSongFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            upcomingSongList = (List<Song>) getArguments().getSerializable("songList");
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming_song, container, false);
        backButton = view.findViewById(R.id.iconBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        upcomingSongRecyclerView = view.findViewById(R.id.upcoming_song_recyclerview);
        upcomingSongRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Retrieve the PlaySongFragment instance
        playSongFragment = PlaySongFragment.getInstance(null, null);

        upcomingSongAdapter = new SongAdapter(getContext(), upcomingSongList, new SongAdapter.OnSongSelectedListener() {
            @Override
            public void onSongSelected(Song song) {
                dismiss();
            }
        });
        upcomingSongRecyclerView.setAdapter(upcomingSongAdapter);

        return view;
    }

    private String getPreviousSongId(String currentSongId) {
        int currentIndex = getCurrentSongIndex(currentSongId);
        if (currentIndex > 0) {
            return upcomingSongList.get(currentIndex - 1).getId();
        } else {
            return upcomingSongList.get(upcomingSongList.size() - 1).getId();
        }
    }

    private String getNextSongId(String currentSongId) {
        int currentIndex = getCurrentSongIndex(currentSongId);
        if (currentIndex < upcomingSongList.size() - 1) {
            return upcomingSongList.get(currentIndex + 1).getId();
        } else {
            return upcomingSongList.get(0).getId();
        }
    }

    private int getCurrentSongIndex(String songId) {
        for (int i = 0; i < upcomingSongList.size(); i++) {
            if (upcomingSongList.get(i).getId().equals(songId)) {
                return i;
            }
        }
        return -1;
    }

}