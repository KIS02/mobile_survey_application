package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.SurveyActivity;

import java.util.ArrayList;

public class FragmentSurvey extends Fragment {

    private LinearLayout surveyListLayout;

    private ArrayList<SurveyItem> surveyItems;

    public FragmentSurvey() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_survey, container, false);

        surveyListLayout = view.findViewById(R.id.surveyListLayout);

        initSurveyData();
        createSurveyItems();

        return view;
    }

    //region ### Survey Option 생성부분 ###
    private void initSurveyData() {
        surveyItems = new ArrayList<>();

        surveyItems.add(new SurveyItem(
                "새 설문 제목",
                "새 설문 설명입니다.",
                10,
                new String[]{"태그1", "태그2"}
        ));

        surveyItems.add(new SurveyItem(
                "애완동물 관리, 어떻게 하시나요?",
                "반려동물 관리 습관에 관한 설문입니다.",
                30,
                new String[]{"애완동물"}
        ));

        surveyItems.add(new SurveyItem(
                "청소년들의 식습관에 관한 조사",
                "청소년의 식습관과 식생활 패턴에 관한 설문입니다.",
                20,
                new String[]{"10대", "음식"}
        ));

        surveyItems.add(new SurveyItem(
                "게임 이용 습관에 관한 설문",
                "게임 이용 시간과 선호 장르에 관한 설문입니다.",
                15,
                new String[]{"게임", "취미"}
        ));

        surveyItems.add(new SurveyItem(
                "여행 선호도 조사",
                "여행 방식과 선호 여행지에 관한 설문입니다.",
                25,
                new String[]{"여행"}
        ));
    }

    private void createSurveyItems() {
        surveyListLayout.removeAllViews();

        for (int i = 0; i < surveyItems.size(); i++) {
            SurveyItem item = surveyItems.get(i);

            View surveyCard = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_survey_card, surveyListLayout, false);

            TextView titleText = surveyCard.findViewById(R.id.surveyTitleText);
            TextView pointText = surveyCard.findViewById(R.id.surveyPointText);
            LinearLayout tagLayout = surveyCard.findViewById(R.id.surveyTagLayout);
            FrameLayout itemSurveyCard = surveyCard.findViewById(R.id.itemSurveyCard);

            titleText.setText(item.title);
            pointText.setText(item.point + " points ✨");

            tagLayout.removeAllViews();

            for (String tag : item.tags) {
                TextView tagText = createTagTextView(tagLayout, tag);
                tagLayout.addView(tagText);
            }

            itemSurveyCard.setOnClickListener(v -> {
                showSurveyDialog(
                        item.title,
                        item.description,
                        "+" + item.point + "P 적립"
                );
            });

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(92)
                    );

            if (i != 0) {
                cardParams.topMargin = dp(12);
            }

            surveyCard.setLayoutParams(cardParams);
            surveyListLayout.addView(surveyCard);
        }
    }

    private TextView createTagTextView(LinearLayout parent, String tag) {
        TextView tagText = (TextView) LayoutInflater.from(requireContext())
                .inflate(R.layout.item_survey_tag, parent, false);

        tagText.setText(tag);

        return tagText;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class SurveyItem {
        String title;
        String description;
        int point;
        String[] tags;

        SurveyItem(String title, String description, int point, String[] tags) {
            this.title = title;
            this.description = description;
            this.point = point;
            this.tags = tags;
        }
    }
    //endregion

    //region ### Survey선택했을 때, ###
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
    //endregion




}