package com.example.musicapp.service;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.example.musicapp.model.TokenResponse;
public interface SpotifyAuthService {
    @FormUrlEncoded
    @POST("token")
    Call<TokenResponse> getToken(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Field("grant_type") String grantType
    );
}