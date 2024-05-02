package com.example.musicapp.fragment;
import androidx.annotation.*;
import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;

import com.example.musicapp.R;

public class FavouriteFragment extends Fragment{
    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_favourite, container,false);
        return view;
    }
}
