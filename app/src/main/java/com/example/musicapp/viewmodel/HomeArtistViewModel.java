package com.example.musicapp.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicapp.fragment.HomeArtistFragment;
import com.example.musicapp.model.Artist;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeArtistViewModel extends ViewModel {
    private final MutableLiveData<List<Artist>> artistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private HomeArtistFragment.SpotifyApiService apiService;
    private List<String> artistIdArray;

    public HomeArtistViewModel() {
        artistIdArray = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(HomeArtistFragment.SpotifyApiService.class);

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
    }

    public LiveData<List<Artist>> getArtistsLiveData() {
        return artistsLiveData;
    }

    public LiveData<Boolean> getIsUpdating() {
        return isUpdating;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void fetchArtists(String accessToken) {
        isUpdating.setValue(true);
        String authorization = "Bearer " + accessToken;
        List<Artist> allArtists = new ArrayList<>();
        final int[] remainingCalls = {artistIdArray.size()};

        for (String artistId : artistIdArray) {
            Call<HomeArtistFragment.ArtistsResponse> call = apiService.getRelatedArtists(authorization, artistId);
            call.enqueue(new Callback<HomeArtistFragment.ArtistsResponse>() {
                @Override
                public void onResponse(Call<HomeArtistFragment.ArtistsResponse> call, Response<HomeArtistFragment.ArtistsResponse> response) {
                    if (response.isSuccessful()) {
                        List<Artist> artistList = response.body().getArtists();
                        allArtists.addAll(artistList);
                        if (--remainingCalls[0] == 0) {
                            artistsLiveData.postValue(allArtists);
                            isUpdating.postValue(false);
                        }
                    } else {
                        errorLiveData.postValue("Failed to fetch artists: " + response.message());
                        isUpdating.postValue(false);
                    }
                }

                @Override
                public void onFailure(Call<HomeArtistFragment.ArtistsResponse> call, Throwable throwable) {
                    errorLiveData.postValue("Error fetching artists: " + throwable.getMessage());
                    isUpdating.postValue(false);
                }
            });
        }
    }
}
