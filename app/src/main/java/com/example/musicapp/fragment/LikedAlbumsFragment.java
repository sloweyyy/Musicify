package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.LikedAlbumAdapter;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.example.musicapp.adapter.FetchAccessToken;
import com.google.firebase.firestore.DocumentReference;
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
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class LikedAlbumsFragment extends Fragment implements FetchAccessToken.AccessTokenCallback  {
    private View view;
    private RecyclerView recyclerView;
    private LikedAlbumAdapter adapter;
    List<AlbumSimplified> albumList = new ArrayList<>();
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private enum SortState {
        DEFAULT, BY_NAME, BY_PRIVACY
    }
    private SortState currentSortState = SortState.DEFAULT;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_liked_albums, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        storage = FirebaseStorage.getInstance();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        ImageView recentlyAddedIcon = view.findViewById(R.id.iconRecentlyAdded);
        TextView recentlyAddedText = view.findViewById(R.id.textRecentlyAdded);
        final boolean[] isRecentlyAdded = {false};

        View.OnClickListener recentlyAddedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyAdded[0] = !isRecentlyAdded[0];
                if (isRecentlyAdded[0]) {
                    //recentlyAddedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyAddedText.setText("Name A-Z");
                    adapter.sortAlbumByName();
                } else {
                    //recentlyAddedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyAddedText.setText("Recently Added");
                    adapter.sortAlbumByName();
                }
            }
        };
        recentlyAddedIcon.setOnClickListener(recentlyAddedClickListener);
        recentlyAddedText.setOnClickListener(recentlyAddedClickListener);

        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        String userId = "KRmDxRGH0sez8q3XRknqmmZq97S2";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots ->
        {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                ArrayList<String> albumids = (ArrayList<String>) documentSnapshot.get("likedAlbums");
                for (String id : albumids){
                    getAlbum(accessToken,id);
                }
                adapter = new LikedAlbumAdapter(getContext(), albumList);
                recyclerView.setAdapter(adapter);
            }
        }).addOnFailureListener(e -> {
        });
    }
    public interface SpotifyApi {
        @GET("v1/albums/{albumId}")
        Call<AlbumSimplified> getAlbum(@Header("Authorization") String authorization, @Path("albumId") String albumId);
    }
    private void getAlbum (String accessToken, String albumId){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();
        SpotifyApi apiService = retrofit.create(SpotifyApi.class);
        String authorization = "Bearer " + accessToken;

        Call<AlbumSimplified> call = apiService.getAlbum(authorization, albumId);
        call.enqueue(new Callback<AlbumSimplified>() {
            @Override
            public void onResponse(@NonNull Call<AlbumSimplified> call, @NonNull Response<AlbumSimplified> response) {
                if (response.isSuccessful()) {
                    AlbumSimplified album = response.body();
                    albumList.add(album);
                    adapter.notifyItemInserted(albumList.size() - 1);
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.message());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<AlbumSimplified> call, Throwable throwable) {
                Log.e("Error fetching:", throwable.getMessage());
            }
        });
    }
}