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
import com.example.musicapp.model.BottomAppBarListener;
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
    private SongAdapter songAdapter;
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
    private ImageView imageView;

    private boolean isFragmentAttached = false;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        isFragmentAttached = true;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        isFragmentAttached = false;
    }
    public ArtistDetailFragment() {}
    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        getArtist(accessToken);
    }

    private void getArtist(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ArtistDetailFragment.SpotifyApiService apiService = retrofit.create(ArtistDetailFragment.SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<Artist> call = apiService.getArtist(authorization, artistId);
        Call<ArtistAlbums> call1 = apiService.getArtistAlbums(authorization, artistId);
        Call<ArtistTopTrack> call2 = apiService.getArtistTopTrack(authorization, artistId);
        //Artist
        call.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(Call<Artist> call, Response<Artist> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    Artist artist = response.body();
                    artistName.setText(artist.getName());
                    Glide.with(requireContext()).load(artist.getImages().get(0).getUrl()).into(imageView);
                }
            }

            @Override
            public void onFailure(Call<Artist> call, Throwable throwable) {

            }
        });
        //Artist's albums
        call1.enqueue(new Callback<ArtistAlbums>() {
            @Override
            public void onResponse(Call<ArtistAlbums> call1, Response<ArtistAlbums> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    ArtistAlbums artistAlbums = response.body();
                    List<AlbumSimplified> albumSimplifiedList = artistAlbums.getListAlbum();
                    albumAdapter = new AlbumAdapter(getContext(), albumSimplifiedList);
                    recyclerViewAlbums.setAdapter(albumAdapter);
                    recyclerViewAlbums.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ArtistAlbums> call1, Throwable throwable) {

            }
        });
        //get Artist's Top Songs
        call2.enqueue(new Callback<ArtistTopTrack>() {
            @Override
            public void onResponse(Call<ArtistTopTrack> call, Response<ArtistTopTrack> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    ArtistTopTrack artistTopTrack = response.body();
                    List<Song> songs = new ArrayList<>();
                    for (SimplifiedTrack simplifiedTrack : artistTopTrack.getListTrack()) {
                        songs.add(Song.fromSimplifiedTrack(simplifiedTrack));
                    }
                    songAdapter = new SongAdapter(getContext(), songs);
                    recyclerViewSongs.setAdapter(songAdapter);
                    recyclerViewSongs.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onFailure(Call<ArtistTopTrack> call, Throwable throwable) {
            }
        });
    }
    public interface SpotifyApiService {
        @GET("v1/artists/{artistId}")
        Call<Artist> getArtist(@Header("Authorization") String authorization, @Path("artistId") String artistId);

        @GET("v1/artists/{artistId}/albums")
        Call<ArtistAlbums> getArtistAlbums(@Header("Authorization") String authorization, @Path("artistId") String artistId);

        @GET("v1/artists/{artistId}/top-tracks")
        Call<ArtistTopTrack> getArtistTopTrack(@Header("Authorization") String authorization, @Path("artistId") String artistId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_artist_detail, container, false);
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        recyclerViewAlbums = view.findViewById(R.id.recyclerView_Albums);
        recyclerViewSongs = view.findViewById(R.id.recyclerView_Songs);
        artistName = view.findViewById(R.id.artistName);
        imageView = view.findViewById(R.id.imgArtist);
        backButton = view.findViewById(R.id.backBtn);
        moreButton = view.findViewById(R.id.moreBtn);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAlbums.setLayoutManager(horizontalLayoutManager);
        recyclerViewAlbums.setLayoutManager(horizontalLayoutManager);
        recyclerViewSongs.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            artistId = getArguments().getString("artistId");
        }
        setupBackButton();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
        setupMoreButton();
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

    private void setupMoreButton() {
        moreButton = getView().findViewById(R.id.moreBtn);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionsDialog(getContext());
            }
        });
    }

    private void showMoreOptionsDialog(Context context) {
        // Create a new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.more_dialog_artist, null);

        // Set the dialog's content view
        builder.setView(dialogView);

        // Get references to the buttons in the dialog
        ImageButton flBtn = dialogView.findViewById(R.id.follow);
        ImageButton reportBtn = dialogView.findViewById(R.id.report);
        TextView flText = dialogView.findViewById(R.id.follow_or_not);
        Button cancel = dialogView.findViewById(R.id.cancel);
        AlertDialog dialog = builder.create();
        dialog.show();

        checkIsFollwed(artistId, new OnIsFollowedCallback() {
            @Override
            public void onResult(boolean isFollowed) {
                if (isFollowed) {
                    flBtn.setBackgroundResource(R.drawable.follow_fill);
                    flText.setTextColor(Color.parseColor("#49A078"));
                    flText.setText("Unfollow");
                } else {
                    flBtn.setBackgroundResource(R.drawable.follow);
                    flText.setTextColor(Color.parseColor("#FFFFFF"));
                    flText.setText("Follow");
                }
            }
        });
        // Set click listeners for the buttons

        flBtn.setOnClickListener(v -> { if (isFragmentAttached) {
            checkIsFollwed(artistId, isFollowed -> {
                if (isFollowed) {
                    unfollowArtist(artistId, requireContext());
                    flBtn.setBackgroundResource(R.drawable.follow);
                    flText.setTextColor(Color.parseColor("#FFFFFF"));
                    flText.setText("Follow");
                } else {
                    addFollowedArtist(artistId, requireContext());
                    flBtn.setBackgroundResource(R.drawable.follow_fill);

                    flText.setTextColor(Color.parseColor("#49A078"));
                    flText.setText("Unfollow");
                }
            });
        }});
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
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
        void onResult(boolean isFollowed);
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
