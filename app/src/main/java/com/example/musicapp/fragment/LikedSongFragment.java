package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.security.keystore.StrongBoxUnavailableException;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class LikedSongFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private View view;
    private TextView songCount, sortStateText;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private RelativeLayout pauseContainer;
    private ImageButton pauseBtn;
    private LinearLayout backButtonLayout;
    List<Song> songs= new ArrayList<>(); ;
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewSearch;
    private EditText search;
    private Button backBtn, sortBtn;
    private SongAdapter songAdapter;
    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        List<Song> songList = new ArrayList<>();
        String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots ->
            {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    ArrayList<String> songIds = (ArrayList<String>) documentSnapshot.get("likedsong");
                    for (String id : songIds) {
                        getTrack(accessToken, id);
                    }
                    songAdapter = new SongAdapter(getContext(), songs);
                    songCount.setText(songAdapter.getItemCount() + " songs");
                    recyclerView.setAdapter(songAdapter);
                }
            }).addOnFailureListener(e -> {
            });
    }

    private enum SortState {
        DEFAULT, BY_NAME
    }
    private SortState currentSortState = SortState.DEFAULT;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.likedsong_fragment, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        backButtonLayout = view.findViewById(R.id.backButtonLayout);
        backBtn = view.findViewById(R.id.iconBack);
        sortBtn = view.findViewById(R.id.iconSort);
        sortStateText = view.findViewById(R.id.sortState);
        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch);
        recyclerViewSearch.setLayoutManager(layoutManagerNew);
        search = view.findViewById(R.id.searchSong);
        songCount = view.findViewById(R.id.songCount);
        storage = FirebaseStorage.getInstance();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        songAdapter = new SongAdapter(requireContext(), new ArrayList<>());
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
        return view;
    }

    public void searchSongs(String query){
        if (query.isEmpty()){
            recyclerViewSearch.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            songAdapter.notifyDataSetChanged();
            return;
        }
        else {
            recyclerViewSearch.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            List<Song> filteredSongs = new ArrayList<>();
            for (Song song : songs) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(song);
                    Log.e("FilteredSong: " + "", filteredSongs.toString());
                }
            }
            songAdapter = new SongAdapter(getContext(), filteredSongs);
            songAdapter.notifyDataSetChanged();
            recyclerViewSearch.setAdapter(songAdapter);
        }

    }

    private void getTrack(String accessToken, String songId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApi apiService = retrofit.create(SpotifyApi.class);
        String authorization = "Bearer " + accessToken;

        Call<SimplifiedTrack> call = apiService.getTrack(authorization, songId);
        call.enqueue(new Callback<SimplifiedTrack>() {
            @Override
            public void onResponse(Call<SimplifiedTrack> call, Response<SimplifiedTrack> response) {
                if (response.isSuccessful()) {
                    SimplifiedTrack track = response.body();
                    Song newSong = Song.fromSimplifiedTrack(track);
                    songs.add(newSong);
                    songAdapter.notifyItemInserted(songs.size() - 1);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.message());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<SimplifiedTrack> call, Throwable throwable) {

            }

        });
    }

    public interface SpotifyApi {
        @GET("v1/tracks/{songId}")
        Call<SimplifiedTrack> getTrack(@Header("Authorization") String authorization, @Path("songId") String songId);
    }
    public static class TrackModel {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("preview_url")
        private String preview_url;

        @SerializedName("artists")
        private List<ArtistModel> artists;

        @SerializedName("album")
        private AlbumModel album;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPreview_url() {
            return preview_url;
        }

        public static class ArtistModel {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }

        public static class AlbumModel {
            @SerializedName("images")
            private List<ImageModel> images;

            public static class ImageModel {
                @SerializedName("url")
                private String url;

                public String getUrl() {
                    return url;
                }
            }
        }
    }
}
