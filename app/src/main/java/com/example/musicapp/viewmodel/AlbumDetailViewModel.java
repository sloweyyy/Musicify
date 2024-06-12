package com.example.musicapp.viewmodel;

import android.app.AlertDialog;
import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.fragment.AlbumDetailFragment;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.service.SpotifyApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
public class AlbumDetailViewModel extends AndroidViewModel {
    public AlbumDetailViewModel(@NonNull Application application) {
        super(application);
    }
    private MutableLiveData<List<Song>> _albumSongs = new MutableLiveData<>();
    public LiveData<List<Song>> albumSongs = _albumSongs;

    private MutableLiveData<AlbumSimplified> _albumDetails = new MutableLiveData<>();
    public LiveData<AlbumSimplified> albumDetails = _albumDetails;

    public void getAlbumSongs(String accessToken, String albumId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        Call<AlbumSimplified> call = apiService.getSongsOfAlbum(authorization, albumId);
        call.enqueue(new Callback<AlbumSimplified>() {
            @Override
            public void onResponse(Call<AlbumSimplified> call, Response<AlbumSimplified> response) {
                if (response.isSuccessful()) {
                    AlbumSimplified albumSimplified = response.body();
                    _albumDetails.setValue(albumSimplified);

                    List<Song> songs = new ArrayList<>();
                    for (SimplifiedTrack simplifiedTrack : albumSimplified.getTracksContainer().tracks) {
                        Song song = Song.fromSimplifiedTrack(simplifiedTrack);
                        song.setImageUrl(albumSimplified.getImages().get(0).getUrl());
                        songs.add(song);
                    }
                    _albumSongs.setValue(songs);
                } else {
                    // Handle error
                }
            }

            @Override
            public void onFailure(Call<AlbumSimplified> call, Throwable t) {
                // Handle error
            }
        });
    }
}
