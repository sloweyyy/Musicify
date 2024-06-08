package com.example.musicapp.viewmodel;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.fragment.ExploreFragment;
import com.example.musicapp.model.Category;
import com.example.musicapp.model.SearchResult;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
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
import retrofit2.http.Query;

public class ExploreViewModel extends AndroidViewModel {
    private MutableLiveData<List<Category>> _categories;
    public LiveData<List<Category>> categories;

    private MutableLiveData<List<Song>> _searchResults;
    public LiveData<List<Song>> searchResults;

    private String accessToken;

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        _categories = new MutableLiveData<>();
        categories = _categories;

        _searchResults = new MutableLiveData<>();
        searchResults = _searchResults;
    }


    public void fetchAccessToken(FetchAccessToken.AccessTokenCallback callback) {
        FetchAccessToken fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(callback);
    }

    public void onTokenReceived(String token) {
        accessToken = token;
        fetchCategories(token);
    }

    public void fetchCategories(String accessToken) {
        this.accessToken  = accessToken;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        Call<CategoryResponse> call = apiService.getCategories("Bearer " + accessToken);

        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryResponse> call, @NonNull Response<CategoryResponse> response) {
                if (response.isSuccessful()) {
                    CategoryResponse categoryResponse = response.body();
                    if (categoryResponse != null) {
                        _categories.setValue(categoryResponse.getCategories().getItems());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        //AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        //builder.setTitle("API call fail").setMessage(errorBody).setPositiveButton("OK", null).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryResponse> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    public void searchSongs(String query, String accessTokenInput) {
        if (accessTokenInput == null || query.isEmpty()) {
            _searchResults.setValue(new ArrayList<>());
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        Call<SearchResult> call = apiService.searchTracks("Bearer " + accessToken, query, "track");

        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(@NonNull Call<SearchResult> call, @NonNull Response<SearchResult> response) {
                if (response.isSuccessful()) {
                    SearchResult searchResult = response.body();
                    if (searchResult != null && searchResult.getTracks() != null) {
                        List<Song> songs = new ArrayList<>();
                        for (SimplifiedTrack track : searchResult.getTracks().getItems()) {
                            songs.add(Song.fromSimplifiedTrack(track));
                        }
                        _searchResults.setValue(songs);
                    }
                } else {
                    // Handle error
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchResult> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }
    public class CategoryResponse {
        private ExploreFragment.Categories categories;

        public ExploreFragment.Categories getCategories() {
            return categories;
        }

        public void setCategories(ExploreFragment.Categories categories) {
            this.categories = categories;
        }

        @Override
        public String toString() {
            return "CategoryResponse{" + "categories=" + categories + '}';
        }
    }

    // Categories class
    public class Categories {
        private String href;
        private List<Category> items;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public List<Category> getItems() {
            return items;
        }

        public void setItems(List<Category> items) {
            this.items = items;
        }

        @Override
        public String toString() {
            return "Categories{" + "href='" + href + '\'' + ", items=" + items + '}';
        }
    }

    public interface SpotifyApiService {
        @GET("v1/categories/{categoryId}")
        Call<CategoryResponse> getCategory(@Header("Authorization") String authorization, @Path("categoryId") String categoryId);

        @GET("v1/browse/categories")
        Call<CategoryResponse> getCategories(@Header("Authorization") String authorization);

        @GET("v1/search")
        Call<SearchResult> searchTracks(@Header("Authorization") String authorization, @Query("q") String query, @Query("type") String type);
    }
}
