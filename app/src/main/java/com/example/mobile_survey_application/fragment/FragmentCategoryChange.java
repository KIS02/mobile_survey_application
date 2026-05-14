package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.widget.CheckBox;
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

import viewmodel.AuthViewModel;

public class FragmentCategoryChange extends Fragment {
    private AuthViewModel authViewModel;

    private CheckBox checkFood, checkTravel, checkAnimal, checkGame, checkIT, checkSports;
    private TextView txtCount;

    private CheckBox[] checkBoxes;

    public FragmentCategoryChange() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        View view = inflater.inflate(R.layout.fragment_category_change, container, false);

        txtCount = view.findViewById(R.id.txtCount);

        CheckBox checkFood = view.findViewById(R.id.checkFood);
        CheckBox checkTravel = view.findViewById(R.id.checkTravel);
        CheckBox checkAnimal = view.findViewById(R.id.checkAnimal);
        CheckBox checkGame = view.findViewById(R.id.checkGame);
        CheckBox checkIT = view.findViewById(R.id.checkIT);
        CheckBox checkSports = view.findViewById(R.id.checkSports);

        checkBoxes = new CheckBox[] {
                checkFood,
                checkTravel,
                checkAnimal,
                checkGame,
                checkIT,
                checkSports
        };

        for (CheckBox checkBox : checkBoxes) {

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                int count = getCheckedCount();

                if (count > 3) {
                    checkBox.setChecked(false);

                    Toast.makeText(
                            getContext(),
                            "최대 3개까지 선택 가능합니다.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                txtCount.setText(count + " / 3 선택됨");
            });
        }

        // 이전에 선택했던 카테고리 복원
        restoreSelectedCategories(authViewModel.getSelectedCategories().getValue());

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnSaveCategory).setOnClickListener(v -> {

            List<String> selected = new ArrayList<>();

            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    selected.add(checkBox.getText().toString());
                }
            }
            authViewModel.setSelectedCategories(selected);

            // 저장한 카테고리가 ["음식", "동물"] 처럼 AuthViewModel의 selectedCategories에 저장됩니다.

            Toast.makeText(
                    getContext(),
                    "카테고리가 저장되었습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void restoreSelectedCategories(List<String> selectedCategories) {
        if (selectedCategories == null) return;

        for (CheckBox checkBox : checkBoxes) {
            String categoryName = checkBox.getText().toString();
            checkBox.setChecked(selectedCategories.contains(categoryName));
        }
    }

    private int getCheckedCount() {

        int count = 0;

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                count++;
            }
        }

        return count;
    }
}