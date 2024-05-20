package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.PlaylistHomeAdapter;
import com.example.musicapp.adapter.SongHomeAdapter;
import com.example.musicapp.model.PlaylistAPI;
import com.example.musicapp.model.PlaylistSimplified;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.google.gson.annotations.SerializedName;

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

public class NewsFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {

    private View view;
    private RecyclerView trackRecyclerView, playlistRecyclerView;
    private FetchAccessToken fetchAccessToken;
    private SongHomeAdapter songHomeAdapter;
    private PlaylistHomeAdapter playlistHomeAdapter;
    private String accesstoken;
    private final String playlistId = "37i9dQZF1DX5G3iiHaIzdf";
    private final String categoryId = "party";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_news, container, false);
        trackRecyclerView = view.findViewById(R.id.horizontal_recyclerView);
        playlistRecyclerView = view.findViewById(R.id.vertical_recyclerView);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(getActivity());
        trackRecyclerView.setLayoutManager(horizontalLayoutManager);
        playlistRecyclerView.setLayoutManager(verticalLayoutManager);
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {

        this.accesstoken = accessToken;
        Log.d("AccessToken", "Token: " + accessToken);
        getSongs(accesstoken);
    }

    private void getSongs(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<PlaylistSimplified> call1 = apiService.getSongs(authorization, playlistId);
        Call<PlaylistsModel> call2 = apiService.getPlaylists(authorization, categoryId);
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
//        alertDialog.setTitle("Error");
//        alertDialog.setPositiveButton("OK", null);
//        AlertDialog dialog = alertDialog.create();
//        dialog.show();
        //hỏrizontal reviewcleview
        Log.d("HEHEHEHE", "Token: " + accessToken);
        call1.enqueue(new Callback<PlaylistSimplified>() {
            @Override
            public void onResponse(Call<PlaylistSimplified> call, Response<PlaylistSimplified> response) {
                if (response.isSuccessful()) {
                    PlaylistSimplified playlistSimplified = response.body();
                    List<Song> songs = new ArrayList<>();
                    for (PlaylistSimplified.TracksModel.ItemModel item : playlistSimplified.tracksContainer.tracks) {
                        SimplifiedTrack track = item.track;
                        songs.add(Song.fromSimplifiedTrack(track));
                    }
                    songHomeAdapter = new SongHomeAdapter(getContext(), songs);
                    trackRecyclerView.setAdapter(songHomeAdapter);

                }
            }

            @Override
            public void onFailure(Call<PlaylistSimplified> call, Throwable throwable) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
                alertDialog.setTitle("Error");
                alertDialog.setMessage(throwable.getMessage());
                alertDialog.setPositiveButton("OK", null);
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });
        //vẻtical
        call2.enqueue(new Callback<PlaylistsModel>() {
            @Override
            public void onResponse(Call<PlaylistsModel> call, Response<PlaylistsModel> response) {
                if (response.isSuccessful()) {
                    PlaylistsModel playlistsContainer = response.body();
                    if (playlistsContainer != null) {
                        ShowPlaylist(playlistsContainer.Playlists.PlaylistsArray);
                    }
                    Log.d("HUHUHUHU", "Token: " + accessToken);
                }
            }

            @Override
            public void onFailure(Call<PlaylistsModel> call, Throwable throwable) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
                alertDialog.setTitle("Error");
                alertDialog.setMessage(throwable.getMessage()); // Showing the error message from the exception
                alertDialog.setPositiveButton("OK", null);
                AlertDialog dialog = alertDialog.create();
                dialog.show();
                Log.d("HUHUHUHU", "Token: " + accessToken);
            }
        });
        Log.d("HIHIHIHI", "Token: " + accessToken);
    }

    public void ShowPlaylist(List<PlaylistAPI> playlists) {
        if (playlists != null) {
//            PlaylistAdapterAPI adapter = new PlaylistAdapterAPI(playlists);
            playlistHomeAdapter = new PlaylistHomeAdapter(getContext(), playlists);
            playlistRecyclerView.setAdapter(playlistHomeAdapter);
        } else {
            Toast.makeText(getContext(), "Không có danh sách categories", Toast.LENGTH_SHORT).show();
        }

    }

    public interface SpotifyApiService {
        @GET("v1/browse/categories/{categoryId}/playlists")
        Call<PlaylistsModel> getPlaylists(@Header("Authorization") String authorization, @Path("categoryId") String categoryId);

        @GET("v1/playlists/{playlistId}")
        Call<PlaylistSimplified> getSongs(@Header("Authorization") String authorization, @Path("playlistId") String playlistId);
    }

    public static class PlaylistsModel {
        @SerializedName("message")
        private String message;

        public String getMessage() {
            return message;
        }

        @SerializedName("playlists")
        public Item Playlists;


        public static class Item {
            @SerializedName("items")
            public List<PlaylistAPI> PlaylistsArray;

        }
    }
}