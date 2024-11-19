package com.example.musicapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.adapter.ArtitstHomeAdapter;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.model.Artist;
import com.example.musicapp.viewmodel.HomeArtistViewModel;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class HomeArtistFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    RecyclerView artist_recyclerView;
    List<String> artistIdArray;
    private View view;
    private FetchAccessToken fetchAccessToken;
    private ArtitstHomeAdapter artitstHomeAdapter;
    private HomeArtistViewModel homeArtistViewModel;
    private String accesstoken;

    public HomeArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeArtistViewModel = new ViewModelProvider(this).get(HomeArtistViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_artist, container, false);
        artist_recyclerView = view.findViewById(R.id.artist_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        artist_recyclerView.setLayoutManager(layoutManager);
        artistIdArray = new ArrayList<>();
        homeArtistViewModel.getArtistsLiveData().observe(getViewLifecycleOwner(), artists -> {
            // Update UI with artists
            artitstHomeAdapter = new ArtitstHomeAdapter(getContext(), artists);
            artist_recyclerView.setAdapter(artitstHomeAdapter);
        });

        homeArtistViewModel.getIsUpdating().observe(getViewLifecycleOwner(), isUpdating -> {
        });

        homeArtistViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
        });


        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accesstoken = accessToken;
        Log.d("AccessToken", "Token: " + accessToken);
//        getArtist(accesstoken);
        homeArtistViewModel.fetchArtists(accessToken);

    }

    public interface SpotifyApiService {

        @GET("v1/artists/{id}/related-artists")
        Call<ArtistsResponse> getRelatedArtists(
                @Header("Authorization") String authorization,
                @Path("id") String artistId
        );
    }

    public class ArtistsResponse {
        @SerializedName("artists")
        private List<Artist> artists;

        // Getters and setters
        public List<Artist> getArtists() {
            return artists;
        }

        public void setArtists(List<Artist> artists) {
            this.artists = artists;
        }
    }
}