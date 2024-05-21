package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.AlbumAdapter;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.LikedAlbumAdapter;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.Artist;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.model.Artist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import com.example.musicapp.adapter.FetchAccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;
import android.content.Context;


public class ArtistDetailFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private RecyclerView recyclerViewAlbums;
    private AlbumAdapter albumAdapter;
    private RecyclerView recyclerViewSongs;
    private Artist artist;
    private List<Artist> followedArtists = new ArrayList<>();
    private Button backButton;
    private Button moreButton;
    private View view;
    private String artistId;
    private String accessToken;
    private FetchAccessToken fetchAccessToken;
    private TextView artistName;
    private TextView artistFollowers;
    private ImageView imageView;

    public ArtistDetailFragment() {}

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        getArtist(accessToken);
        getArtistAlbums(accessToken);
        getArtistTopTrack(accessToken);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_artist_detail, container, false);
        recyclerViewAlbums = view.findViewById(R.id.recyclerView_Albums);
        recyclerViewSongs = view.findViewById(R.id.recyclerView_Songs);
        artistName = view.findViewById(R.id.artistName);
        imageView = view.findViewById(R.id.imgArtist);
        artistFollowers = view.findViewById(R.id.followers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAlbums.setLayoutManager(layoutManager);
        recyclerViewSongs.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            artistId = getArguments().getString("artistId");
        }
//        setupBackButton();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    private void getArtistAlbums(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ArtistDetailFragment.SpotifyApiService_ArtistAlbums apiService = retrofit.create(ArtistDetailFragment.SpotifyApiService_ArtistAlbums.class);
        String authorization = "Bearer " + accessToken;
        Call<ArtistAlbums> call = apiService.getArtistAlbums(authorization, artistId);
        call.enqueue(new Callback<ArtistAlbums>() {

            @Override
            public void onResponse(Call<ArtistAlbums> call, Response<ArtistAlbums> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    ArtistAlbums artistAlbums = response.body();
                    List<AlbumSimplified> albumSimplifiedList = artistAlbums.getListAlbum();
                    albumAdapter = new AlbumAdapter(getContext(), albumSimplifiedList);
                    recyclerViewAlbums.setAdapter(albumAdapter);
                    recyclerViewAlbums.setVisibility(View.VISIBLE);

                } else {
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.message());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<ArtistAlbums> call, Throwable throwable) {

            }
        });
    }
    private void getArtist(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ArtistDetailFragment.SpotifyApiService_Artist apiService = retrofit.create(ArtistDetailFragment.SpotifyApiService_Artist.class);
        String authorization = "Bearer " + accessToken;
        Call<Artist> call = apiService.getArtist(authorization, artistId);
        call.enqueue(new Callback<Artist>() {

            @Override
            public void onResponse(Call<Artist> call, Response<Artist> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    Artist artist = response.body();
                    artistName.setText(artist.getName());
                    artistFollowers.setText(artist.getFollowers().getTotal() + "followers");
                    Glide.with(requireContext()).load(artist.getImages().get(0).getUrl()).into(imageView);
                } else {
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.message());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<Artist> call, Throwable throwable) {

            }
        });
    }

    private void getArtistTopTrack(String accessToken) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.spotify.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        ArtistDetailFragment.SpotifyApiService_ArtistTracks apiService = retrofit.create(ArtistDetailFragment.SpotifyApiService_ArtistTracks.class);
//        String authorization = "Bearer " + accessToken;
//        Call<ArtistTopTrack> call = apiService.getArtistTopTrack(authorization, artistId);
//        call.enqueue(new Callback<ArtistTopTrack>() {
//
//            @Override
//            public void onResponse(Call<ArtistTopTrack> call, Response<ArtistTopTrack> response) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//                if (response.isSuccessful()) {
//                    ArtistTopTrack artistTopTrack = response.body();
//                    List<AlbumSimplified> albumSimplifiedList = artistTopTrack.getListTrack();
//                    albumAdapter = new AlbumAdapter(getContext(), albumSimplifiedList);
//                    recyclerViewAlbums.setAdapter(albumAdapter);
//                    recyclerViewAlbums.setVisibility(View.VISIBLE);
//
//                } else {
//                    builder.setTitle("Cảnh báo");
//                    builder.setMessage(response.message());
//                    builder.setPositiveButton("OK", null);
//                    builder.show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ArtistTopTrack> call, Throwable throwable) {
//            }
//        });
    }

    public interface SpotifyApiService_Artist {
        @GET("v1/artists/{artistId}")
        Call<Artist> getArtist(@Header("Authorization") String authorization, @Path("artistId") String artistId);
    }
    public interface SpotifyApiService_ArtistAlbums {
        @GET("v1/artists/{artistId}/albums")
        Call<ArtistAlbums> getArtistAlbums(@Header("Authorization") String authorization, @Path("artistId") String artistId);
    }
    public interface SpotifyApiService_ArtistTracks {
        @GET("v1/artists/{artistId}")
        Call<ArtistTopTrack> getArtistTopTrack(@Header("Authorization") String authorization, @Path("artistId") String artistId);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
        setupMoreButton();
    }
    private void setupBackButton() {
        backButton = getView().findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setupMoreButton() {
        moreButton = getView().findViewById(R.id.moreBtn);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionsDialog();
            }
        });
    }

    private void showMoreOptionsDialog() {
        // Create a new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.more_dialog_artist, null);

        // Set the dialog's content view
        builder.setView(dialogView);

        // Get references to the buttons in the dialog
        ImageButton flBtn = dialogView.findViewById(R.id.follow);
        ImageButton reportBtn = dialogView.findViewById(R.id.report);
        TextView flText = dialogView.findViewById(R.id.follow_or_not);
        AlertDialog dialog = builder.create();
        dialog.show();

        checkIsFollwed(artistId, new OnIsFollowedCallback() {
            @Override
            public void onResult(boolean isLiked) {
                if (isLiked) {
                    flBtn.setImageResource(R.drawable.favourite_filled);
                    flText.setTextColor(Color.parseColor("#49A078"));
                    flText.setText("Unfollow");
                } else {
                    flBtn.setImageResource(R.drawable.favourite_outline);
                    flText.setTextColor(Color.parseColor("#FFFFFF"));
                    flText.setText("Follow");
                }
            }
        });
        // Set click listeners for the buttons

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
    }
    private void checkIsFollwed(String id, OnIsFollowedCallback onIsFollowedCallback) {
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
    private interface OnIsFollowedCallback {
        void onResult(boolean isLiked);
    }
//    public void updateFollowedArtists(List<Artist> artists) {
//        followedArtists.clear();
//        followedArtists.addAll(artists);
//        notifyDataSetChanged();
//    }

    public void unfollowArtist(String artistId,Context context) {
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
                                    followedArtists.removeIf(artist -> artist.getId().equals(artistId));
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

    public class ArtistAlbums {
        @SerializedName("items")
        private List<AlbumSimplified> ListAlbum;

        public List<AlbumSimplified> getListAlbum()
        {
            return ListAlbum;
        }
    }

    public class ArtistTopTrack{
        @SerializedName("tracks")
        private List<SimplifiedTrack> ListTrack;

        public List<SimplifiedTrack> getListTrack() {
            return ListTrack;
        }
    }
}
