package com.example.musicapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.musicapp.model.Playlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends ViewModel {
    private MutableLiveData<List<Playlist>> playlists = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;

    public PlaylistViewModel() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public LiveData<List<Playlist>> getPlaylists() {
        return playlists;
    }

    public void fetchPlaylists() {
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Playlist> fetchedPlaylists = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Playlist playlist = document.toObject(Playlist.class);
                        playlist.setId(document.getId());
                        playlist.setSongs((List<String>) document.get("songs"));
                        fetchedPlaylists.add(playlist);
                    }
                    playlists.setValue(fetchedPlaylists);
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error fetching playlists", e);
                });
    }

    public void createPlaylist(String playlistName, String description, String imageURL, String privacy) {
        Playlist newPlaylist = new Playlist(userId, playlistName, description, imageURL, privacy);
        db.collection("playlists")
                .add(newPlaylist)
                .addOnSuccessListener(documentReference -> {
                    String playlistId = documentReference.getId();
                    newPlaylist.setId(playlistId);
                    // Update the document with the ID
                    documentReference.set(newPlaylist)
                            .addOnSuccessListener(aVoid -> {
                                // Playlist created successfully (e.g., refresh the UI)
                            })
                            .addOnFailureListener(e -> {
                                // Handle error (e.g., display a message to the user)
                                Log.e("PlaylistViewModel", "Error updating playlist", e);
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error creating playlist", e);
                });
    }

    public void deletePlaylist(Playlist playlist) {
        db.collection("playlists").document(playlist.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Playlist deleted successfully (e.g., refresh the UI)
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error deleting playlist", e);
                });
    }

    public void updatePlaylistPrivacy(Playlist playlist) {
        db.collection("playlists").document(playlist.getId())
                .update("privacy", playlist.getPrivacy())
                .addOnSuccessListener(aVoid -> {
                    // Playlist privacy updated successfully (e.g., refresh the UI)
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error updating playlist privacy", e);
                });
    }

    public void updatePlaylistName(Playlist playlist) {
        db.collection("playlists").document(playlist.getId())
                .update("name", playlist.getName())
                .addOnSuccessListener(aVoid -> {
                    // Playlist name updated successfully (e.g., refresh the UI)
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error updating playlist name", e);
                });
    }

    public void updatePlaylistSongCount(Playlist playlist) {
        db.collection("playlists").document(playlist.getId())
                .update("songCount", playlist.getSongCount())
                .addOnSuccessListener(aVoid -> {
                    // Playlist song count updated successfully (e.g., refresh the UI)
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error updating playlist song count", e);
                });
    }

    public void updatePlaylistImage(Playlist playlist) {
        db.collection("playlists").document(playlist.getId())
                .update("imageURL", playlist.getImageURL()) // Assuming "imageURL" is the field name
                .addOnSuccessListener(aVoid -> {
                    // Playlist image updated successfully (e.g., refresh the UI)
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., display a message to the user)
                    Log.e("PlaylistViewModel", "Error updating playlist image", e);
                });
    }

    public void updatePlaylist(Playlist playlist) {
        db.collection("playlists").document(playlist.getId())
                .set(playlist)
                .addOnSuccessListener(aVoid -> {
                    // Playlist updated successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Log.e("PlaylistViewModel", "Error updating playlist", e);
                });
    }
}