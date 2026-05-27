package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.R;

import java.util.ArrayList;
import java.util.List;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import model.CategoryResponse;
import model.UserResponse;
import model.UserUpdateRequest;
import viewmodel.AuthViewModel;

public class FragmentProfileEdit extends Fragment {

    private static final String TAG_PROFILE_DEBUG = "PROFILE_DEBUG";

    private AuthViewModel authViewModel;
    private TextView txtCategory;
    private EditText etName;
    private EditText etTelephone;
    private EditText etNickname;
    private EditText etBirthDate;
    private EditText etLocation;
    private EditText etRegion;
    private final List<CategoryResponse> categoryList = new ArrayList<>();

    public FragmentProfileEdit() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        etName = view.findViewById(R.id.etProfileName);
        etTelephone = view.findViewById(R.id.etProfileTelephone);
        etNickname = view.findViewById(R.id.etProfileNickname);
        etBirthDate = view.findViewById(R.id.etProfileBirthDate);
        etLocation = view.findViewById(R.id.etProfileLocation);
        etRegion = view.findViewById(R.id.etProfileRegion);
        txtCategory = view.findViewById(R.id.txtCategory);

        authViewModel.loadMyProfile();
        authViewModel.loadCategories();
        authViewModel.resetProfileUpdateResult();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.profileImageArea).setOnClickListener(v -> {
            Log.d("ProfileEdit", "프로필 이미지 클릭됨");
            showProfileImageDialog();
        });

        view.findViewById(R.id.btnCategory).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("mode", FragmentCategoryChange.MODE_PROFILE_EDIT);

            FragmentCategoryChange fragment = new FragmentCategoryChange();
            fragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        authViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            if (categories != null) {
                categoryList.addAll(categories);
            }
            updateCategoryText();
        });

        authViewModel.getSelectedCategoryIds().observe(getViewLifecycleOwner(), selectedIds ->
                updateCategoryText()
        );

        authViewModel.getMyProfile().observe(getViewLifecycleOwner(), this::bindProfile);

        authViewModel.getProfileUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (!Boolean.TRUE.equals(success) || !isAdded()) {
                return;
            }

            authViewModel.resetProfileUpdateResult();
            Toast.makeText(requireContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            authViewModel.loadMyProfile();
        });

        authViewModel.getProfileUpdateError().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty() || !isAdded()) {
                return;
            }

            authViewModel.resetProfileUpdateResult();
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });

        view.findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            String birthDate = etBirthDate.getText().toString().trim();
            Log.d(TAG_PROFILE_DEBUG,
                    "FragmentProfileEdit save click birthDate=" + valueOrEmpty(birthDate));
            UserUpdateRequest request = new UserUpdateRequest(
                    emptyToNull(etName.getText().toString().trim()),
                    emptyToNull(etTelephone.getText().toString().trim()),
                    emptyToNull(etNickname.getText().toString().trim()),
                    emptyToNull(birthDate),
                    emptyToNull(etLocation.getText().toString().trim()),
                    emptyToNull(etRegion.getText().toString().trim())
            );
            authViewModel.updateMyProfile(request);
        });

        return view;
    }

    private void bindProfile(UserResponse user) {
        if (user == null) {
            return;
        }

        setText(etName, user.getName());
        setText(etTelephone, user.getTelephone());
        setText(etNickname, user.getNickname());
        setText(etBirthDate, user.getBirthDate());
        setText(etLocation, user.getLocation());
        setText(etRegion, user.getRegion());
    }

    private void updateCategoryText() {
        if (txtCategory == null) {
            return;
        }

        List<Long> selectedIds = authViewModel.getSelectedCategoryIds().getValue();
        if (selectedIds == null || selectedIds.isEmpty() || categoryList.isEmpty()) {
            txtCategory.setText("선택된 카테고리: 없음");
            return;
        }

        List<String> selectedNames = new ArrayList<>();
        for (CategoryResponse category : categoryList) {
            if (selectedIds.contains(category.getId())) {
                selectedNames.add(category.getName());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedNames.size(); i++) {
            sb.append(selectedNames.get(i));
            if (i < selectedNames.size() - 1) {
                sb.append(", ");
            }
        }

        txtCategory.setText(sb.toString());
    }

    private void setText(EditText editText, String value) {
        editText.setText(value == null ? "" : value);
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private String valueOrEmpty(String value) {
        return value.isEmpty() ? "(empty)" : value;
    }

    private void showProfileImageDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("프로필 이미지 수정")
                .setMessage("프로필 이미지를 변경하는 기능입니다.")
                .setPositiveButton("확인", null)
                .show();
    }
}