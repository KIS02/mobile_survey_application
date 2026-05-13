package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.R;

public class FragmentProfileEdit extends Fragment {

    public FragmentProfileEdit() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.profileImageArea).setOnClickListener(v -> {
            Log.d("ProfileEdit", "프로필 이미지 클릭됨");
            showProfileImageDialog();
        });

        view.findViewById(R.id.btnCategory).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentCategoryChange())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void showProfileImageDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("프로필 이미지 수정")
                .setMessage("프로필 이미지를 변경하는 기능입니다.")
                .setPositiveButton("확인", null)
                .show();
    }
}