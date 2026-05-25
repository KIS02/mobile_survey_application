package com.example.mobile_survey_application.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobile_survey_application.R;

import model.UserResponse;
import viewmodel.AuthViewModel;

public class FragmentProfile extends Fragment {

    private AuthViewModel authViewModel;
    private TextView txtProfileName;
    private TextView txtProfileEmail;

    public FragmentProfile() {
        // Required empty public constructor
    }

    public static FragmentProfile newInstance(String param1, String param2) {
        FragmentProfile fragment = new FragmentProfile();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);

        authViewModel.getMyProfile().observe(getViewLifecycleOwner(), this::bindProfile);
        authViewModel.loadMyProfile();

        view.findViewById(R.id.cardProfile).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentProfileEdit())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.menuCoupon).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentCouponBox())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.menuPointHistory).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentPointHistory())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void bindProfile(UserResponse user) {
        if (user == null) {
            return;
        }

        txtProfileName.setText(valueOrDefault(user.getName(), "사용자"));
        txtProfileEmail.setText(valueOrDefault(user.getEmail(), "이메일 정보 없음"));
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value;
    }
}