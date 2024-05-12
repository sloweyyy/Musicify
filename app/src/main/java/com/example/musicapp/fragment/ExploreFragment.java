package com.example.musicapp.fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.fragment.CenterSpaceItemDecoration;
import com.example.musicapp.R;
import com.example.musicapp.adapter.exploreAdapter;

import com.example.musicapp.adapter.FetchAccessToken;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

import java.util.List;

import com.example.musicapp.model.Category;
import com.google.gson.Gson;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import org.json.*;

public class ExploreFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    View view;
    RecyclerView recyclerView;
    private FetchAccessToken fetchAccessToken;
    @Override
    public void onTokenReceived(String accessToken)
    {
        fetchCategories(accessToken);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_explore, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewExplore);
        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        recyclerView.setLayoutManager(layoutManager);
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }


    public interface SpotifyApiService {
        @GET("v1/categories/{categoryId}")
        Call<CategoryResponse> getCategory(
                @Header("Authorization") String authorization,
                @Path("categoryId") String categoryId
        );

        @GET("v1/browse/categories")
        Call<CategoryResponse> getCategories(
                @Header("Authorization") String authorization
        );

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
                return "CategoryResponse{" +
                        "categories=" + categories +
                        '}';
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
            return "Categories{" +
                    "href='" + href + '\'' +
                    ", items=" + items +
                    '}';
        }
    }
    public void fetchCategories(String accessToken) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApiService apiService = retrofit.create(SpotifyApiService.class);
        Call<CategoryResponse> call = apiService.getCategories("Bearer " + accessToken);
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {

                if (response.isSuccessful())
                {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    CategoryResponse categoryResponse = gson.fromJson(jsonResponse, CategoryResponse.class);
                    List<Category> items = categoryResponse.getCategories().getItems();
                    showCategories(items);

                } else {
                    try {
                        String errorBody = response.errorBody().string(); // Lấy thông tin lỗi từ phản hồi
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("API call fail")
                                .setMessage(errorBody)
                                .setPositiveButton("OK", null)
                                .show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                String errorMessage = t.getMessage();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("API call faile")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
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
