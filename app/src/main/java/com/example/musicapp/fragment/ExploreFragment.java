package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.exploreAdapter;
import com.example.musicapp.model.Category;
import com.example.musicapp.model.TokenResponse;
import com.example.musicapp.service.SpotifyAuthService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class ExploreFragment extends Fragment {
    View view;

    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_explore, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewExplore);
        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        recyclerView.setLayoutManager(layoutManager);

        getTokenFromSpotify(new AccessTokenCallback() {
            @Override
            public void onTokenReceived(String accessToken) {
                fetchCategories(accessToken);
            }
        });
        return view;
    }

    public String getTokenFromSpotify(final AccessTokenCallback callback) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://accounts.spotify.com/api/").addConverterFactory(GsonConverterFactory.create()).build();

        SpotifyAuthService authService = retrofit.create(SpotifyAuthService.class);
        final AccessTokenWrapper accessTokenWrapper = new AccessTokenWrapper();
        String credentials = "19380ddfc0344af29cb61de3c6655fda:1b0bf947882f4b89a1705cc65443ae9c";
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
                        Toast.makeText(getActivity(), "Access Token: " + accessToken, Toast.LENGTH_SHORT).show();
                        accessTokenWrapper.setAccessToken(accessToken);
                        callback.onTokenReceived(accessToken);
                    }

                } else {
                    try {
                        String errorBody = response.errorBody().string(); // Lấy thông tin lỗi từ phản hồi
                        Toast.makeText(getActivity(), "API Call failed: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                String errorMessage = t.getMessage(); // Lấy thông báo lỗi
                Toast.makeText(getActivity(), "API Call failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        return accessTokenWrapper.getAccessToken();
    }

    public static class AccessTokenWrapper {
        private String accessToken;

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

    public interface SpotifyApiService {
        @GET("v1/categories/{categoryId}")
        Call<CategoryResponse> getCategory(@Header("Authorization") String authorization, @Path("categoryId") String categoryId);

        @GET("v1/browse/categories")
        Call<CategoryResponse> getCategories(@Header("Authorization") String authorization);

    }

    public class CategoryResponse {
        private Categories categories;

        public Categories getCategories() {
            return categories;
        }

        public void setCategories(Categories categories) {
            this.categories = categories;
        }

        @Override
        public String toString() {
            return "CategoryResponse{" + "categories=" + categories + '}';
        }
    }

    public class Categories {
        private String href;
        private List<Category> items;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public List<Category> getItems() {
            return items;
        }

        public void setItems(List<Category> items) {
            this.items = items;
        }

        @Override
        public String toString() {
            return "Categories{" + "href='" + href + '\'' + ", items=" + items + '}';
        }
    }

    public void fetchCategories(String accessToken) {

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();

        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        Call<CategoryResponse> call = apiService.getCategories("Bearer " + accessToken);
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {

                if (response.isSuccessful()) {


                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    CategoryResponse categoryResponse = gson.fromJson(jsonResponse, CategoryResponse.class);
                    List<Category> items = categoryResponse.getCategories().getItems();
                    showCategories(items);

                } else {
                    try {
                        String errorBody = response.errorBody().string(); // Lấy thông tin lỗi từ phản hồi
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("API call fail").setMessage(errorBody).setPositiveButton("OK", null).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                String errorMessage = t.getMessage();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("API call faile").setMessage(errorMessage).setPositiveButton("OK", null).show();
            }
        });
    }

    public void showCategories(List<Category> categories) {
        if (categories != null) {
            exploreAdapter adapter = new exploreAdapter(categories);
            recyclerView.setAdapter(adapter);
        } else {
            // Xử lý khi danh sách categories là null, ví dụ: hiển thị thông báo lỗi
            Toast.makeText(getContext(), "Không có danh sách categories", Toast.LENGTH_SHORT).show();
        }
    }

    public interface AccessTokenCallback {
        void onTokenReceived(String accessToken);
    }
}
