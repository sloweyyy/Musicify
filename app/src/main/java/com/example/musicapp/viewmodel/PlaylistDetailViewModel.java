package com.example.musicapp.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicapp.model.Playlist;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.service.SpotifyApiService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class PlaylistDetailViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final MutableLiveData<Playlist> playlistLiveData;
    private final MutableLiveData<List<Song>> songListLiveData;
    private String accessToken;

    public PlaylistDetailViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        playlistLiveData = new MutableLiveData<>();
        songListLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<Playlist> getPlaylistLiveData() {
        return playlistLiveData;
    }

    public LiveData<List<Song>> getSongListLiveData() {
        return songListLiveData;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public void fetchPlaylistById(String playlistId) {
        db.collection("playlists").document(playlistId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Playlist playlist = document.toObject(Playlist.class);
                    playlistLiveData.setValue(playlist);
                    fetchSpotifyTracks(playlist.getSongs());
                } else {
                    Log.d("PlaylistDetailViewModel", "No such playlist");
                }
            } else {
                Log.d("PlaylistDetailViewModel", "Get failed with ", task.getException());
            }
        });
    }

    private void fetchSpotifyTracks(List<String> trackIds) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);

        List<Song> fetchedSongs = new ArrayList<>();

        for (String trackId : trackIds) {
            Call<SimplifiedTrack> call = apiService.getTrack("Bearer " + accessToken, trackId);
            call.enqueue(new Callback<SimplifiedTrack>() {
                @Override
                public void onResponse(Call<SimplifiedTrack> call, Response<SimplifiedTrack> response) {
                    if (response.isSuccessful()) {
                        SimplifiedTrack simplifiedTrack = response.body();
                        Song song = Song.fromSimplifiedTrack(simplifiedTrack);
                        fetchedSongs.add(song);

                        if (fetchedSongs.size() == trackIds.size()) {
                            songListLiveData.setValue(fetchedSongs);
                        }

                    } else {
                        Log.e("PlaylistDetailViewModel", "Error fetching Spotify track: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<SimplifiedTrack> call, Throwable t) {
                    Log.e("PlaylistDetailViewModel", "Error fetching Spotify track", t);
                }
            });
        }
    }

    public void updatePlaylist(Playlist playlist, String newName, Uri selectedImageUri) {
        if (selectedImageUri != null && !selectedImageUri.toString().equals(playlist.getImageURL())) {
            StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
            storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newImageURL = uri.toString();
                    updatePlaylistInFirestore(playlist, newName, newImageURL);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplication(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                Log.e("PlaylistDetailViewModel", "Error uploading image", e);
            });
        } else {
            updatePlaylistInFirestore(playlist, newName, playlist.getImageURL());
        }
    }

    private void updatePlaylistInFirestore(Playlist playlist, String newName, String newImageURL) {
        db.collection("playlists").document(playlist.getId())
                .update("name", newName, "imageURL", newImageURL)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplication(), "Playlist updated successfully", Toast.LENGTH_SHORT).show();
                    playlist.setName(newName);
                    playlist.setImageURL(newImageURL);
                    playlistLiveData.setValue(playlist);
                }).addOnFailureListener(e -> {
                    Toast.makeText(getApplication(), "Failed to update playlist", Toast.LENGTH_SHORT).show();
                    Log.e("PlaylistDetailViewModel", "Error updating playlist", e);
                });
    }

    public void deletePlaylist(Playlist playlist) {
        db.collection("playlists").document(playlist.getId()).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplication(), "Playlist deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplication(), "Failed to delete playlist", Toast.LENGTH_SHORT).show();
            Log.e("PlaylistDetailViewModel", "Error deleting playlist", e);
        });
    }

//    public interface SpotifyApiService {
//        @GET("v1/tracks/{trackId}")
//        Call<SimplifiedTrack> getTrack(@Header("Authorization") String authorization, @Path("trackId") String trackId);
//    }
}
