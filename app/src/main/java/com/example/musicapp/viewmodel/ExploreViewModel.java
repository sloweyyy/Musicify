package com.example.musicapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.model.Categories;
import com.example.musicapp.model.CategoryResponse;
import com.example.musicapp.model.SearchResult;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.service.SpotifyApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExploreViewModel extends AndroidViewModel {
  private final MutableLiveData<List<Categories>> _categories;
  private final MutableLiveData<List<Song>> _searchResults;
  public LiveData<List<Categories>> categories;
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
    this.accessToken = accessToken;
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
    Call<CategoryResponse> call = apiService.getCategories("Bearer " + accessToken);

    call.enqueue(
        new Callback<CategoryResponse>() {
          @Override
          public void onResponse(
              @NonNull Call<CategoryResponse> call, @NonNull Response<CategoryResponse> response) {
            if (response.isSuccessful()) {
              CategoryResponse categoryResponse = response.body();
              if (categoryResponse != null) {
                _categories.setValue(categoryResponse.getCategories().getItems());
              }
            } else {
              try {
                String errorBody = response.errorBody().string();
                // AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                // builder.setTitle("API call fail").setMessage(errorBody).setPositiveButton("OK",
                // null).show();
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

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
    Call<SearchResult> call = apiService.searchTracks("Bearer " + accessToken, query, "track");

    call.enqueue(
        new Callback<SearchResult>() {
          @Override
          public void onResponse(
              @NonNull Call<SearchResult> call, @NonNull Response<SearchResult> response) {
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
}
