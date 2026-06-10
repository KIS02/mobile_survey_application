package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.SurveyActivity;
import com.example.mobile_survey_application.util.HeaderSettingsHelper;

import java.util.List;

import data.local.TokenManager;
import model.SurveyResponse;
import viewmodel.SurveyViewModel;

public class FragmentSurvey extends Fragment {

    public static final String EXTRA_SURVEY_ID = "surveyId";
    private static final String TAG_SURVEY_LIST_DEBUG = "SURVEY_LIST_DEBUG";

    private LinearLayout surveyListLayout;
    private TokenManager tokenManager;
    private SurveyViewModel surveyViewModel;

    public FragmentSurvey() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_survey, container, false);

        surveyListLayout = view.findViewById(R.id.surveyListLayout);
        HeaderSettingsHelper.bindNavigateToSetting(view, this);

        tokenManager = new TokenManager(requireContext());
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);
        observeViewModel();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSurveys();
    }

    private void observeViewModel() {
        surveyViewModel.getSurveyList().observe(getViewLifecycleOwner(), this::renderSurveyList);

        surveyViewModel.getSurveyListError().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty()) {
                return;
            }
            Log.e(TAG_SURVEY_LIST_DEBUG,
                    "UI failure state: loadSurveys failed, message=" + message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSurveys() {
        if (tokenManager == null) {
            return;
        }

        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG_SURVEY_LIST_DEBUG, "loadSurveys request");
        surveyViewModel.loadSurveys(accessToken);
    }

    private void renderSurveyList(List<SurveyResponse> surveys) {
        if (surveyListLayout == null) {
            return;
        }

        surveyListLayout.removeAllViews();

        if (surveys == null || surveys.isEmpty()) {
            Log.w(TAG_SURVEY_LIST_DEBUG,
                    "UI empty state: API succeeded but survey list is empty (not a client failure)");
            surveyListLayout.addView(createEmptyView("설문 목록이 없습니다."));
            return;
        }

        Log.d(TAG_SURVEY_LIST_DEBUG,
                "UI success state: loadSurveys rendered count=" + surveys.size());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < surveys.size(); i++) {
            SurveyResponse survey = surveys.get(i);
            if (survey == null) {
                continue;
            }

            Log.d(TAG_SURVEY_LIST_DEBUG,
                    "survey id=" + survey.getId()
                            + ", title=" + survey.getTitle()
                            + ", categoryName=" + survey.getCategoryName()
                            + ", reward=" + survey.getReward());

            View surveyCard = inflater.inflate(R.layout.item_survey_card, surveyListLayout, false);

            TextView titleText = surveyCard.findViewById(R.id.surveyTitleText);
            TextView pointText = surveyCard.findViewById(R.id.surveyPointText);
            LinearLayout tagLayout = surveyCard.findViewById(R.id.surveyTagLayout);
            FrameLayout itemSurveyCard = surveyCard.findViewById(R.id.itemSurveyCard);

            titleText.setText(valueOrDefault(survey.getTitle(), "제목 없음"));
            pointText.setText(formatReward(survey.getReward()));

            tagLayout.removeAllViews();
            String categoryName = survey.getCategoryName();
            if (categoryName == null || categoryName.trim().isEmpty()) {
                Log.d(TAG_SURVEY_LIST_DEBUG,
                        "categoryName missing for surveyId=" + survey.getId());
                tagLayout.addView(createTagTextView(tagLayout, "카테고리 없음"));
            } else {
                tagLayout.addView(createTagTextView(tagLayout, categoryName));
            }

            itemSurveyCard.setOnClickListener(v -> {
                Long surveyId = survey.getId();
                Log.d(TAG_SURVEY_LIST_DEBUG, "selected surveyId=" + surveyId);
                showSurveyDialog(survey);
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

    private TextView createEmptyView(String message) {
        TextView empty = new TextView(requireContext());
        empty.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        empty.setPadding(0, 40, 0, 40);
        empty.setText(message);
        empty.setTextColor(0xFF777777);
        empty.setTextSize(14f);
        return empty;
    }

    private String formatReward(Integer reward) {
        if (reward == null) {
            return "0 points";
        }
        return "0 ~ " + reward + " points ";
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showSurveyDialog(SurveyResponse survey) {
        String title = valueOrDefault(survey.getTitle(), "설문");
        String description = valueOrDefault(survey.getDescription(), "설명 없음");
        String rewardText = "+" + (survey.getReward() != null ? survey.getReward() : 0) + "P 적립";

        new AlertDialog.Builder(requireContext())
                .setTitle("설문 정보")
                .setMessage(
                        title +
                                "\n\n" +
                                description +
                                "\n\n소요 시간 : 약 1~2분\n" +
                                rewardText
                )
                .setNegativeButton("취소", null)
                .setPositiveButton("시작하기", (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), SurveyActivity.class);
                    if (survey.getId() != null) {
                        intent.putExtra(EXTRA_SURVEY_ID, survey.getId());
                    }
                    startActivity(intent);
                })
                .show();
    }
}
