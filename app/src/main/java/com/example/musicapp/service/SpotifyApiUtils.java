package com.example.musicapp.service;

import com.example.musicapp.model.Artist;
import com.example.musicapp.model.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class SpotifyApiUtils {

    private static Retrofit retrofit;
    private static SpotifyApi spotifyApi;

    public static SpotifyApi getSpotifyApi(String accessToken) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/v1/") // Spotify API base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (spotifyApi == null) {
            spotifyApi = retrofit.create(SpotifyApi.class);
        }
        return spotifyApi;
    }

    public interface SpotifyApi {

        @GET("artists/{artistId}")
        Call<Artist> getArtist(@Header("Authorization") String authorization, @Path("artistId") String artistId);

        @GET("artists/{artistId}/albums")
        Call<List<Song>> getArtistAlbums(@Header("Authorization") String authorization, @Path("artistId") String artistId,
                                         @Query("include_groups") String includeGroups);
    }
}