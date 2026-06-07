package com.example.mobile_survey_application.fragment;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.SurveyActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import data.local.NotificationPreferenceManager;
import data.local.TokenManager;
import model.SurveyDetailResponse;
import model.SurveyOptionResponse;
import model.SurveyQuestionResponse;
import model.SurveyResponse;
import viewmodel.SurveyViewModel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.mobile_survey_application.worker.RandomSurveyReminderWorker;

import java.util.concurrent.TimeUnit;

import android.os.CountDownTimer;

import data.local.RandomSurveyCooldownManager;


public class FragmentHome extends Fragment {


    // 테스트용 설문조사 전용
    private static final boolean USE_MOCK_SURVEY = false;
    private static final boolean USE_MOCK_INTERNAL_SURVEY = false;

    private TextView randomSurveyButton;
    private LinearLayout questionLayout;
    private LinearLayout selectSurveyList;

    private TokenManager tokenManager;
    private SurveyViewModel surveyViewModel;

    private SurveyResponse randomSurvey;
    private ViewPager2 randomSurveySlideView;
    private TextView randomSurveyProgressText;
    private Button nextQuestionButton;

    private MockInternalSurveyAdapter mockInternalSurveyAdapter;
    private boolean isInternalSurveyUiReady = false;


    private InternalSurveyAdapter internalSurveyAdapter;
    private SurveyDetailResponse internalSurveyDetail;

    private List<SurveyQuestionResponse> internalQuestions = new ArrayList<>();
    private Map<Long, Long> selectedServerOptionByQuestionId = new HashMap<>();


    private int reminderTime = 30;
    private TimeUnit reminderTimeUnit = TimeUnit.SECONDS;

    private RandomSurveyCooldownManager randomSurveyCooldownManager;
    private CountDownTimer randomSurveyCountDownTimer;

