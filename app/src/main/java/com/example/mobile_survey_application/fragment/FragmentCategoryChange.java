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

    private TextView txtCount;

    private CheckBox[] checkBoxes;
    private Long[] categoryIds;

    private AuthViewModel authViewModel;

    public FragmentCategoryChange() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category_change, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        txtCount = view.findViewById(R.id.txtCount);

        CheckBox checkFood = view.findViewById(R.id.checkFood);
        CheckBox checkTravel = view.findViewById(R.id.checkTravel);
        CheckBox checkAnimal = view.findViewById(R.id.checkAnimal);
        CheckBox checkGame = view.findViewById(R.id.checkGame);
        CheckBox checkIT = view.findViewById(R.id.checkIT);
        CheckBox checkSports = view.findViewById(R.id.checkSports);
//임시로 구현되어있는 버전입니다. 추후에 서버를 통해 데이터를 받아오게 변경해야합니다.
        checkBoxes = new CheckBox[] {
                checkFood,
                checkTravel,
                checkAnimal,
                checkGame,
                checkIT,
                checkSports
        };

        categoryIds = new Long[] {
                1L, // Food
                2L, // Travel
                3L, // Animal
                4L, // Game
                5L, // IT
                6L  // Sports
        };

        restoreSelectedCategories();

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

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnSaveCategory).setOnClickListener(v -> {

            List<Long> selectedIds = getSelectedCategoryIds();

            if (selectedIds.isEmpty()) {
                Toast.makeText(
                        getContext(),
                        "카테고리를 1개 이상 선택해주세요.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            authViewModel.setSelectedCategoryIds(selectedIds);

            Toast.makeText(
                    getContext(),
                    "카테고리가 저장되었습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            getParentFragmentManager().popBackStack();
        });

        return view;
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

    private List<Long> getSelectedCategoryIds() {
        List<Long> selectedIds = new ArrayList<>();

        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isChecked()) {
                selectedIds.add(categoryIds[i]);
            }
        }

        return selectedIds;
    }

    private void restoreSelectedCategories() {
        List<Long> selectedIds = authViewModel.getSelectedCategoryIds().getValue();

        if (selectedIds == null) {
            txtCount.setText("0 / 3 선택됨");
            return;
        }

        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setChecked(selectedIds.contains(categoryIds[i]));
        }

        txtCount.setText(getCheckedCount() + " / 3 선택됨");
    }
}