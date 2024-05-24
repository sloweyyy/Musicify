package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

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

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList;
    private Button backButton;
    private Button threeDotsButton;
    private BottomSheetDialog editPlaylistDialog;
    private Playlist currentPlaylist;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;

    private static final String ARG_PLAYLIST_ID = "playlistId";
    private static final String ARG_PLAYLIST_NAME = "playlistName";
    private static final String ARG_PLAYLIST_DESCRIPTION = "playlistDescription";
    private static final String ARG_PLAYLIST_THUMBNAIL = "playlistThumbnail";
    private static final String ARG_PLAYLIST_IMAGE_URL = "playlistImageURL";
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

        thumbnailImageView = view.findViewById(R.id.playlistBanner);
        nameTextView = view.findViewById(R.id.playlistName);
        descriptionTextView = view.findViewById(R.id.playlistDescription);
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify((FetchAccessToken.AccessTokenCallback) this);
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
            setupRecyclerView(); // Call setupRecyclerView() first
            if (mPlaylistId != null) {
                fetchPlaylistSongs(mPlaylistId);
            } else {
                Log.e("PlaylistDetailFragment", "Playlist ID is null");
            }
        } else {
            // Handle the case where the RecyclerView isn't found
        }
        setupBackButton();


        threeDotsButton.setOnClickListener(v -> {
            getPlaylistById(mPlaylistId, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentPlaylist = document.toObject(Playlist.class);
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
            // Handle song selection here (e.g., play the song)
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void fetchPlaylistSongs(String playlistId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Playlist playlist = documentSnapshot.toObject(Playlist.class);

                // Get Spotify song IDs from playlist (assuming the field is named 'spotifySongIds')
                assert playlist != null;
                spotifySongIds = playlist.getSongs();

                if (spotifySongIds != null && !spotifySongIds.isEmpty()) {
                    // Fetch Spotify tracks using the IDs
                    fetchSpotifyTracks(spotifySongIds);
                } else {
                    // Handle the case where there are no Spotify songs in the playlist
                    Log.d("PlaylistDetailFragment", "No Spotify songs found in the playlist");
                    // You might want to display a message to the user or fetch songs from your "songs" collection
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
                        SimplifiedTrack simplifiedTrack = response.body(); // Get SimplifiedTrack
                        Song song = Song.fromSimplifiedTrack(simplifiedTrack); // Convert to Song
                        fetchedSongs.add(song);

                        if (fetchedSongs.size() == trackIds.size()) {
                            adapter.clearSongs();
                            songList.addAll(fetchedSongs);
                            adapter.notifyDataSetChanged();
                        }

                    } else {
                        Log.e("PlaylistDetailFragment", "Error fetching Spotify track: " + response.code());
                        // Handle API errors
                    }
                }

                @Override
                public void onFailure(Call<SimplifiedTrack> call, Throwable t) {
                    Log.e("PlaylistDetailFragment", "Error fetching Spotify track", t);
                    // Handle network errors
                }
            });
        }
    }


    private void fetchSongDetails(String songId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("songs") // Assuming your songs are in a "songs" collection
                .document(songId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Song song = documentSnapshot.toObject(Song.class);
                        adapter.addSong(song); // Add the song to the adapter
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
        Button cancelButton = sheetView.findViewById(R.id.cancelCreatePlaylist);
        titleTextView.setText("Edit Playlist");
        updateButton.setText("Update");

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

        cancelButton.setOnClickListener(v -> editPlaylistDialog.dismiss());


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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    public void getPlaylistById(String playlistId, OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistId).get().addOnCompleteListener(listener);
    }

    public interface SpotifyApiService {
        @GET("v1/tracks/{trackId}")
        Call<SimplifiedTrack> getTrack(@Header("Authorization") String authorization, @Path("trackId") String trackId);
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        // Initiate the fetching of Spotify tracks
        if (mPlaylistId != null) {
            fetchPlaylistSongs(mPlaylistId);
        } else {
            Log.e("PlaylistDetailFragment", "Playlist ID is null");
        }
    }


}
