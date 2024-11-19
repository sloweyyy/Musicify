package com.example.musicapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.service.SpotifyApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LikedSongViewModel extends AndroidViewModel {
  private final MutableLiveData<List<Song>> _songs = new MutableLiveData<>();
  public LiveData<List<Song>> songs = _songs;

  private String accessToken;

  public LikedSongViewModel(@NonNull Application application) {
    super(application);
  }

  public void onTokenReceived(String accessToken) {
    this.accessToken = accessToken;
    fetchLikedSongs(accessToken);
  }

  public void fetchLikedSongs(String accessToken) {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String userId = auth.getCurrentUser().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("users")
        .whereEqualTo("id", userId)
        .get()
        .addOnSuccessListener(
            queryDocumentSnapshots -> {
              if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                ArrayList<String> songIds = (ArrayList<String>) documentSnapshot.get("likedsong");
                List<Song> songs = new ArrayList<>();
                for (String id : songIds) {
                  getTrack(accessToken, id, songs);
                }
                _songs.setValue(songs);
              }
            })
        .addOnFailureListener(
            e -> {
              // Handle failure
            });
  }

  public void getTrack(String accessToken, String songId, List<Song> songs) {
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
    String authorization = "Bearer " + accessToken;

    Call<SimplifiedTrack> call = apiService.getTrack(authorization, songId);
    call.enqueue(
        new Callback<SimplifiedTrack>() {
          @Override
          public void onResponse(Call<SimplifiedTrack> call, Response<SimplifiedTrack> response) {
            if (response.isSuccessful()) {
              SimplifiedTrack track = response.body();
              if (track != null) {
                Song song = Song.fromSimplifiedTrack(track);
                songs.add(song);
                _songs.setValue(songs);
              }
            } else {
              // ... (Error handling) ...
            }
          }

          @Override
          public void onFailure(Call<SimplifiedTrack> call, Throwable throwable) {
            // Handle failure
          }
        });
  }
}
