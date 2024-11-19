package com.example.musicapp.service;


import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.Artist;
import com.example.musicapp.model.CategoryResponse;
import com.example.musicapp.model.SearchResult;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.viewmodel.ArtistDetailViewModel;
import com.example.musicapp.viewmodel.PlaylistDetailAPIViewModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyApiService {
    @GET("v1/categories/{categoryId}")
    Call<CategoryResponse> getCategory(@Header("Authorization") String authorization, @Path("categoryId") String categoryId);

    @GET("v1/browse/categories")
    Call<CategoryResponse> getCategories(@Header("Authorization") String authorization);

    @GET("v1/search")
    Call<SearchResult> searchTracks(@Header("Authorization") String authorization, @Query("q") String query, @Query("type") String type);

    @GET("v1/tracks/{songId}")
    Call<SimplifiedTrack> getTrack(@Header("Authorization") String authorization, @Path("songId") String songId);

    @GET("v1/playlists/{playlistId}")
    Call<PlaylistDetailAPIViewModel.PlaylistSimplified> getSongs(@Header("Authorization") String authorization, @Path("playlistId") String playlistId);

    @GET("v1/albums/{albumId}")
    Call<AlbumSimplified> getAlbum(@Header("Authorization") String authorization, @Path("albumId") String albumId);

    @GET("v1/albums/{albumId}")
    Call<AlbumSimplified> getSongsOfAlbum(@Header("Authorization") String authorization, @Path("albumId") String albumId);
    @GET("v1/artists/{artistId}")
    Call<Artist> getArtist(@Header("Authorization") String authorization, @Path("artistId") String artistId);

    @GET("v1/artists/{artistId}/albums")
    Call<ArtistDetailViewModel.ArtistAlbums> getArtistAlbums(@Header("Authorization") String authorization, @Path("artistId") String artistId);

    @GET("v1/artists/{artistId}/top-tracks")
    Call<ArtistDetailViewModel.ArtistTopTrack> getArtistTopTrack(@Header("Authorization") String authorization, @Path("artistId") String artistId);

}