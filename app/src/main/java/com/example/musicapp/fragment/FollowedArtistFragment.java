package com.example.musicapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.FollowedArtistAdapter;
import com.google.firebase.storage.FirebaseStorage;

public class FollowedArtistFragment extends Fragment implements FetchAccessToken.AccessTokenCallback{
    private View view;
    private RecyclerView recyclerView;
    private FollowedArtistAdapter adapter;
//    private Map<AlbumSimplified, LocalDateTime> likedAlbums= new HashMap<>();
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private FirebaseStorage storage;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_artists, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        storage = FirebaseStorage.getInstance();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        ImageView recentlyAddedIcon = view.findViewById(R.id.iconSortArtist);
        TextView recentlyAddedText = view.findViewById(R.id.textRecentlyPlayed);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {

    }
}
