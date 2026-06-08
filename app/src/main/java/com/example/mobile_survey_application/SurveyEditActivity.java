package com.example.mobile_survey_application;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import model.CategoryResponse;
import model.admin.AdminSurveyResponse;
import model.admin.UpdateSurveyRequest;
import data.repository.CategoryRepository;
import viewmodel.AdminViewModel;

public class SurveyEditActivity extends AppCompatActivity {

    private static final String[] GENDER_OPTIONS = {"성별 무관", "MALE", "FEMALE"};

    private AdminViewModel adminViewModel;
    private long surveyId;
    private String surveyStatus;

    private EditText etTitle, etDescription, etReward, etTargetCount;
    private EditText etAgeMin, etAgeMax, etStartAt, etEndAt;
    private Spinner spinnerCategory, spinnerGender;
    private ProgressBar progressBar;

    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private AdminSurveyResponse loadedSurvey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_edit);

        surveyId = getIntent().getLongExtra("surveyId", -1);
        surveyStatus = getIntent().getStringExtra("status");

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        initViews();
        observeViewModel();
        loadCategories();
        adminViewModel.loadSurvey(surveyId);
    }

    private void initViews() {
        etTitle       = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etReward      = findViewById(R.id.et_reward);
        etTargetCount = findViewById(R.id.et_target_count);
        etAgeMin      = findViewById(R.id.et_age_min);
        etAgeMax      = findViewById(R.id.et_age_max);
        etStartAt     = findViewById(R.id.et_start_at);
        etEndAt       = findViewById(R.id.et_end_at);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerGender   = findViewById(R.id.spinner_gender);
        progressBar   = findViewById(R.id.progress_bar);

        TextView tvNotice = findViewById(R.id.tv_active_notice);

        // ACTIVE 상태: 날짜 필드만 활성화
        boolean isActive = "ACTIVE".equals(surveyStatus);
        if (isActive) {
            tvNotice.setVisibility(View.VISIBLE);
            etTitle.setEnabled(false);
            etDescription.setEnabled(false);
            etReward.setEnabled(false);
            etTargetCount.setEnabled(false);
            etAgeMin.setEnabled(false);
            etAgeMax.setEnabled(false);
            spinnerCategory.setEnabled(false);
            spinnerGender.setEnabled(false);
        }

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, GENDER_OPTIONS);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> catPlaceholder = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        catPlaceholder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catPlaceholder);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveSurvey());
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
                            SurveyEditActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                    if (loadedSurvey != null) prefillCategorySpinner();
                });
            }
            @Override
            public void onFailure(String message) {}
        });
    }

    private void observeViewModel() {
        adminViewModel.getIsLoading().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            findViewById(R.id.btn_save).setEnabled(!loading);
        });

        adminViewModel.getSurveyDetail().observe(this, survey -> {
            if (survey != null) {
                loadedSurvey = survey;
                prefillFields(survey);
            }
        });

        adminViewModel.getActionSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                adminViewModel.clearActionSuccess();
                Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        adminViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                adminViewModel.clearErrorMessage();
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void prefillFields(AdminSurveyResponse survey) {
        etTitle.setText(survey.getTitle());
        etDescription.setText(survey.getDescription() != null ? survey.getDescription() : "");
        etReward.setText(survey.getReward() != null ? String.valueOf(survey.getReward()) : "");
        etTargetCount.setText(survey.getTargetCount() != null
                ? String.valueOf(survey.getTargetCount()) : "");
        etAgeMin.setText(survey.getTargetAgeMin() != null
                ? String.valueOf(survey.getTargetAgeMin()) : "");
        etAgeMax.setText(survey.getTargetAgeMax() != null
                ? String.valueOf(survey.getTargetAgeMax()) : "");
        etStartAt.setText(survey.getStartAt() != null ? fromIso(survey.getStartAt()) : "");
        etEndAt.setText(survey.getEndAt() != null ? fromIso(survey.getEndAt()) : "");

        // 성별
        if (survey.getTargetGender() != null) {
            for (int i = 0; i < GENDER_OPTIONS.length; i++) {
                if (GENDER_OPTIONS[i].equals(survey.getTargetGender())) {
                    spinnerGender.setSelection(i);
                    break;
                }
            }
        }

        prefillCategorySpinner();
    }

    private void prefillCategorySpinner() {
        if (loadedSurvey == null || categoryList.isEmpty()) return;
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(loadedSurvey.getCategoryId())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    /** "2026-01-01T09:00:00" → "2026-01-01 09:00" (화면 표시용) */
    private String fromIso(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        // T 기준으로 분리 후 초(:ss) 제거
        String[] parts = iso.split("T");
        if (parts.length < 2) return iso;
        String time = parts[1].length() >= 5 ? parts[1].substring(0, 5) : parts[1];
        return parts[0] + " " + time;
    }

    /** "2026-01-01 09:00" → "2026-01-01T09:00:00" (서버 LocalDateTime 파싱용) */
    private String toIso(String input) {
        if (input == null || input.isEmpty()) return null;
        String s = input.trim();
        // 이미 T 포함이면 그대로
        if (s.contains("T")) return s;
        // "yyyy-MM-dd HH:mm" → "yyyy-MM-ddTHH:mm:00"
        return s.replace(" ", "T") + ":00";
    }

    private void saveSurvey() {
        String startAt = toIso(etStartAt.getText().toString().trim());
        String endAt   = toIso(etEndAt.getText().toString().trim());

        UpdateSurveyRequest request;

        if ("ACTIVE".equals(surveyStatus)) {
            request = new UpdateSurveyRequest(startAt, endAt);
        } else {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) { etTitle.setError("제목을 입력하세요."); return; }

            if (categoryList.isEmpty()) {
                Toast.makeText(this, "카테고리 목록을 불러오는 중입니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Long categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();

            String rewardStr = etReward.getText().toString().trim();
            Integer reward;
            try { reward = rewardStr.isEmpty() ? null : Integer.parseInt(rewardStr); }
            catch (NumberFormatException e) { etReward.setError("숫자를 입력하세요."); return; }

            String tcStr = etTargetCount.getText().toString().trim();
            Integer targetCount;
            try { targetCount = tcStr.isEmpty() ? null : Integer.parseInt(tcStr); }
            catch (NumberFormatException e) { etTargetCount.setError("숫자를 입력하세요."); return; }

            String ageMinStr = etAgeMin.getText().toString().trim();
            String ageMaxStr = etAgeMax.getText().toString().trim();
            Short ageMin, ageMax;
            try { ageMin = ageMinStr.isEmpty() ? null : Short.parseShort(ageMinStr); }
            catch (NumberFormatException e) { etAgeMin.setError("숫자를 입력하세요."); return; }
            try { ageMax = ageMaxStr.isEmpty() ? null : Short.parseShort(ageMaxStr); }
            catch (NumberFormatException e) { etAgeMax.setError("숫자를 입력하세요."); return; }

            String genderSel = GENDER_OPTIONS[spinnerGender.getSelectedItemPosition()];
            String targetGender = "성별 무관".equals(genderSel) ? null : genderSel;

            String desc = etDescription.getText().toString().trim();

            request = new UpdateSurveyRequest(
                    title,
                    desc.isEmpty() ? null : desc,
                    categoryId, reward, targetCount,
                    (startAt == null || startAt.isEmpty()) ? null : startAt,
                    (endAt == null || endAt.isEmpty()) ? null : endAt,
                    targetGender, ageMin, ageMax,
                    null);
        }

        adminViewModel.updateSurvey(surveyId, request);
    }
}
