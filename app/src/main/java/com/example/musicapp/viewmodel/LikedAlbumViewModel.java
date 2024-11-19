package com.example.musicapp.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.musicapp.model.AlbumSimplified;
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

public class LikedAlbumViewModel extends AndroidViewModel {
  private final MutableLiveData<List<AlbumSimplified>> _albums = new MutableLiveData<>();
  public LiveData<List<AlbumSimplified>> albums = _albums;
  private FirebaseStorage storage;

  public LikedAlbumViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<List<AlbumSimplified>> getLikedAlbumsLiveData() {
    return _albums;
  }

  public void fetchLikedAlbums(String accessToken) {
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("users")
        .whereEqualTo("id", userId)
        .get()
        .addOnSuccessListener(
            queryDocumentSnapshots -> {
              if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                ArrayList<String> albumids =
                    (ArrayList<String>) documentSnapshot.get("likedAlbums");
                List<AlbumSimplified> albums = new ArrayList<>();
                for (String id : albumids) {
                  getAlbum(accessToken, id, albums);
                }
                _albums.setValue(albums);
              }
            })
        .addOnFailureListener(e -> {});
  }

  private void getAlbum(String accessToken, String albumId, List<AlbumSimplified> albumList) {
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
    String authorization = "Bearer " + accessToken;

    Call<AlbumSimplified> call = apiService.getAlbum(authorization, albumId);
    call.enqueue(
        new Callback<AlbumSimplified>() {
          @Override
          public void onResponse(
              @NonNull Call<AlbumSimplified> call, @NonNull Response<AlbumSimplified> response) {
            if (response.isSuccessful()) {
              AlbumSimplified album = response.body();
              albumList.add(album);
              _albums.setValue(albumList);
              //                    adapter.notifyItemInserted(albumList.size() - 1);
            }
          }

          @Override
          public void onFailure(Call<AlbumSimplified> call, Throwable throwable) {
            Log.e("Error fetching:", throwable.getMessage());
          }
        });
  }
}
