package com.example.musicapp.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.musicapp.model.Playlist;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlaylistViewModel extends ViewModel {
    private final MutableLiveData<List<Playlist>> playlists = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseStorage storage;
    private final FirebaseFirestore db;

    public PlaylistViewModel() {
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Playlist>> getPlaylists() {
        return playlists;
    }

    public void fetchPlaylists(String userId) {
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Playlist> fetchedPlaylists = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        Playlist playlist = document.toObject(Playlist.class);
                        playlist.setId(document.getId());
                        Log.d("PlaylistViewModel", "Fetched playlist: " + playlist.getId());
                        List<String> songs = (List<String>) document.get("songs");
                        playlist.setSongs(songs != null ? songs : new ArrayList<>());
                        fetchedPlaylists.add(playlist);
                    });
                    playlists.setValue(fetchedPlaylists);
                })
                .addOnFailureListener(e -> Log.e("PlaylistViewModel", "Failed to fetch playlists", e));
    }

    public void savePlaylistToFirestore(Playlist newPlaylist, OnSuccessListener<Boolean> onSuccessListener) {
        if (newPlaylist.getName().isEmpty()) {
            onSuccessListener.onSuccess(false);
            return;
        }

        db.collection("playlists")
                .add(newPlaylist)
                .addOnSuccessListener(documentReference -> {
                    onSuccessListener.onSuccess(true);
                })
                .addOnFailureListener(e -> onSuccessListener.onSuccess(false));
    }


    public void uploadImage(Uri selectedImageUri, OnSuccessListener<Uri> onSuccessListener) {
        StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener))
                .addOnFailureListener(e -> Log.e("PlaylistViewModel", "Error uploading image", e));
    }

    public void sortPlaylistsByName() {
        List<Playlist> sortedPlaylists = new ArrayList<>(playlists.getValue());
        sortedPlaylists.sort((playlist1, playlist2) -> {
            String name1 = playlist1.getName();
            String name2 = playlist2.getName();
            return name1 != null && name2 != null ? name1.compareTo(name2) : 0;
        });
        playlists.setValue(sortedPlaylists);
    }

    public void sortPlaylistsbyDate() {
        List<Playlist> sortedPlaylists = new ArrayList<>(playlists.getValue());
        sortedPlaylists.sort((playlist1, playlist2) -> {
            Date date1 = playlist1.getCreatedAt();
            Date date2 = playlist2.getCreatedAt();
            return date1 != null && date2 != null ? date1.compareTo(date2) : 0;
        });
        playlists.setValue(sortedPlaylists);
    }

    public void createPlaylist(String playlistName, Uri imageUri, OnSuccessListener<Boolean> onSuccessListener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (imageUri != null) {
            StorageReference storageRef = storage.getReference().child("playlist_images/" + UUID.randomUUID().toString());
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageURL = uri.toString();
                        savePlaylistToFirestore(new Playlist(userId, playlistName, "Description", imageURL), onSuccessListener);
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("PlaylistViewModel", "Failed to upload image", e);
                        onSuccessListener.onSuccess(false);
                    });
        } else {
            Date date = new Date();
            savePlaylistToFirestore(new Playlist(userId, playlistName, null, date), onSuccessListener);
        }
    }
}
