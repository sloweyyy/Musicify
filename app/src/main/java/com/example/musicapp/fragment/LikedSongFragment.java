package com.example.musicapp.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.viewmodel.LikedSongViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

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

public class LikedSongFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private View view;
    private TextView songCount, sortStateText;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private RelativeLayout pauseContainer;
    private ImageButton pauseBtn;
    private LinearLayout backButtonLayout;
    //private List<Song> songs= new ArrayList<>(); ; // Remove this line
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewSearch;
    private EditText search;
    private Button backBtn, sortBtn;
    private SongAdapter songAdapter;
    private LikedSongViewModel viewModel;

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
      viewModel.fetchLikedSongs(accessToken);
    }

    private enum SortState {
        DEFAULT, BY_NAME
    }

    private SortState currentSortState = SortState.DEFAULT;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    HomeFragment homeFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.likedsong_fragment, container, false);
        viewModel = new ViewModelProvider(this).get(LikedSongViewModel.class);
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        backButtonLayout = view.findViewById(R.id.backButtonLayout);
        backBtn = view.findViewById(R.id.iconBack);
        sortBtn = view.findViewById(R.id.iconSort);
        sortStateText = view.findViewById(R.id.sortState);
        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
        search = view.findViewById(R.id.searchSong);
        songCount = view.findViewById(R.id.songCount);
        storage = FirebaseStorage.getInstance();
        homeFragment = new HomeFragment();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        songAdapter = new SongAdapter(requireContext(), new ArrayList<>(),homeFragment);
        recyclerView.setAdapter(songAdapter);
        pauseContainer = view.findViewById(R.id.pauseContainer);
        pauseBtn = view.findViewById(R.id.pauseBtn);

        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                searchSongs(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSortState == SortState.DEFAULT){
                    currentSortState = SortState.BY_NAME;
                    sortStateText.setText("Sort by name");
                    songAdapter.sortSongByName();
                }
                else {
                    currentSortState = SortState.DEFAULT;
                    sortStateText.setText("Default");
                    songAdapter.clearSongs();
                    onTokenReceived(accessToken);
                    songAdapter.notifyDataSetChanged();
                }
            }
        });
        pauseContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             songAdapter.PlayFirstSong();
            }
        });
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songAdapter.PlayFirstSong();
            }
        });
        setupSearchEditText();
        setupObservers();
        return view;
    }

    public void searchSongs(String query) {
        if (query.isEmpty()) {
            recyclerViewSearch.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (songAdapter != null) {
                songAdapter.notifyDataSetChanged();
            }
            return;
        } else {
            recyclerViewSearch.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            List<Song> filteredSongs = new ArrayList<>();
            for (Song song : songAdapter.getSongs()) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(song);
                    Log.e("FilteredSong: " + "", filteredSongs.toString());
                }
            }
            songAdapter = new SongAdapter(getContext(), filteredSongs,homeFragment);
            songAdapter.notifyDataSetChanged();
            recyclerViewSearch.setAdapter(songAdapter);
        }

    }

    private void setupObservers() {
        viewModel.songs.observe(getViewLifecycleOwner(), songs -> {
            songAdapter.setSongs(songs);
            songCount.setText(songs.size() + " songs");
        });
    }

    private void setupSearchEditText() {
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                searchSongs(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }



    public interface SpotifyApi {
        @GET("v1/tracks/{songId}")
        Call<SimplifiedTrack> getTrack(@Header("Authorization") String authorization, @Path("songId") String songId);
    }


}