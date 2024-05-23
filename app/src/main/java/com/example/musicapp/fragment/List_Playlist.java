package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.PlaylistAdapter;
import com.example.musicapp.adapter.PlaylistAdapterAPI;
import com.example.musicapp.adapter.exploreAdapter;
import com.example.musicapp.model.PlaylistAPI;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class List_Playlist extends Fragment implements FetchAccessToken.AccessTokenCallback {
    View view;
    RecyclerView recyclerView;

    private String categoryId;

    private PlaylistAdapter adapter;
    private LinearLayout backButtonLayout;
    private Button iconBack;
    TextView header;
    private FetchAccessToken fetchAccessToken;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_playlist, container, false);
        recyclerView = view.findViewById(R.id.playlistRecyclerView);
        header = view.findViewById(R.id.PlaylistHeaderName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        backButtonLayout = view.findViewById(R.id.backButtonLayout);
        iconBack = view.findViewById(R.id.iconBack);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        return view;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        getPlaylists (accessToken);
    }

    public void getPlaylists (String accessToken){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();
        List_Playlist.SpotifyApiService apiService = retrofit.create(List_Playlist.SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<List_Playlist.PlaylistsModel> call = apiService.getPlaylists(authorization, categoryId);
        call.enqueue(new Callback<List_Playlist.PlaylistsModel>() {
            @Override
            public void onResponse(Call<PlaylistsModel> call, Response<PlaylistsModel> response) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                if (response.isSuccessful()) {
                    List_Playlist.PlaylistsModel Playlists = response.body();
                    header.setText(Playlists.message);
                    if (Playlists != null) {
                        ShowPlaylist(Playlists.Playlists.PlaylistsArray);
                    }
                } else {
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.body().message);
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }
            @Override
            public void onFailure(Call<PlaylistsModel> call, Throwable throwable) {

            }
        });

    }

    public void ShowPlaylist(List<PlaylistAPI> playlists){
        if (playlists != null) {
            PlaylistAdapterAPI adapter = new PlaylistAdapterAPI(playlists);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(getContext(), "Không có danh sách categories", Toast.LENGTH_SHORT).show();
        }

    }
    public interface SpotifyApiService {
        @GET("v1/browse/categories/{categoryId}/playlists")
        Call<List_Playlist.PlaylistsModel> getPlaylists(@Header("Authorization") String authorization, @Path("categoryId") String categoryId);
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
    public void getPlaylist(String accessToken){
    }
}
