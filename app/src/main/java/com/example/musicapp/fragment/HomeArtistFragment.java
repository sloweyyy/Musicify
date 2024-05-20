package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.database.Observable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicapp.R;
import com.example.musicapp.adapter.ArtitstHomeAdapter;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.model.Artist;
import com.example.musicapp.model.PlaylistSimplified;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class HomeArtistFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    RecyclerView artist_recyclerView;
    private View view;
    private FetchAccessToken fetchAccessToken;
    private ArtitstHomeAdapter artitstHomeAdapter;
    private String accesstoken;
    List<String> artistIdArray ;

    public HomeArtistFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_artist, container, false);
        artist_recyclerView = view.findViewById(R.id.artist_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        artist_recyclerView.setLayoutManager(layoutManager);
        artistIdArray = new ArrayList<>();

        //add id
        artistIdArray.add("2CIMQHirSU0MQqyYHq0eOx");
        artistIdArray.add("57dN52uHvrHOxijzpIgu3E");
        artistIdArray.add("1vCWHaC5f2uS3yhpwWbIA6");
        artistIdArray.add("5HZtdKfC4xU0wvhEyYDWiY");
        artistIdArray.add("06HL4z0CvFAxyc27GXpf02");
        artistIdArray.add("3diftVOq7aEIebXKkC34oR");
        artistIdArray.add("6d0dLenjy5CnR5ZMn2agiV");
        artistIdArray.add("41MozSoPIsD1dJM0CLPjZF");
        artistIdArray.add("6M2wZ9GZgrQXHCFfjv46we");
        artistIdArray.add("1Xyo4u8uXC1ZmMpatF05PJ");
        artistIdArray.add("1McMsnEElThX1knmY4oliG");
        artistIdArray.add("6qqNVTkY8uBg9cP3Jd7DAH");
        artistIdArray.add("0du5cEVh5yTK9QJze8zA0C");
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accesstoken=accessToken;
        Log.d("AccessToken", "Token: " + accessToken);
        getArtist(accesstoken);
    }
    public void getArtist (String accessToken)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        HomeArtistFragment.SpotifyApiService apiService = retrofit.create(HomeArtistFragment.SpotifyApiService.class);
        String authorization = "Bearer " + accessToken;
        String artistIds = String.join(",", artistIdArray);
        List<Artist> allArtists = new ArrayList<>();
        final int[] remainingCalls = { artistIdArray.size() };

        for (String artistId : artistIdArray) {
            Call<ArtistsResponse> call = apiService.getRelatedArtists(authorization, artistId);
             call.enqueue(new Callback<ArtistsResponse>() {
                 @Override
                 public void onResponse(Call<ArtistsResponse> call, Response<ArtistsResponse> response) {
                     if (response.isSuccessful()) {
                         Log.d("HUHUHUHU", "Token: " + accessToken);
                         List<Artist> artistList = response.body().getArtists();
                         if(artistList.isEmpty()){
                             Log.d("Oh no", "Empty artist list");
                         }else {
                             Artist firstArtist = artistList.get(0);
                             Log.d("First Artist", "Name: "+ firstArtist.getName() + ", ID: "  );
                         }
                         allArtists.addAll(artistList);
                         if (--remainingCalls[0] == 0) {
                             artitstHomeAdapter  = new ArtitstHomeAdapter(getContext(), allArtists);
                             artist_recyclerView.setAdapter(artitstHomeAdapter);
                         }

                     }
                 }

                 @Override
                 public void onFailure(Call<ArtistsResponse> call, Throwable throwable) {
                     Log.d("HEHEHHEHEE", "Token: " + accessToken);
                     AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage(throwable.getMessage());
                    alertDialog.setPositiveButton("OK", null);
                    AlertDialog dialog = alertDialog.create();
                    dialog.show();
                 }
             });
        }
//        ArtitstHomeAdapter adapter = new ArtitstHomeAdapter(getContext(), allArtists);
//        artist_recyclerView.setAdapter(adapter);
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