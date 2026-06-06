package com.example.mobile_survey_application;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import data.repository.CategoryRepository;
import model.CategoryResponse;
import model.admin.CreateOptionRequest;
import model.admin.CreateQuestionRequest;
import model.admin.CreateSurveyRequest;
import viewmodel.AdminViewModel;

public class CreateSurveyActivity extends AppCompatActivity {

    private static final String[] QUESTION_TYPES = {"일반", "지시", "거짓", "유사", "역채점"};
    private static final String[] GENDER_OPTIONS = {"성별 무관", "MALE", "FEMALE"};

    private AdminViewModel adminViewModel;
    private LinearLayout containerQuestions;
    private Spinner spinnerCategory;
    private Spinner spinnerGender;
    private EditText etTitle, etDescription, etReward, etTargetCount, etAgeMin, etAgeMax;
    private ProgressBar progressBar;
    private Button btnSubmit;

    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private int questionCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_survey);

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        initViews();
        setupCategorySpinner();
        setupGenderSpinner();
        observeViewModel();

        loadCategories();

        // 처음 문항 1개 기본 추가
        addQuestion();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etReward = findViewById(R.id.et_reward);
        etTargetCount = findViewById(R.id.et_target_count);
        etAgeMin = findViewById(R.id.et_age_min);
        etAgeMax = findViewById(R.id.et_age_max);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerGender = findViewById(R.id.spinner_gender);
        containerQuestions = findViewById(R.id.container_questions);
        progressBar = findViewById(R.id.progress_bar);
        btnSubmit = findViewById(R.id.btn_submit);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_question).setOnClickListener(v -> addQuestion());
        btnSubmit.setOnClickListener(v -> submitSurvey());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, GENDER_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadCategories() {
        new CategoryRepository().getCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<CategoryResponse> result) {
                runOnUiThread(() -> {
                    categoryList.clear();
                    categoryList.addAll(result);
                    List<String> names = new ArrayList<>();
                    for (CategoryResponse c : result) names.add(c.getName());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CreateSurveyActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                });
            }

            @Override
            public void onFailure(String message) {
                runOnUiThread(() ->
                        Toast.makeText(CreateSurveyActivity.this,
                                "카테고리 로딩 실패: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addQuestion() {
        questionCount++;
        View questionView = LayoutInflater.from(this)
                .inflate(R.layout.item_question_editor, containerQuestions, false);

        TextView tvNumber = questionView.findViewById(R.id.tv_question_number);
        tvNumber.setText("문항 " + questionCount);

        // 문항 유형 스피너
        Spinner spinnerType = questionView.findViewById(R.id.spinner_question_type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, QUESTION_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // 선택지 컨테이너
        LinearLayout containerOptions = questionView.findViewById(R.id.container_options);

        // 기본 선택지 4개 추가
        for (int i = 1; i <= 4; i++) {
            addOption(containerOptions, i);
        }

        // 선택지 추가 버튼
        questionView.findViewById(R.id.btn_add_option).setOnClickListener(v -> {
            int optionCount = containerOptions.getChildCount() + 1;
            addOption(containerOptions, optionCount);
        });

        // 문항 삭제 버튼
        int currentCount = questionCount;
        questionView.setTag(currentCount);
        questionView.findViewById(R.id.btn_delete_question).setOnClickListener(v -> {
            containerQuestions.removeView(questionView);
            renumberQuestions();
        });

        containerQuestions.addView(questionView);
    }

    private void addOption(LinearLayout container, int order) {
        View optionView = LayoutInflater.from(this)
                .inflate(R.layout.item_option_editor, container, false);

        TextView tvNumber = optionView.findViewById(R.id.tv_option_number);
        tvNumber.setText(String.valueOf(order));

        optionView.findViewById(R.id.btn_delete_option).setOnClickListener(v -> {
            container.removeView(optionView);
            renumberOptions(container);
        });

        container.addView(optionView);
    }

    private void renumberQuestions() {
        questionCount = 0;
        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            View v = containerQuestions.getChildAt(i);
            questionCount++;
            TextView tv = v.findViewById(R.id.tv_question_number);
            if (tv != null) tv.setText("문항 " + questionCount);
        }
    }

    private void renumberOptions(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            TextView tv = v.findViewById(R.id.tv_option_number);
            if (tv != null) tv.setText(String.valueOf(i + 1));
        }
    }

    private void submitSurvey() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("설문 제목을 입력하세요.");
            return;
        }

        String rewardStr = etReward.getText().toString().trim();
        if (rewardStr.isEmpty()) {
            etReward.setError("리워드 크레딧을 입력하세요.");
            return;
        }

        if (containerQuestions.getChildCount() == 0) {
            Toast.makeText(this, "문항을 1개 이상 추가하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int reward = Integer.parseInt(rewardStr);
        String description = etDescription.getText().toString().trim();

        Long categoryId = null;
        if (!categoryList.isEmpty()) {
            categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();
        }

        String targetCountStr = etTargetCount.getText().toString().trim();
        Integer targetCount = targetCountStr.isEmpty() ? null : Integer.parseInt(targetCountStr);

        String ageMinStr = etAgeMin.getText().toString().trim();
        String ageMaxStr = etAgeMax.getText().toString().trim();
        Short ageMin = ageMinStr.isEmpty() ? null : Short.parseShort(ageMinStr);
        Short ageMax = ageMaxStr.isEmpty() ? null : Short.parseShort(ageMaxStr);

        String genderSelection = GENDER_OPTIONS[spinnerGender.getSelectedItemPosition()];
        String targetGender = "성별 무관".equals(genderSelection) ? null : genderSelection;

        List<CreateQuestionRequest> questions = buildQuestions();
        if (questions == null) return;

        CreateSurveyRequest request = new CreateSurveyRequest(
                title, description.isEmpty() ? null : description,
                categoryId, reward, targetCount,
                null, null,
                targetGender, ageMin, ageMax,
                questions
        );

        adminViewModel.createSurvey(request);
    }

    private List<CreateQuestionRequest> buildQuestions() {
        List<CreateQuestionRequest> questions = new ArrayList<>();

        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            View qView = containerQuestions.getChildAt(i);
            EditText etText = qView.findViewById(R.id.et_question_text);
            Spinner spinnerType = qView.findViewById(R.id.spinner_question_type);
            LinearLayout containerOptions = qView.findViewById(R.id.container_options);

            String questionText = etText.getText().toString().trim();
            if (questionText.isEmpty()) {
                etText.setError("문항 내용을 입력하세요.");
                return null;
            }

            String questionType = (String) spinnerType.getSelectedItem();

            List<CreateOptionRequest> options = buildOptions(containerOptions);
            if (options == null) return null;
            if (options.isEmpty()) {
                Toast.makeText(this, (i + 1) + "번 문항에 선택지를 추가하세요.", Toast.LENGTH_SHORT).show();
                return null;
            }

            questions.add(new CreateQuestionRequest(
                    questionText, questionType, (short) (i + 1), null, options));
        }

        return questions;
    }

    private List<CreateOptionRequest> buildOptions(LinearLayout container) {
        List<CreateOptionRequest> options = new ArrayList<>();
        for (int j = 0; j < container.getChildCount(); j++) {
            View oView = container.getChildAt(j);
            EditText etText = oView.findViewById(R.id.et_option_text);
            String optionText = etText.getText().toString().trim();
            if (optionText.isEmpty()) {
                etText.setError("선택지 내용을 입력하세요.");
                return null;
            }
            options.add(new CreateOptionRequest(optionText, (short) (j + 1)));
        }
        return options;
    }

    private void observeViewModel() {
        adminViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!isLoading);
        });

        adminViewModel.getCreateSurveyResult().observe(this, response -> {
            Toast.makeText(this,
                    "설문이 등록되었습니다!\n제목: " + response.getTitle(),
                    Toast.LENGTH_LONG).show();
            finish();
        });

        adminViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
