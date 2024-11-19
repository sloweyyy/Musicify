package com.example.musicapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.FollowedArtistAdapter;
import com.example.musicapp.model.Artist;
import com.example.musicapp.viewmodel.LikedArtistViewModel;
import com.google.firebase.storage.FirebaseStorage;
import java.util.List;

public class FollowedArtistFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private View view;
    private RecyclerView recyclerView;
    private LikedArtistViewModel viewModel;
    private FollowedArtistAdapter adapter;
    private List<Artist> followedArtists;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_artists, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        viewModel = new ViewModelProvider(this).get(LikedArtistViewModel.class);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        storage = FirebaseStorage.getInstance();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        ImageView recentlyPlayedIcon = view.findViewById(R.id.iconSortArtist);
        TextView recentlyPlayedText = view.findViewById(R.id.textRecentlyPlayed);
        final boolean[] isRecentlyPlayed = {false};

        View.OnClickListener recentlyAddedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyPlayed[0] = !isRecentlyPlayed[0];
                if (isRecentlyPlayed[0]) {
                    recentlyPlayedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyPlayedText.setText("Name A-Z");
                    adapter.sortArtistByName(); // Call sortArtistByName()
                } else {
                    recentlyPlayedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyPlayedText.setText("Name Z-A");
                    adapter.sortArtistByName();
                }
            }
        };
        recentlyPlayedIcon.setOnClickListener(recentlyAddedClickListener);
        recentlyPlayedText.setOnClickListener(recentlyAddedClickListener);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        viewModel.fetchLikedArtists(accessToken);
        viewModel.artists.observe(this, artists -> {
            adapter = new FollowedArtistAdapter(getContext(), artists);
            recyclerView.setAdapter(adapter);
        });
    }

}
