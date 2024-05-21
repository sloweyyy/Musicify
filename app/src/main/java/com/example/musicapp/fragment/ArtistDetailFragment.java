package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.AlbumAdapter;
import com.example.musicapp.adapter.FetchAccessToken;
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
import com.google.gson.annotations.SerializedName;


public class ArtistDetailFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private RecyclerView recyclerViewAlbums;
    private AlbumAdapter albumAdapter;

    private RecyclerView recyclerViewSongs;
    private Artist artist;
    private Button backButton;
    private View view;
    private String artistId;
    private String accessToken;
    private FetchAccessToken fetchAccessToken;
    private TextView artistName;

    private ImageView imageView;

    public ArtistDetailFragment() {}

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
//        getAlbums(accessToken);
        getArtistAlbums(accessToken);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_artist_detail, container, false);
        recyclerViewAlbums = view.findViewById(R.id.recyclerView_Albums);
        recyclerViewSongs = view.findViewById(R.id.recyclerView_Songs);
        artistName = view.findViewById(R.id.artistName);
        imageView = view.findViewById(R.id.albumBanner);
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
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
    }

    private void setupBackButton() {
        backButton = getView().findViewById(R.id.iconBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    public interface SpotifyApiService_Artist {
        @GET("v1/artists/{artistId}")
        Call<Artist> getAlbums(@Header("Authorization") String authorization, @Path("artistId") String artistId);
    }
    public interface SpotifyApiService_ArtistAlbums {
        @GET("v1/artists/{artistId}/albums")
        Call<ArtistAlbums> getArtistAlbums(@Header("Authorization") String authorization, @Path("artistId") String artistId);
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
