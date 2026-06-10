package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.util.BirthDateTextWatcher;
import com.example.mobile_survey_application.util.PhoneNumberTextWatcher;
import com.example.mobile_survey_application.util.ProfileImageHelper;
import com.example.mobile_survey_application.util.RegisterInputValidator;

import java.util.ArrayList;
import java.util.List;
import model.CategoryResponse;
import model.UserResponse;
import model.UserUpdateRequest;
import viewmodel.AuthViewModel;

public class FragmentProfileEdit extends Fragment {

    private static final String TAG_PROFILE_DEBUG = "PROFILE_DEBUG";

    private AuthViewModel authViewModel;
    private ImageView imgProfile;
    private TextView txtCategory;
    private EditText etName;
    private EditText etTelephone;
    private EditText etNickname;
    private EditText etBirthDate;
    private EditText etLocation;
    private EditText etOccupation;
    private String preservedRegion;
    private final List<CategoryResponse> categoryList = new ArrayList<>();

    public FragmentProfileEdit() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        imgProfile = view.findViewById(R.id.imgProfile);
        etName = view.findViewById(R.id.etProfileName);
        etTelephone = view.findViewById(R.id.etProfileTelephone);
        etNickname = view.findViewById(R.id.etProfileNickname);
        etBirthDate = view.findViewById(R.id.etProfileBirthDate);
        etLocation = view.findViewById(R.id.etProfileLocation);
        etOccupation = view.findViewById(R.id.etProfileOccupation);
        txtCategory = view.findViewById(R.id.txtCategory);

        setupInputValidation();

        authViewModel.loadMyProfile();
        authViewModel.loadCategories();
        authViewModel.resetProfileUpdateResult();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
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

        view.findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());

        return view;
    }

    private void setupInputValidation() {
        etName.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(20),
                RegisterInputValidator.nameInputFilter()
        });
        etNickname.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(12),
                RegisterInputValidator.nicknameInputFilter()
        });
        etTelephone.addTextChangedListener(new PhoneNumberTextWatcher(etTelephone));
        etBirthDate.addTextChangedListener(new BirthDateTextWatcher(etBirthDate));
        clearErrorOnChange(etName);
        clearErrorOnChange(etTelephone);
        clearErrorOnChange(etNickname);
        clearErrorOnChange(etBirthDate);
    }

    private void clearErrorOnChange(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void saveProfile() {
        clearFieldErrors();

        String name = etName.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        String nameError = RegisterInputValidator.validateName(name);
        if (nameError != null) {
            showFieldError(etName, nameError);
            return;
        }

        String telephoneError = RegisterInputValidator.validateTelephone(telephone);
        if (telephoneError != null) {
            showFieldError(etTelephone, telephoneError);
            return;
        }

        String nicknameError = RegisterInputValidator.validateNickname(nickname);
        if (nicknameError != null) {
            showFieldError(etNickname, nicknameError);
            return;
        }

        String birthDateError = RegisterInputValidator.validateBirthDate(birthDate);
        if (birthDateError != null) {
            showFieldError(etBirthDate, birthDateError);
            return;
        }

        Log.d(TAG_PROFILE_DEBUG,
                "FragmentProfileEdit save click birthDate=" + valueOrEmpty(birthDate));

        UserUpdateRequest request = new UserUpdateRequest(
                emptyToNull(name),
                emptyToNull(telephone),
                emptyToNull(nickname),
                emptyToNull(birthDate),
                emptyToNull(etLocation.getText().toString().trim()),
                emptyToNull(preservedRegion),
                emptyToNull(etOccupation.getText().toString().trim())
        );
        authViewModel.updateMyProfile(request);
    }

    private void clearFieldErrors() {
        etName.setError(null);
        etTelephone.setError(null);
        etNickname.setError(null);
        etBirthDate.setError(null);
    }

    private void showFieldError(EditText editText, String message) {
        editText.setError(message);
        editText.requestFocus();
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
        preservedRegion = user.getRegion();
        setText(etOccupation, user.getOccupation());
        ProfileImageHelper.loadProfileImage(this, imgProfile, user);
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
}