package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.SurveyActivity;

public class FragmentSurvey extends Fragment {

    private LinearLayout layoutSurvey1;
    private LinearLayout layoutSurvey2;

    public FragmentSurvey() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_survey, container, false);

        layoutSurvey1 = view.findViewById(R.id.layoutSurvey1);
        layoutSurvey2 = view.findViewById(R.id.layoutSurvey2);

        layoutSurvey1.setOnClickListener(v -> {
            showSurveyDialog(
                    "애완동물 관리, 어떻게 하시나요?",
                    "반려동물 관리 습관에 관한 설문입니다.",
                    "+30P 적립"
            );
        });

        layoutSurvey2.setOnClickListener(v -> {
            showSurveyDialog(
                    "청소년들의 식습관에 관한 조사",
                    "청소년들의 식습관 패턴을 파악하기 위한 설문입니다.",
                    "+20P 적립"
            );
        });

        return view;
    }

    private void showSurveyDialog(String title, String description, String reward) {

        new AlertDialog.Builder(requireContext())
                .setTitle("설문 정보")
                .setMessage(
                        title +
                                "\n\n" +
                                description +
                                "\n\n소요 시간 : 약 1~2분\n" +
                                reward
                )
                .setNegativeButton("취소", null)
                .setPositiveButton("시작하기", (dialog, which) -> {

                    Intent intent = new Intent(requireContext(), SurveyActivity.class);
                    startActivity(intent);

                })
                .show();
    }
}