    private String currentRandomSurveyText = "";
    private NotificationPreferenceManager notificationPreferenceManager;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isGranted) {
                            Toast.makeText(
                                    requireContext(),
                                    "알림 권한이 없어 24시간 뒤 발송 예정인 알림이 표시되지 않을 수 있습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }


    private void scheduleRandomSurveyReminder() {
        if (notificationPreferenceManager == null) {
            notificationPreferenceManager = new NotificationPreferenceManager(requireContext());
        }

        if (!notificationPreferenceManager.isNotificationEnabled()) {
            notificationPreferenceManager.cancelRandomSurveyReminder();
            return;
        }

        requestNotificationPermissionIfNeeded();

        OneTimeWorkRequest reminderRequest =
                new OneTimeWorkRequest.Builder(RandomSurveyReminderWorker.class)
                        .setInitialDelay(reminderTime, reminderTimeUnit)
                        .build();

        WorkManager.getInstance(requireContext().getApplicationContext())
                .enqueueUniqueWork(
                        NotificationPreferenceManager.RANDOM_SURVEY_REMINDER_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        reminderRequest
                );

        Toast.makeText(
                requireContext(),
                "24시간 뒤 랜덤 설문 알림이 발송됩니다.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void beginRandomSurveyCooldown() {
        if (randomSurveyCooldownManager == null) {
            randomSurveyCooldownManager = new RandomSurveyCooldownManager(requireContext());
        }

        randomSurveyCooldownManager.startCooldown(reminderTime, reminderTimeUnit);

        scheduleRandomSurveyReminder();

        updateRandomSurveyCooldownUi();
    }

    private void updateRandomSurveyCooldownUi() {
        stopRandomSurveyCooldownTimer();

        if (randomSurveyCooldownManager == null || !randomSurveyCooldownManager.isCoolingDown()) {
            renderRandomSurveyAvailableUi();
            return;
        }

        long remainingMillis = randomSurveyCooldownManager.getRemainingMillis();

        randomSurveyButton.setEnabled(false);

        randomSurveyCountDownTimer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                randomSurveyButton.setText(
                        "랜덤 설문은 잠시 후 다시 가능합니다.\n" +
                                "남은 시간 " + formatRemainingTime(millisUntilFinished)
                );
            }

            @Override
            public void onFinish() {
                if (randomSurveyCooldownManager != null) {
                    randomSurveyCooldownManager.clearCooldown();
                }

                renderRandomSurveyAvailableUi();
            }
        };

        randomSurveyCountDownTimer.start();
    }

    private void renderRandomSurveyAvailableUi() {
        stopRandomSurveyCooldownTimer();

        randomSurveyButton.setEnabled(true);

        if (currentRandomSurveyText != null && !currentRandomSurveyText.isEmpty()) {
            randomSurveyButton.setText(currentRandomSurveyText);
        } else {
            randomSurveyButton.setText("진행 가능한 랜덤 설문이 없습니다.");
        }
    }

    private void stopRandomSurveyCooldownTimer() {
        if (randomSurveyCountDownTimer != null) {
            randomSurveyCountDownTimer.cancel();
            randomSurveyCountDownTimer = null;
        }
    }

    private String formatRemainingTime(long millis) {
        long totalSeconds = millis / 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRandomSurveyCooldownTimer();
    }

    public FragmentHome() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, container, false);

        randomSurveyButton = view.findViewById(R.id.randomSurveyButton);
        questionLayout = view.findViewById(R.id.questionLayout);
        selectSurveyList = view.findViewById(R.id.selectSurveyList);
        randomSurveySlideView = view.findViewById(R.id.randomSurveySlideView);
        randomSurveyProgressText = view.findViewById(R.id.randomSurveyProgressText);
        nextQuestionButton = view.findViewById(R.id.nextQuestionButton);

        tokenManager = new TokenManager(requireContext());
        notificationPreferenceManager = new NotificationPreferenceManager(requireContext());
        randomSurveyCooldownManager = new RandomSurveyCooldownManager(requireContext());
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);


        initInternalSurveyViews(view);

        observeViewModel();

        randomSurveyButton.setOnClickListener(v -> {
            if (randomSurveyCooldownManager != null && randomSurveyCooldownManager.isCoolingDown()) {
                Toast.makeText(
                        requireContext(),
                        "아직 랜덤 설문을 다시 할 수 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();

                updateRandomSurveyCooldownUi();
                return;
            }

            startInternalSurvey(randomSurvey == null ? null : randomSurvey.getId());
        });

        resizeRandomSurveyBox(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (USE_MOCK_SURVEY) {
            renderHomeSurveys(createMockSurveys());
        } else {
            loadSurveys();
        }
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

        surveyViewModel.getSurveyDetail().observe(getViewLifecycleOwner(), this::renderServerInternalSurvey);

        surveyViewModel.getSubmitSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "제출이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                beginRandomSurveyCooldown();

                questionLayout.setVisibility(View.GONE);
                randomSurveyButton.setVisibility(View.VISIBLE);

                surveyViewModel.doneSubmitSuccess();
            }
        });

        surveyViewModel.getSubmitDenied().observe(getViewLifecycleOwner(), denied -> {
            if (Boolean.TRUE.equals(denied)) {
                Toast.makeText(requireContext(), "아직 응답하지 않은 문항이 있습니다.", Toast.LENGTH_SHORT).show();

                int firstUnansweredIndex = findFirstUnansweredServerQuestionIndex();
                if (firstUnansweredIndex != -1) {
                    randomSurveySlideView.setCurrentItem(firstUnansweredIndex, true);
                }

                surveyViewModel.doneSubmitDenied();
            }
        });

        surveyViewModel.getSubmitErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
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

        currentRandomSurveyText =
                title +
                        "\n" +
                        category +
                        " · " +
                        reward;

        randomSurveyButton.setText(currentRandomSurveyText);

        updateRandomSurveyCooldownUi();

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
            return "0 points";
        }

        return "0 ~ " + reward + " points";
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value;
    }


    //region ### 랜덤 설문조사 구현부분 ###

    private void initInternalSurveyViews(View view) {
        randomSurveySlideView = view.findViewById(R.id.randomSurveySlideView);
        randomSurveyProgressText = view.findViewById(R.id.randomSurveyProgressText);
        nextQuestionButton = view.findViewById(R.id.nextQuestionButton);

        Log.d(TAG_HOME_SURVEY_DEBUG,
                "initInternalSurveyViews "
                        + "questionLayout=" + questionLayout
                        + ", randomSurveySlideView=" + randomSurveySlideView
                        + ", randomSurveyProgressText=" + randomSurveyProgressText
                        + ", nextQuestionButton=" + nextQuestionButton);

        if (questionLayout == null ||
                randomSurveySlideView == null ||
                randomSurveyProgressText == null ||
                nextQuestionButton == null) {

            isInternalSurveyUiReady = false;

            Log.e(TAG_HOME_SURVEY_DEBUG,
                    "내부설문 UI 초기화 실패");

            return;
        }

        questionLayout.setVisibility(View.GONE);

        if (USE_MOCK_INTERNAL_SURVEY) {
            mockInternalSurveyAdapter = new MockInternalSurveyAdapter();
            randomSurveySlideView.setAdapter(mockInternalSurveyAdapter);
        } else {
            internalSurveyAdapter = new InternalSurveyAdapter();
            randomSurveySlideView.setAdapter(internalSurveyAdapter);
        }

        randomSurveySlideView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (USE_MOCK_INTERNAL_SURVEY) {
                    updateMockInternalSurveyProgress(position);
                } else {
                    updateServerInternalSurveyProgress(position);
                }
            }
        });

        nextQuestionButton.setOnClickListener(v -> {
            if (USE_MOCK_INTERNAL_SURVEY) {
                handleMockNextButton();
            } else {
                handleServerNextButton();
            }
        });

        isInternalSurveyUiReady = true;

        Log.d(TAG_HOME_SURVEY_DEBUG, "내부설문 UI 초기화 성공");
    }

    private void startInternalSurvey(Long surveyId) {
        if (!isInternalSurveyUiReady) {
            Toast.makeText(requireContext(), "내부설문 UI가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (USE_MOCK_INTERNAL_SURVEY) {
            renderMockInternalSurvey();
            return;
        }

        String accessToken = tokenManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (surveyId == null) {
            Toast.makeText(requireContext(), "설문 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }



        surveyViewModel.loadSurveyById(accessToken, surveyId);
    }

    private class InternalSurveyAdapter extends RecyclerView.Adapter<InternalSurveyAdapter.InternalSurveyViewHolder> {

        @NonNull
        @Override
        public InternalSurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_survey_question, parent, false);

            itemView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            return new InternalSurveyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull InternalSurveyViewHolder holder, int position) {
            SurveyQuestionResponse question = internalQuestions.get(position);

            holder.questionTitle.setText((position + 1) + ". " + question.getContent());
            holder.choiceRow.removeAllViews();

            List<SurveyOptionResponse> options = question.getOptions();

            if (options == null || options.isEmpty()) {
                return;
            }

            RadioButton[] radioButtons = new RadioButton[options.size()];

            for (int i = 0; i < options.size(); i++) {
                createServerInternalOptionButton(
                        i,
                        question,
                        options.get(i),
                        radioButtons,
                        holder.choiceRow,
                        position
                );
            }
        }

        @Override
        public int getItemCount() {
            if (internalQuestions == null) {
                return 0;
            }

            return internalQuestions.size();
        }

        private class InternalSurveyViewHolder extends RecyclerView.ViewHolder {

            TextView questionTitle;
            LinearLayout choiceRow;

            public InternalSurveyViewHolder(@NonNull View itemView) {
                super(itemView);

                questionTitle = itemView.findViewById(R.id.tv_question_title);
                choiceRow = itemView.findViewById(R.id.choice_row);
            }
        }
    }

    private void createServerInternalOptionButton(int idx,
                                                  SurveyQuestionResponse question,
                                                  SurveyOptionResponse option,
                                                  RadioButton[] radioButtons,
                                                  LinearLayout choiceRow,
                                                  int questionPosition) {

        View choiceView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_survey_choice, choiceRow, false);

        TextView numberText = choiceView.findViewById(R.id.tv_choice_number);
        RadioButton radioButton = choiceView.findViewById(R.id.rb_choice);

        numberText.setText(option.getText());

        radioButtons[idx] = radioButton;

        Long selectedOptionId = selectedServerOptionByQuestionId.get(question.getId());

        if (selectedOptionId != null && selectedOptionId.equals(option.getId())) {
            radioButton.setChecked(true);
        }

        radioButton.setOnClickListener(v -> {
            for (RadioButton rb : radioButtons) {
                if (rb != null) {
                    rb.setChecked(false);
                }
            }

            radioButtons[idx].setChecked(true);

            selectedServerOptionByQuestionId.put(question.getId(), option.getId());
            surveyViewModel.selectOption(question.getId(), option.getId());

            if (questionPosition < internalQuestions.size() - 1) {
                randomSurveySlideView.postDelayed(() -> {
                    randomSurveySlideView.setCurrentItem(questionPosition + 1, true);
                }, 250);
            }
        });

        choiceRow.addView(choiceView);
    }


    private void renderServerInternalSurvey(SurveyDetailResponse detail) {
        if (detail == null) {
            return;
        }

        internalSurveyDetail = detail;
        selectedServerOptionByQuestionId.clear();

        List<SurveyQuestionResponse> questions = detail.getQuestions();

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(requireContext(), "설문 문항이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        internalQuestions.clear();
        internalQuestions.addAll(questions);

        internalSurveyAdapter = new InternalSurveyAdapter();
        randomSurveySlideView.setAdapter(internalSurveyAdapter);

        randomSurveySlideView.setCurrentItem(0, false);
        updateServerInternalSurveyProgress(0);

        randomSurveySlideView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateServerInternalSurveyProgress(position);
            }
        });

        randomSurveyButton.setVisibility(View.GONE);
        questionLayout.setVisibility(View.VISIBLE);

        Log.d(TAG_HOME_SURVEY_DEBUG,
                "server internal survey rendered surveyId=" + detail.getId()
                        + ", questionCount=" + internalQuestions.size());
    }

    private void updateServerInternalSurveyProgress(int position) {
        if (internalQuestions == null || internalQuestions.isEmpty()) {
            randomSurveyProgressText.setText("0 / 0");
            return;
        }

        randomSurveyProgressText.setText((position + 1) + " / " + internalQuestions.size());

        if (position == internalQuestions.size() - 1) {
            nextQuestionButton.setText("완료");
        } else {
            nextQuestionButton.setText("다음 항목");
        }
    }

    private void handleServerNextButton() {
        if (internalQuestions == null || internalQuestions.isEmpty()) {
            Toast.makeText(requireContext(), "진행 중인 설문이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentIndex = randomSurveySlideView.getCurrentItem();

        if (currentIndex < internalQuestions.size() - 1) {
            randomSurveySlideView.setCurrentItem(currentIndex + 1, true);
        } else {
            submitServerInternalSurvey();
        }
    }

    private void submitServerInternalSurvey() {
        int firstUnansweredIndex = findFirstUnansweredServerQuestionIndex();

        if (firstUnansweredIndex != -1) {
            Toast.makeText(
                    requireContext(),
                    "아직 응답하지 않은 문항이 있습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            randomSurveySlideView.setCurrentItem(firstUnansweredIndex, true);
            return;
        }

        String accessToken = tokenManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        showServerSubmitConfirmDialog(accessToken);
    }

    private void showServerSubmitConfirmDialog(String accessToken) {
        new AlertDialog.Builder(requireContext())
                .setTitle("설문을 제출할까요?")
                .setMessage("제출 후에는 답변을 수정할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("제출", (dialog, which) -> {
                    surveyViewModel.submitSurvey(accessToken);
                })
                .show();
    }

    private void showMockSubmitConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("테스트 설문을 제출할까요?")
                .setMessage("제출 후에는 답변을 수정할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("제출", (dialog, which) -> {
                    completeMockInternalSurvey();
                })
                .show();
    }

    private void completeMockInternalSurvey() {
        for (MockInternalQuestion question : mockInternalQuestions) {
            Long selectedOptionId = selectedMockOptionByQuestionId.get(question.id);

            Log.d(TAG_HOME_SURVEY_DEBUG,
                    "mock submit questionId=" + question.id
                            + ", question=" + question.content
                            + ", selectedOptionId=" + selectedOptionId);
        }

        Toast.makeText(
                requireContext(),
                "테스트 내부설문이 완료되었습니다.",
                Toast.LENGTH_SHORT
        ).show();

        beginRandomSurveyCooldown();

        questionLayout.setVisibility(View.GONE);
        randomSurveyButton.setVisibility(View.VISIBLE);
    }

    private int findFirstUnansweredServerQuestionIndex() {
        for (int i = 0; i < internalQuestions.size(); i++) {
            SurveyQuestionResponse question = internalQuestions.get(i);

            if (question == null || question.getId() == null) {
                return i;
            }

            if (!selectedServerOptionByQuestionId.containsKey(question.getId())) {
                return i;
            }
        }

        return -1;
    }

    private void handleMockNextButton() {
        if (mockInternalQuestions == null || mockInternalQuestions.isEmpty()) {
            Toast.makeText(requireContext(), "진행 중인 내부설문이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentIndex = randomSurveySlideView.getCurrentItem();

        if (currentIndex < mockInternalQuestions.size() - 1) {
            randomSurveySlideView.setCurrentItem(currentIndex + 1, true);
        } else {
            submitMockInternalSurvey();
        }
    }

//endregion

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

    //region ### 테스트용 임시설문조사 (테스트 불필요시 삭제) ###

    public static final String EXTRA_SURVEY_ID = "surveyId";
    private static final String TAG_HOME_SURVEY_DEBUG = "HOME_SURVEY_DEBUG";


    private final List<MockInternalQuestion> mockInternalQuestions = new ArrayList<>();
    private final Map<Long, Long> selectedMockOptionByQuestionId = new HashMap<>();


    private void renderMockInternalSurvey() {
        if (!isInternalSurveyUiReady) {
            Toast.makeText(requireContext(), "내부설문 UI가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mockInternalSurveyAdapter == null) {
            Toast.makeText(requireContext(), "내부설문 어댑터가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedMockOptionByQuestionId.clear();

        mockInternalQuestions.clear();
        mockInternalQuestions.addAll(createMockInternalQuestions());

        if (mockInternalQuestions.isEmpty()) {
            Toast.makeText(requireContext(), "테스트 문항이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        mockInternalSurveyAdapter.notifyDataSetChanged();

        randomSurveySlideView.setCurrentItem(0, false);
        updateMockInternalSurveyProgress(0);

        randomSurveyButton.setVisibility(View.GONE);
        questionLayout.setVisibility(View.VISIBLE);

        Log.d(TAG_HOME_SURVEY_DEBUG,
                "mock internal survey started, questionCount=" + mockInternalQuestions.size());

    }

    private List<MockInternalQuestion> createMockInternalQuestions() {
        List<MockInternalQuestion> questions = new ArrayList<>();

        questions.add(new MockInternalQuestion(
                1L,
                "나는 아침 식사를 자주 한다.",
                createFivePointOptions(1L)
        ));

        questions.add(new MockInternalQuestion(
                2L,
                "나는 하루 한 끼를 규칙적으로 먹는다.",
                createFivePointOptions(2L)
        ));

        questions.add(new MockInternalQuestion(
                3L,
                "나는 패스트푸드를 자주 섭취한다.",
                createFivePointOptions(3L)
        ));

        questions.add(new MockInternalQuestion(
                4L,
                "나는 야식을 자주 먹는다.",
                createFivePointOptions(4L)
        ));

        questions.add(new MockInternalQuestion(
                5L,
                "나는 채소를 자주 먹는다.",
                createFivePointOptions(5L)
        ));

        return questions;
    }

    private List<MockInternalOption> createFivePointOptions(long questionId) {
        List<MockInternalOption> options = new ArrayList<>();

        options.add(new MockInternalOption(questionId * 10 + 1, "1"));
        options.add(new MockInternalOption(questionId * 10 + 2, "2"));
        options.add(new MockInternalOption(questionId * 10 + 3, "3"));
        options.add(new MockInternalOption(questionId * 10 + 4, "4"));
        options.add(new MockInternalOption(questionId * 10 + 5, "5"));

        return options;
    }

    private void updateMockInternalSurveyProgress(int position) {
        if (randomSurveyProgressText == null) {
            return;
        }

        if (mockInternalQuestions.isEmpty()) {
            randomSurveyProgressText.setText("0 / 0");
            return;
        }

        randomSurveyProgressText.setText((position + 1) + " / " + mockInternalQuestions.size());

        if (nextQuestionButton != null) {
            if (position == mockInternalQuestions.size() - 1) {
                nextQuestionButton.setText("완료");
            } else {
                nextQuestionButton.setText("다음 항목");
            }
        }
    }

    private void submitMockInternalSurvey() {
        int firstUnansweredIndex = findFirstUnansweredMockQuestionIndex();

        if (firstUnansweredIndex != -1) {
            Toast.makeText(
                    requireContext(),
                    "아직 응답하지 않은 문항이 있습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            randomSurveySlideView.setCurrentItem(firstUnansweredIndex, true);
            return;
        }

        showMockSubmitConfirmDialog();
    }

    private int findFirstUnansweredMockQuestionIndex() {
        for (int i = 0; i < mockInternalQuestions.size(); i++) {
            MockInternalQuestion question = mockInternalQuestions.get(i);

            if (!selectedMockOptionByQuestionId.containsKey(question.id)) {
                return i;
            }
        }

        return -1;
    }

    private static class MockInternalQuestion {
        long id;
        String content;
        List<MockInternalOption> options;

        MockInternalQuestion(long id, String content, List<MockInternalOption> options) {
            this.id = id;
            this.content = content;
            this.options = options;
        }
    }

    private static class MockInternalOption {
        long id;
        String text;

        MockInternalOption(long id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    private class MockInternalSurveyAdapter extends RecyclerView.Adapter<MockInternalSurveyAdapter.MockInternalSurveyViewHolder> {

        @NonNull
        @Override
        public MockInternalSurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_survey_question, parent, false);

            itemView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            return new MockInternalSurveyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MockInternalSurveyViewHolder holder, int position) {
            MockInternalQuestion question = mockInternalQuestions.get(position);

            holder.questionTitle.setText((position + 1) + ". " + question.content);
            holder.choiceRow.removeAllViews();

            RadioButton[] radioButtons = new RadioButton[question.options.size()];

            for (int i = 0; i < question.options.size(); i++) {
                createMockInternalOptionButton(
                        i,
                        question,
                        question.options.get(i),
                        radioButtons,
                        holder.choiceRow,
                        position
                );
            }
        }

        @Override
        public int getItemCount() {
            return mockInternalQuestions.size();
        }

        private class MockInternalSurveyViewHolder extends RecyclerView.ViewHolder {

            TextView questionTitle;
            LinearLayout choiceRow;

            public MockInternalSurveyViewHolder(@NonNull View itemView) {
                super(itemView);

                questionTitle = itemView.findViewById(R.id.tv_question_title);
                choiceRow = itemView.findViewById(R.id.choice_row);
            }
        }
    }

    private void createMockInternalOptionButton(int idx,
                                                MockInternalQuestion question,
                                                MockInternalOption option,
                                                RadioButton[] radioButtons,
                                                LinearLayout choiceRow,
                                                int questionPosition) {

        View choiceView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_survey_choice, choiceRow, false);

        TextView numberText = choiceView.findViewById(R.id.tv_choice_number);
        RadioButton radioButton = choiceView.findViewById(R.id.rb_choice);

        numberText.setText(option.text);

        radioButtons[idx] = radioButton;

        Long selectedOptionId = selectedMockOptionByQuestionId.get(question.id);

        if (selectedOptionId != null && selectedOptionId.equals(option.id)) {
            radioButton.setChecked(true);
        }

        radioButton.setOnClickListener(v -> {
            for (RadioButton rb : radioButtons) {
                if (rb != null) {
                    rb.setChecked(false);
                }
            }

            radioButtons[idx].setChecked(true);
            selectedMockOptionByQuestionId.put(question.id, option.id);

            Log.d(TAG_HOME_SURVEY_DEBUG,
                    "mock select questionId=" + question.id
                            + ", optionId=" + option.id
                            + ", text=" + option.text);

            if (questionPosition < mockInternalQuestions.size() - 1) {
                randomSurveySlideView.postDelayed(() -> {
                    randomSurveySlideView.setCurrentItem(questionPosition + 1, true);
                }, 250);
            }
        });

        choiceRow.addView(choiceView);
    }

    private static class MockSurvey {
        long id;
        String title;
        String description;
        String category;
        int reward;
        String[] questions;

        MockSurvey(long id, String title, String description, String category, int reward, String[] questions) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.reward = reward;
            this.questions = questions;
        }
    }

    private List<SurveyResponse> createMockSurveys() {
        List<SurveyResponse> mockSurveys = new ArrayList<>();

        SurveyResponse survey1 = new SurveyResponse();
        survey1.setId(-1L);
        survey1.setTitle("식습관 테스트 설문");
        survey1.setDescription("식습관과 생활 패턴을 확인하기 위한 테스트 설문입니다.");
        survey1.setCategoryName("건강");
        survey1.setReward(30);

        SurveyResponse survey2 = new SurveyResponse();
        survey2.setId(-2L);
        survey2.setTitle("앱 사용성 테스트 설문");
        survey2.setDescription("앱 화면 구성과 사용 편의성에 대한 테스트 설문입니다.");
        survey2.setCategoryName("IT");
        survey2.setReward(50);

        SurveyResponse survey3 = new SurveyResponse();
        survey3.setId(-3L);
        survey3.setTitle("여가 생활 테스트 설문");
        survey3.setDescription("평소 여가 생활 패턴을 확인하기 위한 테스트 설문입니다.");
        survey3.setCategoryName("생활");
        survey3.setReward(20);

        mockSurveys.add(survey1);
        mockSurveys.add(survey2);
        mockSurveys.add(survey3);

        return mockSurveys;
    }
    //endregion
}