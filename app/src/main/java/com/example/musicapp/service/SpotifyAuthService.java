package com.example.musicapp.service;

import android.app.appsearch.SearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.example.musicapp.model.TokenResponse;
public interface SpotifyAuthService {
    @FormUrlEncoded
    @POST("token")
    Call<TokenResponse> getToken(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Field("grant_type") String grantType
    );

    @GET("v1/search")
    Call<SearchResult> searchTracks(@Header("Authorization") String authorization, @Query("q") String query, @Query("type") String type);
}