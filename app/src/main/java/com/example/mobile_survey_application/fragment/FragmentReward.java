package com.example.mobile_survey_application.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobile_survey_application.R;

public class FragmentReward extends Fragment {

    public FragmentReward() {
        // Required empty public constructor
    }

    public static FragmentReward newInstance(String param1, String param2) {
        FragmentReward fragment = new FragmentReward();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reward, container, false);

        view.findViewById(R.id.btnUsePoint).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentPointStore())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}