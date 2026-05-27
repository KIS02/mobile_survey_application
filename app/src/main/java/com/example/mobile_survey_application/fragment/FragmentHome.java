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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import data.local.TokenManager;
import model.SurveyResponse;
import viewmodel.SurveyViewModel;

public class FragmentHome extends Fragment {

    public static final String EXTRA_SURVEY_ID = "surveyId";
    private static final String TAG_HOME_SURVEY_DEBUG = "HOME_SURVEY_DEBUG";

    private TextView randomSurveyButton;
    private LinearLayout questionLayout;
    private LinearLayout selectSurveyList;

    private TokenManager tokenManager;
    private SurveyViewModel surveyViewModel;

    private SurveyResponse randomSurvey;

    public FragmentHome() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        randomSurveyButton = view.findViewById(R.id.randomSurveyButton);
        questionLayout = view.findViewById(R.id.questionLayout);
        selectSurveyList = view.findViewById(R.id.selectSurveyList);

        tokenManager = new TokenManager(requireContext());
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);

        observeViewModel();

        randomSurveyButton.setOnClickListener(v -> {
            if (randomSurvey == null) {
                Toast.makeText(requireContext(), "불러온 설문이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            showSurveyDialog(randomSurvey);
        });

        resizeRandomSurveyBox(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSurveys();
    }

    private void observeViewModel() {
        surveyViewModel.getSurveyList().observe(getViewLifecycleOwner(), this::renderHomeSurveys);

        surveyViewModel.getSurveyListError().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty()) {
                return;
            }

            Log.e(TAG_HOME_SURVEY_DEBUG,
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

        Log.d(TAG_HOME_SURVEY_DEBUG, "loadSurveys request from FragmentHome");
        surveyViewModel.loadSurveys(accessToken);
    }

    private void renderHomeSurveys(List<SurveyResponse> surveys) {
        if (surveys == null || surveys.isEmpty()) {
            randomSurvey = null;

            randomSurveyButton.setText("진행 가능한 랜덤 설문이 없습니다.");

            if (selectSurveyList != null) {
                selectSurveyList.removeAllViews();
                selectSurveyList.addView(createEmptyView("추천 설문이 없습니다."));
            }

            return;
        }

        List<SurveyResponse> validSurveys = new ArrayList<>();

        for (SurveyResponse survey : surveys) {
            if (survey != null && survey.getId() != null) {
                validSurveys.add(survey);
            }
        }

        if (validSurveys.isEmpty()) {
            randomSurvey = null;
            randomSurveyButton.setText("진행 가능한 랜덤 설문이 없습니다.");

            if (selectSurveyList != null) {
                selectSurveyList.removeAllViews();
                selectSurveyList.addView(createEmptyView("추천 설문이 없습니다."));
            }

            return;
        }

        Log.d(TAG_HOME_SURVEY_DEBUG,
                "UI success state: home surveys rendered count=" + validSurveys.size());

        setRandomSurvey(validSurveys);
        renderRecommendedSurveys(validSurveys);
    }

    private void setRandomSurvey(List<SurveyResponse> surveys) {
        int randomIndex = new Random().nextInt(surveys.size());
        randomSurvey = surveys.get(randomIndex);

        String title = valueOrDefault(randomSurvey.getTitle(), "랜덤 설문");
        String category = valueOrDefault(randomSurvey.getCategoryName(), "카테고리 없음");
        String reward = formatReward(randomSurvey.getReward());

        randomSurveyButton.setText(
                title +
                        "\n" +
                        category +
                        " · " +
                        reward
        );

        if (questionLayout != null) {
            questionLayout.setVisibility(View.GONE);
        }
    }

    private void renderRecommendedSurveys(List<SurveyResponse> surveys) {
        if (selectSurveyList == null) {
            Log.e(TAG_HOME_SURVEY_DEBUG,
                    "recommendedSurveyListLayout is null. Check fragment_home.xml id.");
            return;
        }

        selectSurveyList.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        int maxCount = Math.min(surveys.size(), 5);

        for (int i = 0; i < maxCount; i++) {
            SurveyResponse survey = surveys.get(i);

            View surveyCard = inflater.inflate(
                    R.layout.item_survey_card,
                    selectSurveyList,
                    false
            );

            TextView titleText = surveyCard.findViewById(R.id.surveyTitleText);
            TextView pointText = surveyCard.findViewById(R.id.surveyPointText);
            LinearLayout tagLayout = surveyCard.findViewById(R.id.surveyTagLayout);
            FrameLayout itemSurveyCard = surveyCard.findViewById(R.id.itemSurveyCard);

            titleText.setText(valueOrDefault(survey.getTitle(), "제목 없음"));
            pointText.setText(formatReward(survey.getReward()));

            tagLayout.removeAllViews();

            String categoryName = survey.getCategoryName();

            if (categoryName == null || categoryName.trim().isEmpty()) {
                tagLayout.addView(createTagTextView(tagLayout, "카테고리 없음"));
            } else {
                tagLayout.addView(createTagTextView(tagLayout, categoryName));
            }

            itemSurveyCard.setOnClickListener(v -> showSurveyDialog(survey));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(92)
                    );

            if (i != 0) {
                cardParams.topMargin = dp(12);
            }

            surveyCard.setLayoutParams(cardParams);
            selectSurveyList.addView(surveyCard);
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

        empty.setPadding(0, dp(20), 0, dp(20));
        empty.setText(message);
        empty.setTextColor(0xFF777777);
        empty.setTextSize(14f);

        return empty;
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
                    intent.putExtra(EXTRA_SURVEY_ID, survey.getId());
                    startActivity(intent);
                })
                .show();
    }

    private String formatReward(Integer reward) {
        if (reward == null) {
            return "0 points ✨";
        }

        return reward + " points ✨";
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value;
    }

    //region ### 화면 크기 자동 조절 함수 ###
    private void resizeRandomSurveyBox(View root) {
        View contentArea = root.findViewById(R.id.contentArea);
        View randomHeader = root.findViewById(R.id.randomHeader);
        View selectHeader = root.findViewById(R.id.selectHeader);
        View sectionDivider = root.findViewById(R.id.sectionDivider);
        FrameLayout randomSurveyBox = root.findViewById(R.id.randomSurveyBox);

        if (contentArea == null ||
                randomHeader == null ||
                selectHeader == null ||
                sectionDivider == null ||
                randomSurveyBox == null) {
            return;
        }

        contentArea.post(() -> {
            int parentWidth = contentArea.getWidth();

            int desiredRandomBoxHeight = parentWidth * 3 / 4;

            int minScrollAreaHeight = dp(80);

            int fixedHeight =
                    randomHeader.getHeight()
                            + getVerticalMargins(randomSurveyBox)
                            + sectionDivider.getHeight()
                            + getVerticalMargins(sectionDivider)
                            + selectHeader.getHeight()
                            + dp(12)
                            + minScrollAreaHeight;

            int maxRandomBoxHeight = contentArea.getHeight() - fixedHeight;

            if (maxRandomBoxHeight < dp(80)) {
                maxRandomBoxHeight = dp(80);
            }

            int finalHeight = Math.min(desiredRandomBoxHeight, maxRandomBoxHeight);

            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) randomSurveyBox.getLayoutParams();

            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = finalHeight;

            randomSurveyBox.setLayoutParams(params);
        });
    }

    private int getVerticalMargins(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams =
                    (ViewGroup.MarginLayoutParams) layoutParams;

            return marginParams.topMargin + marginParams.bottomMargin;
        }

        return 0;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
    //endregion
}