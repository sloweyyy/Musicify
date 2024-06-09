package com.example.musicapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.Artist;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.service.SpotifyApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArtistDetailViewModel extends AndroidViewModel {
    private MutableLiveData<Artist> _artistDetail = new MutableLiveData<>();
    public LiveData<Artist> artistDetail = _artistDetail;
    private MutableLiveData<List<AlbumSimplified>> _artistAlbums = new MutableLiveData<>();
    public LiveData<List<AlbumSimplified>> artistAlbums = _artistAlbums;

    private MutableLiveData<List<Song>> _artistTopSongs = new MutableLiveData<>();
    public LiveData<List<Song>> artistTopSongs = _artistTopSongs;

    public ArtistDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void getArtist(String accessToken, String artistId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<Artist> call = apiService.getArtist(authorization, artistId);
        //Artist
        call.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(Call<Artist> call, Response<Artist> response) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    Artist artist = response.body();
                    _artistDetail.setValue(artist);
                }
            }

            @Override
            public void onFailure(Call<Artist> call, Throwable throwable) {

            }
        });
    }

    //Artist's albums
    public void getArtistAlbums(String accessToken, String artistId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<ArtistAlbums> call = apiService.getArtistAlbums(authorization, artistId);
        call.enqueue(new Callback<ArtistAlbums>() {
            @Override
            public void onResponse(Call<ArtistAlbums> call1, Response<ArtistAlbums> response) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    ArtistAlbums albums = response.body();
                    List<AlbumSimplified> albumSimplifiedList = albums.getListAlbum();
                    _artistAlbums.setValue(albumSimplifiedList);
                }
            }

            @Override
            public void onFailure(Call<ArtistAlbums> call1, Throwable throwable) {

            }
        });
    }

    //get Artist's Top Songs
    public void getArtistTopSongs(String accessToken, String artistId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<ArtistTopTrack> call = apiService.getArtistTopTrack(authorization, artistId);
        call.enqueue(new Callback<ArtistTopTrack>() {
            @Override
            public void onResponse(Call<ArtistTopTrack> call, Response<ArtistTopTrack> response) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    ArtistTopTrack artistTopTrack = response.body();
                    List<Song> songs = new ArrayList<>();
                    for (SimplifiedTrack simplifiedTrack : artistTopTrack.getListTrack()) {
                        songs.add(Song.fromSimplifiedTrack(simplifiedTrack));
                    }
                    _artistTopSongs.setValue(songs);
                }
            }

            @Override
            public void onFailure(Call<ArtistTopTrack> call, Throwable throwable) {
            }
        });
    }

    public void unfollowArtist(String artistId, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedArtist", FieldValue.arrayRemove(artistId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Removed from followed artists successfully", Toast.LENGTH_SHORT).show();
//                                    followedArtists.removeIf(artist -> artist.getId().equals(artistId));
                                })
                                .addOnFailureListener(e -> Log.e("FollowedArtistAdapter", "Failed to remove artist from followed artists: " + e.getMessage()));
                    } else {
                        Log.e("FollowedArtistAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("FollowedArtistAdapter", "Failed to retrieve user document: " + e.getMessage()));
    }

    public void addFollowedArtist(String artistId, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedArtist", FieldValue.arrayUnion(artistId))
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Added to followed artists successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Log.e("FollowedArtistAdapter", "Failed to add artist to followed artists: " + e.getMessage()));
                    } else {
                        Log.e("FollowedArtistAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("FollowedArtistAdapter", "Failed to retrieve user document: " + e.getMessage()));
    }

    public void checkIsFollwed(String id, OnIsFollowedCallback onIsFollowedCallback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> followedArtists = (List<String>) userDoc.get("likedArtist");
                        onIsFollowedCallback.onResult(followedArtists.contains(id));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ArtistDetailFragment", "Failed to retrieve user document: " + e.getMessage());
                    onIsFollowedCallback.onResult(false);
                });
    }

    public interface OnIsFollowedCallback {
        void onResult(boolean isFollowed);
    }

    public class ArtistAlbums {
        @SerializedName("items")
        private List<AlbumSimplified> ListAlbum;

        public List<AlbumSimplified> getListAlbum() {
            return ListAlbum;
        }
    }

    public class ArtistTopTrack {
        @SerializedName("tracks")
        private List<SimplifiedTrack> ListTrack;

        public List<SimplifiedTrack> getListTrack() {
            return ListTrack;
        }
    }
}
