package com.example.musicapp.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.adapter.exploreAdapter;
import com.example.musicapp.model.Categories;
import com.example.musicapp.model.SearchResult;
import com.example.musicapp.model.Song;
import com.example.musicapp.viewmodel.ExploreViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;


public class ExploreFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    View view;
    RecyclerView recyclerView;
    private FetchAccessToken fetchAccessToken;
    private EditText searchEditText;
    private String accessToken; // Store the access token
    private SongAdapter songAdapter; // Adapter for search results
    private exploreAdapter exploreAdapter; // Adapter for categories
    private RecyclerView recyclerViewSongs;

    private ExploreViewModel viewModel;
    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        viewModel.fetchCategories(accessToken);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_explore, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewExplore);
        recyclerViewSongs = view.findViewById(R.id.recyclerViewSongs);
        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new CenterSpaceItemDecoration(2, 90)); // 2 columns, 16dp spacing
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        searchEditText = view.findViewById(R.id.searchExplore);
        setupSearchEditText();
        setupObservers();
        return view;
    }

    private void setupObservers() {
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            showCategories(categories);
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), songs -> {
            updateSearchResults(songs);
        });
    }

    private void setupSearchEditText() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                viewModel.searchSongs(query, accessToken);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
    }


    // Method to search for songs
//    private void searchSongs(String query) {
//        if (accessToken == null || query.isEmpty()) {
//            // Hide songs RecyclerView, show categories RecyclerView
//            recyclerViewSongs.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//            return;
//        }
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.spotify.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
//
//        Call<SearchResult> call = apiService.searchTracks("Bearer " + accessToken, query, "track");
//        call.enqueue(new Callback<SearchResult>() {
//            @Override
//            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
//                if (response.isSuccessful()) {
//                    SearchResult searchResult = response.body();
//                    if (searchResult != null && searchResult.getTracks() != null) {
//                        List<SimplifiedTrack> tracks = searchResult.getTracks().getItems();
//                        List<Song> songs = new ArrayList<>();
//                        for (SimplifiedTrack track : tracks) {
//                            songs.add(Song.fromSimplifiedTrack(track));
//                        }
//                        // Update the RecyclerView with search results
//                        songAdapter = new SongAdapter(getContext(), songs);
//                        recyclerViewSongs.setAdapter(songAdapter);
//
//                        // Hide categories RecyclerView, show songs RecyclerView
//                        recyclerView.setVisibility(View.GONE);
//                        recyclerViewSongs.setVisibility(View.VISIBLE);
//                    } else {
//                        // Handle case where search results are empty or null
//                        recyclerView.setAdapter(null); // Clear the RecyclerView
//                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    // Handle error
//                    Toast.makeText(getContext(), "Failed to search tracks", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SearchResult> call, Throwable t) {
//                // Handle failure
//                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    public void fetchCategories(String accessToken) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.spotify.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
//        Call<CategoryResponse> call = apiService.getCategories("Bearer " + accessToken);
//        call.enqueue(new Callback<CategoryResponse>() {
//            @Override
//            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
//                if (response.isSuccessful()) {
//                    Gson gson = new Gson();
//                    String jsonResponse = gson.toJson(response.body());
//                    CategoryResponse categoryResponse = gson.fromJson(jsonResponse, CategoryResponse.class);
//                    List<Category> items = categoryResponse.getCategories().getItems();
//                    showCategories(items);
//                } else {
//                    try {
//                        String errorBody = response.errorBody().string();
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                        builder.setTitle("API call fail").setMessage(errorBody).setPositiveButton("OK", null).show();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<CategoryResponse> call, Throwable t) {
//                String errorMessage = t.getMessage();
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                builder.setTitle("API call failed").setMessage(errorMessage).setPositiveButton("OK", null).show();
//            }
//        });
//    }

    // Method to show categories in the RecyclerView
    public void showCategories(List<com.example.musicapp.model.Categories> categories) {
        if (categories != null) {
            exploreAdapter = new exploreAdapter(categories);
            recyclerView.setAdapter(exploreAdapter);
        } else {
            Toast.makeText(getContext(), "Không có danh sách categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSearchResults(List<Song> songs) {
        if (songs != null && !songs.isEmpty()) {
            songAdapter = new SongAdapter(getContext(), songs, new SongAdapter.OnSongSelectedListener() {
                @Override
                public void onSongSelected(Song song) {
                    // Handle song selection
                }
            });
            recyclerViewSongs.setVisibility(View.VISIBLE); // Hiển thị RecyclerView cho kết quả tìm kiếm
            recyclerView.setVisibility(View.GONE); // Ẩn RecyclerView cho danh sách categories
            recyclerViewSongs.setAdapter(songAdapter);
        } else {
            recyclerViewSongs.setVisibility(View.GONE); // Ẩn RecyclerView cho kết quả tìm kiếm
            recyclerView.setVisibility(View.VISIBLE); // Hiển thị RecyclerView cho danh sách categories
            // Optionally, you can show a message or do something else when there are no search results
        }
    }



    // SpotifyApiService interface


    // CategoryResponse class


    // Categories class

}