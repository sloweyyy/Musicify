package com.example.musicapp.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.musicapp.model.TokenResponse;
import com.example.musicapp.service.SpotifyAuthService;
import java.io.IOException;
import java.util.Base64;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FetchAccessToken
{
    private String accessToken;
    private long tokenExpirationTime;

    private void setAccessTokenExpiration(long expiresIn) {
        long currentTimeMillis = System.currentTimeMillis();
        tokenExpirationTime = currentTimeMillis + expiresIn * 1000; // Chuyển đổi từ giây sang mili giây
    }

    public void getTokenFromSpotify(final AccessTokenCallback callback)
    {
        if (accessToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            // Access token còn hạn, truyền vào callback ngay lập tức
            callback.onTokenReceived(accessToken);
            return;
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://accounts.spotify.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyAuthService authService = retrofit.create(SpotifyAuthService.class);
        AccessTokenWrapper accessTokenWrapper = new AccessTokenWrapper();
        String credentials = "b5f0a6c3766c4b289bcf0fb3e6e94242:f24468c86f82426baac220142d3e0451";
        String authoToken = Base64.getEncoder().encodeToString(credentials.getBytes());
        String authorization = "Basic " + authoToken;
        String grantType = "client_credentials";
        String contentType = "application/x-www-form-urlencoded";

        Call<TokenResponse> call = authService.getToken(authorization, contentType, grantType);
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful()) {
                    TokenResponse tokenResponse = response.body();
                    if (tokenResponse != null) {
                        String accessToken = tokenResponse.accessToken;
                        long expiresIn = tokenResponse.expiresIn; // Thời gian hết hạn tính bằng giây
                        accessTokenWrapper.setAccessToken(accessToken);
                        setAccessTokenExpiration(expiresIn); // Cập nhật thời gian hết hạn
                        callback.onTokenReceived(accessToken);
                    }

                } else {
                    try {
                        String errorBody = response.errorBody().string(); // Lấy thông tin lỗi từ phản hồi
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                String errorMessage = t.getMessage(); // Lấy thông báo lỗi
            }
        });
//        return accessTokenWrapper.getAccessToken();
    }
    public interface AccessTokenCallback {
        void onTokenReceived(String accessToken);

        @Nullable
        View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
    }

    public static class AccessTokenWrapper
    {
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

}
