package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobile_survey_application.R;

public class FragmentCategoryChange extends Fragment {

    private TextView txtCount;

    private CheckBox[] checkBoxes;

    public FragmentCategoryChange() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnSaveCategory).setOnClickListener(v -> {
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
}