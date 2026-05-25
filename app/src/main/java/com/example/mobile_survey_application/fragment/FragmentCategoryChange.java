package com.example.mobile_survey_application.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobile_survey_application.R;

import java.util.ArrayList;
import java.util.List;

import model.CategoryResponse;
import viewmodel.AuthViewModel;

public class FragmentCategoryChange extends Fragment {

    private static final String ARG_MODE = "mode";
    public static final String MODE_REGISTER = "register";
    public static final String MODE_PROFILE_EDIT = "profile_edit";
    private static final String TAG_CATEGORY_DEBUG = "CATEGORY_DEBUG";
    private static final int MAX_CATEGORY_COUNT = 3;

    private TextView txtCount;
    private LinearLayout categoryContainer;
    private AuthViewModel authViewModel;
    private String mode = MODE_REGISTER;
    private List<CategoryResponse> currentCategories = new ArrayList<>();

    public FragmentCategoryChange() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category_change, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, MODE_REGISTER);
        }

        txtCount = view.findViewById(R.id.txtCount);
        categoryContainer = (LinearLayout) view.findViewById(R.id.checkFood).getParent();
        categoryContainer.removeAllViews();

        observeCategories();
        observeSelectedCategoryIds();
        observeCategoryUpdateResult();
        authViewModel.loadCategories();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnSaveCategory).setOnClickListener(v -> {

            List<Long> selectedIds = getSelectedCategoryIds();

            if (selectedIds.size() != MAX_CATEGORY_COUNT) {
                Toast.makeText(
                        getContext(),
                        "카테고리를 " + MAX_CATEGORY_COUNT + "개 선택해주세요.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (MODE_PROFILE_EDIT.equals(mode)) {
                Log.d(TAG_CATEGORY_DEBUG,
                        "profile category save click selectedIds=" + selectedIds);
                authViewModel.updateMyCategories(selectedIds);
            } else {
                Log.d(TAG_CATEGORY_DEBUG,
                        "signup category selectedIds=" + selectedIds);
                authViewModel.setSelectedCategoryIds(selectedIds);
                Toast.makeText(
                        getContext(),
                        "카테고리가 저장되었습니다.",
                        Toast.LENGTH_SHORT
                ).show();
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void observeCategories() {
        authViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            currentCategories = categories == null ? new ArrayList<>() : categories;
            renderCategories(authViewModel.getSelectedCategoryIds().getValue());
        });
    }

    private void observeSelectedCategoryIds() {
        authViewModel.getSelectedCategoryIds().observe(getViewLifecycleOwner(), selectedIds -> {
            if (!currentCategories.isEmpty()) {
                renderCategories(selectedIds);
            }
        });
    }

    private void observeCategoryUpdateResult() {
        if (!MODE_PROFILE_EDIT.equals(mode)) {
            return;
        }

        authViewModel.resetCategoryUpdateResult();

        authViewModel.getCategoryUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (!Boolean.TRUE.equals(success) || !isAdded()) {
                return;
            }

            authViewModel.resetCategoryUpdateResult();
            Toast.makeText(requireContext(), "카테고리가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });

        authViewModel.getCategoryUpdateError().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty() || !isAdded()) {
                return;
            }

            authViewModel.resetCategoryUpdateResult();
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void renderCategories(List<Long> selectedIds) {
        categoryContainer.removeAllViews();

        for (CategoryResponse category : currentCategories) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(category.getName());
            checkBox.setTextSize(15);
            checkBox.setTextColor(0xFF222222);
            checkBox.setGravity(android.view.Gravity.CENTER_VERTICAL);
            checkBox.setPadding(dp(14), 0, dp(14), 0);
            checkBox.setBackgroundResource(R.drawable.bg_category_item);
            checkBox.setButtonTintList(ColorStateList.valueOf(0xFF7B61FF));
            checkBox.setTag(category.getId());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(52)
            );
            params.setMargins(0, 0, 0, dp(12));
            checkBox.setLayoutParams(params);

            if (selectedIds != null && selectedIds.contains(category.getId())) {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int count = getCheckedCount();

                if (count > MAX_CATEGORY_COUNT) {
                    checkBox.setChecked(false);
                    Toast.makeText(
                            getContext(),
                            "최대 " + MAX_CATEGORY_COUNT + "개까지 선택 가능합니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                txtCount.setText(count + " / " + MAX_CATEGORY_COUNT + " 선택됨");
            });

            categoryContainer.addView(checkBox);
        }

        txtCount.setText(getCheckedCount() + " / " + MAX_CATEGORY_COUNT + " 선택됨");
    }

    private int getCheckedCount() {
        int count = 0;

        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            if (categoryContainer.getChildAt(i) instanceof CheckBox
                    && ((CheckBox) categoryContainer.getChildAt(i)).isChecked()) {
                count++;
            }
        }

        return count;
    }

    private List<Long> getSelectedCategoryIds() {
        List<Long> selectedIds = new ArrayList<>();

        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            if (categoryContainer.getChildAt(i) instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) categoryContainer.getChildAt(i);
                if (checkBox.isChecked()) {
                    selectedIds.add((Long) checkBox.getTag());
                }
            }
        }

        return selectedIds;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}