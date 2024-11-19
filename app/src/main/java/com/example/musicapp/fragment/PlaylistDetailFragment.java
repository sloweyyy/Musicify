package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.Playlist;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class PlaylistDetailFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String ARG_PLAYLIST_ID = "playlistId";
    private static final String ARG_PLAYLIST_NAME = "playlistName";
    private static final String ARG_PLAYLIST_DESCRIPTION = "playlistDescription";
    private static final String ARG_PLAYLIST_THUMBNAIL = "playlistThumbnail";
    private static final String ARG_PLAYLIST_IMAGE_URL = "playlistImageURL";
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList;
    private Button backButton;
    private Button threeDotsButton;
    private BottomSheetDialog editPlaylistDialog;
    private Playlist currentPlaylist;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private String mPlaylistImageURL;
    private String mPlaylistName;
    private String mPlaylistDescription;
    private ImageView thumbnailImageView;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private String mPlaylistId;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private List<String> spotifySongIds;
    private SongAdapter songAdapter;


    public PlaylistDetailFragment() {
        // Required empty public constructor
    }


    public static PlaylistDetailFragment newInstance(String playlistName, String imageURL, String playlistDescription, String playlistId) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYLIST_NAME, playlistName);
        args.putString(ARG_PLAYLIST_IMAGE_URL, imageURL);
        args.putString(ARG_PLAYLIST_DESCRIPTION, playlistDescription);
        args.putString(ARG_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPlaylistName = bundle.getString(ARG_PLAYLIST_NAME);
            mPlaylistDescription = bundle.getString(ARG_PLAYLIST_DESCRIPTION);
            mPlaylistImageURL = bundle.getString(ARG_PLAYLIST_IMAGE_URL);
            mPlaylistId = bundle.getString(ARG_PLAYLIST_ID);
        }

        Log.d("PlaylistDetailFragment", "Playlist ID: " + mPlaylistId);

        thumbnailImageView = view.findViewById(R.id.playlistBanner);
        nameTextView = view.findViewById(R.id.playlistName);
        descriptionTextView = view.findViewById(R.id.playlistDescription);
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        threeDotsButton = view.findViewById(R.id.threeDots);
        backButton = getView().findViewById(R.id.iconBack);
        descriptionTextView.setVisibility(View.GONE);

        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            mPlaylistName = getArguments().getString(ARG_PLAYLIST_NAME);
            mPlaylistDescription = getArguments().getString(ARG_PLAYLIST_DESCRIPTION);
            mPlaylistImageURL = getArguments().getString(ARG_PLAYLIST_IMAGE_URL);
            mPlaylistId = getArguments().getString(ARG_PLAYLIST_ID);
        }

        Glide.with(requireContext()).load(mPlaylistImageURL).placeholder(R.drawable.image_up).error(R.drawable.image_up).into(thumbnailImageView);

        nameTextView.setText(mPlaylistName);
        descriptionTextView.setText(mPlaylistDescription);

        recyclerView = view.findViewById(R.id.recyclerViewSongDetail);
        if (recyclerView != null) {
            setupRecyclerView();
            if (mPlaylistId != null) {
                fetchPlaylistSongs(mPlaylistId);
            } else {
                Log.e("PlaylistDetailFragment", "Playlist ID is null");
            }
        } else {
        }
        setupBackButton();


        threeDotsButton.setOnClickListener(v -> {
            getPlaylistById(mPlaylistId, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentPlaylist = new Playlist(document.getId(), document.getString("userId"), document.getString("name"), document.getString("description"), document.getString("imageURL"));
                        showEditPlaylistBottomSheet(currentPlaylist);
                    } else {
                        Log.d("PlaylistDetailFragment", "No such playlist");
                    }
                } else {
                    Log.d("PlaylistDetailFragment", "Get failed with ", task.getException());
                }
            });
        });

    }

    private void setupBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

    }


    private void setupRecyclerView() {
        songList = new ArrayList<>();
        adapter = new SongAdapter(getContext(), songList, song -> {
        });
        adapter.setOnLongItemClickListener(new SongAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Song song) {
                showDeleteConfirmationDialog(song);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void showDeleteConfirmationDialog(Song song) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Song").setMessage("Are you sure you want to delete this song from the playlist?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> updatedSongIds = new ArrayList<>(currentPlaylist.getSongs());
                updatedSongIds.remove(song.getId());
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("playlists").document(currentPlaylist.getId()).update("songs", updatedSongIds).addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Song deleted successfully", Toast.LENGTH_SHORT).show();
                    currentPlaylist.setSongs(updatedSongIds);
                    fetchSpotifyTracks(updatedSongIds);
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete song", Toast.LENGTH_SHORT).show();
                    Log.e("PlaylistDetailFragment", "Error deleting song", e);
                });
            }
        }).setNegativeButton("Cancel", null).show();
    }

    private void fetchPlaylistSongs(String playlistId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Playlist playlist = documentSnapshot.toObject(Playlist.class);
                currentPlaylist = documentSnapshot.toObject(Playlist.class);


                assert playlist != null;
                spotifySongIds = playlist.getSongs();

                if (spotifySongIds != null && !spotifySongIds.isEmpty()) {
                    fetchSpotifyTracks(spotifySongIds);
                } else {
                    Log.d("PlaylistDetailFragment", "No Spotify songs found in the playlist");
                }
            } else {
                Log.d("PlaylistDetailFragment", "No playlist found with ID: " + playlistId);
            }
        }).addOnFailureListener(e -> {
            Log.e("PlaylistDetailFragment", "Error fetching playlist songs", e);
        });
    }

    private void fetchSpotifyTracks(List<String> trackIds) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();

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
                            adapter.clearSongs();
                            songList.addAll(fetchedSongs);
                            adapter.notifyDataSetChanged();
                        }

                    } else {
                        Log.e("PlaylistDetailFragment", "Error fetching Spotify track: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<SimplifiedTrack> call, Throwable t) {
                    Log.e("PlaylistDetailFragment", "Error fetching Spotify track", t);
                }
            });
        }
    }


    private void fetchSongDetails(String songId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("songs").document(songId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Song song = documentSnapshot.toObject(Song.class);
                adapter.addSong(song);
            } else {
                Log.d("PlaylistDetailFragment", "No song found with ID: " + songId);
            }
        }).addOnFailureListener(e -> {
            Log.e("PlaylistDetailFragment", "Error fetching song details", e);
        });
    }

    private void loadSongsFromSpotify() {

        adapter.notifyDataSetChanged();
    }

    private void showEditPlaylistBottomSheet(Playlist playlist) {
        editPlaylistDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_new_playlist, null);
        editPlaylistDialog.setContentView(sheetView);

        TextView titleTextView = sheetView.findViewById(R.id.createPlaylistTitle);
        EditText playlistNameEditText = sheetView.findViewById(R.id.playListName);
        ImageView playlistImage = sheetView.findViewById(R.id.playlistImage);
        Button updateButton = sheetView.findViewById(R.id.createPlaylist);
        Button deleteButton = sheetView.findViewById(R.id.cancelCreatePlaylist);
        titleTextView.setText("Edit Playlist");
        updateButton.setText("Update");
        deleteButton.setText("Delete");

        playlistNameEditText.setText(playlist.getName());
        if (playlist.getImageURL() != null) {
            Glide.with(requireContext()).load(playlist.getImageURL()).into(playlistImage);
            selectedImageUri = Uri.parse(playlist.getImageURL());
        } else {
            playlistImage.setImageResource(R.drawable.image_up);
        }

        updateButton.setOnClickListener(v -> {
            String newName = playlistNameEditText.getText().toString();

            if (selectedImageUri != null && !selectedImageUri.toString().equals(playlist.getImageURL())) {
                StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
                storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageURL = uri.toString();

                        updatePlaylist(playlist, newName, newImageURL, editPlaylistDialog);
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e("PlaylistDetailFragment", "Error uploading image", e);
                });
            } else {
                updatePlaylist(playlist, newName, playlist.getImageURL(), editPlaylistDialog);
            }
        });

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext()).setTitle("Delete Playlist").setMessage("Are you sure you want to delete this playlist?").setPositiveButton("Yes", (dialog, which) -> {
                deletePlaylist(playlist);
                editPlaylistDialog.dismiss();
            }).setNegativeButton("No", null).show();
        });

        playlistImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        editPlaylistDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if (editPlaylistDialog != null && editPlaylistDialog.isShowing()) {
                ImageView playlistImage = editPlaylistDialog.findViewById(R.id.playlistImage);
                playlistImage.setImageURI(selectedImageUri);
            }
        }
    }

    private void updatePlaylist(Playlist playlist, String newName, String newImageURL, BottomSheetDialog bottomSheetDialog) {
        if (selectedImageUri != null && !selectedImageUri.toString().equals(playlist.getImageURL())) {
            StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
            storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String uploadedImageURL = uri.toString();
                    updatePlaylistInFirestore(playlist, newName, uploadedImageURL, bottomSheetDialog);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                Log.e("PlaylistDetailFragment", "Error uploading image", e);
            });
        } else {
            updatePlaylistInFirestore(playlist, newName, newImageURL, bottomSheetDialog);
        }
    }


    private void updatePlaylistInFirestore(Playlist playlist, String newName, String newImageURL, BottomSheetDialog bottomSheetDialog) {
        if (playlist.getId() == null) {
            Log.e("PlaylistDetailFragment", "Playlist ID is null. Cannot update playlist.");
            // Handle the error appropriately, e.g., display an error message to the user.
            return; // Stop further execution.
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("PlaylistDetailFragment", "Updating playlist: " + playlist.getId());
        db.collection("playlists").document(playlist.getId()).update("name", newName, "imageURL", newImageURL).addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Playlist updated successfully", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();

            currentPlaylist.setName(newName);
            currentPlaylist.setImageURL(newImageURL);

            nameTextView.setText(newName);
            descriptionTextView.setText(currentPlaylist.getDescription());
            Glide.with(requireContext()).load(newImageURL).into(thumbnailImageView);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to update playlist", Toast.LENGTH_SHORT).show();
            Log.e("PlaylistDetailFragment", "Error updating playlist", e);
        });
    }

    private void deletePlaylist(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Playlist deleted successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to delete playlist", Toast.LENGTH_SHORT).show();
            Log.e("PlaylistDetailFragment", "Error deleting playlist", e);
        });
    }

    public void getPlaylistById(String playlistId, OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistId)
                .get()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.e("PlaylistDetailFragment", "Error fetching playlist data", e);

                });
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        if (mPlaylistId != null) {
            fetchPlaylistSongs(mPlaylistId);
        } else {
            Log.e("PlaylistDetailFragment", "Playlist ID is null");
        }
    }

    public interface SpotifyApiService {
        @GET("v1/tracks/{trackId}")
        Call<SimplifiedTrack> getTrack(@Header("Authorization") String authorization, @Path("trackId") String trackId);
    }
}
