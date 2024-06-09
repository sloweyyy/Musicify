package com.example.musicapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.activities.TermsConditionActivity;


public class TermsAndCondition extends Fragment {
View view;
    ImageButton iconBack;
    ScrollView tvPolicyContent;
    ProfileFragment profileFragment;
    public TermsAndCondition() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_terms_and_condition, container, false);
        iconBack = view.findViewById(R.id.iconBack);
        tvPolicyContent= view.findViewById(R.id.tv_policy_content);
        profileFragment=new ProfileFragment();

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();

            }
        });
        return view;
    }
}