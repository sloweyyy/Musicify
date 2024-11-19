package com.example.musicapp.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.musicapp.model.Artist;
import com.example.musicapp.service.SpotifyApiService;
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

public class LikedArtistViewModel extends AndroidViewModel {
  private final MutableLiveData<List<Artist>> _artists = new MutableLiveData<>();
  public LiveData<List<Artist>> artists = _artists;
  private FirebaseStorage storage;

  public LikedArtistViewModel(@NonNull Application application) {
    super(application);
  }

  public void fetchLikedArtists(String accessToken) {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String userId = auth.getCurrentUser().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("users")
        .whereEqualTo("id", userId)
        .get()
        .addOnSuccessListener(
            queryDocumentSnapshots -> {
              if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                //                if (userDoc.contains("likedArtist")) {
                ArrayList<String> likedArtistsId = (ArrayList<String>) userDoc.get("likedArtist");
                List<Artist> likedArtists = new ArrayList<>(); // Initialize followedArtists here
                for (String artistId : likedArtistsId) {
                  getArtist(accessToken, artistId, likedArtists);
                }
                _artists.setValue(likedArtists);
                //                }
              }
            })
        .addOnFailureListener(e -> {});
  }

  private void getArtist(String accessToken, String artistId, List<Artist> artistList) {
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
    String authorization = "Bearer " + accessToken;

    Call<Artist> call = apiService.getArtist(authorization, artistId);
    call.enqueue(
        new Callback<Artist>() {
          @Override
          public void onResponse(@NonNull Call<Artist> call, @NonNull Response<Artist> response) {
            if (response.isSuccessful()) {
              Artist artist = response.body();
              artistList.add(artist);
              _artists.setValue(artistList);
            }
          }

          @Override
          public void onFailure(Call<Artist> call, Throwable throwable) {
            Log.e("Error fetching:", throwable.getMessage());
          }
        });
  }
}
