package com.example.musicapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.service.SpotifyApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlbumDetailViewModel extends AndroidViewModel {
  private final MutableLiveData<List<Song>> _albumSongs = new MutableLiveData<>();
  private final MutableLiveData<AlbumSimplified> _albumDetails = new MutableLiveData<>();
  public LiveData<List<Song>> albumSongs = _albumSongs;
  public LiveData<AlbumSimplified> albumDetails = _albumDetails;
  public AlbumDetailViewModel(@NonNull Application application) {
    super(application);
  }

  public void getAlbumSongs(String accessToken, String albumId) {
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
    String authorization = "Bearer " + accessToken;
    Call<AlbumSimplified> call = apiService.getSongsOfAlbum(authorization, albumId);
    call.enqueue(
        new Callback<AlbumSimplified>() {
          @Override
          public void onResponse(Call<AlbumSimplified> call, Response<AlbumSimplified> response) {
            if (response.isSuccessful()) {
              AlbumSimplified albumSimplified = response.body();
              _albumDetails.setValue(albumSimplified); // Lưu album chi tiết

              List<Song> songs = new ArrayList<>();
              for (SimplifiedTrack simplifiedTrack : albumSimplified.getTracksContainer().tracks) {
                songs.add(Song.fromSimplifiedTrack(simplifiedTrack));
              }
              _albumSongs.setValue(songs);
            } else {
              // Handle error
            }
          }

          @Override
          public void onFailure(Call<AlbumSimplified> call, Throwable t) {
            // Handle error
          }
        });
  }
}
