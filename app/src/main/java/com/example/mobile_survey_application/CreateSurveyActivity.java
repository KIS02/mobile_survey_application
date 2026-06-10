package com.example.mobile_survey_application;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import data.repository.CategoryRepository;
import model.CategoryResponse;
import model.admin.CreateQuestionRequest;
import model.admin.CreateSurveyRequest;
import model.admin.OptionSetResponse;
import model.admin.PreviewReliabilityRequest;
import model.admin.PreviewReliabilityResponse;
import viewmodel.AdminViewModel;

public class CreateSurveyActivity extends AppCompatActivity {

    private static final String[] QUESTION_TYPES = {"일반", "지시", "거짓", "유사", "반대"};
    private static final String[] GENDER_OPTIONS = {"성별 무관", "MALE", "FEMALE"};

    private AdminViewModel adminViewModel;
    private LinearLayout containerQuestions;
    private Spinner spinnerCategory, spinnerGender;
    private EditText etTitle, etDescription, etReward, etTargetCount, etAgeMin, etAgeMax;
    private EditText etSimilarCount, etReverseCount;
    private ProgressBar progressBar;
    private Button btnSubmit, btnAiPreview, btnAddInstruction, btnAddBogus;

    private final List<CategoryResponse> categoryList = new ArrayList<>();
    private final List<OptionSetResponse> optionSetList = new ArrayList<>();
    private int questionCount = 0;
    /** AI 미리보기 요청 시 사용한 원본 문항 목록 — 다이얼로그에서 출처 표시에 사용 */
    private List<PreviewReliabilityRequest.QuestionInput> lastPreviewInputs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_survey);

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        initViews();
        observeViewModel();

        loadCategories();
        adminViewModel.loadOptionSets();

        addQuestion();
    }

    private void initViews() {
        etTitle        = findViewById(R.id.et_title);
        etDescription  = findViewById(R.id.et_description);
        etReward       = findViewById(R.id.et_reward);
        etTargetCount  = findViewById(R.id.et_target_count);
        etAgeMin       = findViewById(R.id.et_age_min);
        etAgeMax       = findViewById(R.id.et_age_max);
        etSimilarCount = findViewById(R.id.et_similar_count);
        etReverseCount = findViewById(R.id.et_reverse_count);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerGender   = findViewById(R.id.spinner_gender);
        containerQuestions = findViewById(R.id.container_questions);
        progressBar    = findViewById(R.id.progress_bar);
        btnSubmit          = findViewById(R.id.btn_submit);
        btnAiPreview       = findViewById(R.id.btn_ai_preview);
        btnAddInstruction  = findViewById(R.id.btn_add_instruction);
        btnAddBogus        = findViewById(R.id.btn_add_bogus);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, GENDER_OPTIONS);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> catPlaceholder = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        catPlaceholder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catPlaceholder);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_question).setOnClickListener(v -> addQuestion());
        btnAiPreview.setOnClickListener(v -> requestAiPreview());
        btnAddInstruction.setOnClickListener(v -> adminViewModel.addRandomInstruction());
        btnAddBogus.setOnClickListener(v -> adminViewModel.addRandomBogus());
        btnSubmit.setOnClickListener(v -> submitSurvey());
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
        View qView = LayoutInflater.from(this)
                .inflate(R.layout.item_question_editor, containerQuestions, false);

        // 문항 번호
        TextView tvNumber = qView.findViewById(R.id.tv_question_number);
        tvNumber.setText("문항 " + questionCount);

        // 문항 유형 스피너
        Spinner spinnerType = qView.findViewById(R.id.spinner_question_type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, QUESTION_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // 지시 문항 정답 순서 영역
        View layoutCorrectOrder = qView.findViewById(R.id.layout_correct_order);
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                layoutCorrectOrder.setVisibility(
                        "지시".equals(QUESTION_TYPES[pos]) ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 옵션 세트 스피너
        Spinner spinnerOptionSet = qView.findViewById(R.id.spinner_option_set);
        refreshOptionSetSpinner(spinnerOptionSet);

        // 옵션 세트 선택 시 미리보기 갱신
        LinearLayout previewContainer = qView.findViewById(R.id.container_option_preview);
        spinnerOptionSet.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                updateOptionPreview(previewContainer, pos);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 문항 삭제
        qView.findViewById(R.id.btn_delete_question).setOnClickListener(v -> {
            int deletedIdx = containerQuestions.indexOfChild(qView);
            containerQuestions.removeView(qView);
            updateAiTagIndices(deletedIdx);
            renumberQuestions();
        });

        containerQuestions.addView(qView);

        // optionSets가 이미 로드된 경우 미리보기 즉시 갱신
        if (!optionSetList.isEmpty()) {
            updateOptionPreview(previewContainer, 0);
        }
    }

    private void addGeneratedQuestion(PreviewReliabilityResponse.GeneratedQuestion gq) {
        questionCount++;
        View qView = LayoutInflater.from(this)
                .inflate(R.layout.item_question_editor, containerQuestions, false);

        qView.setTag(gq);

        TextView tvNumber = qView.findViewById(R.id.tv_question_number);
        boolean isAiLabeled = "유사".equals(gq.getQuestionType()) || "반대".equals(gq.getQuestionType());
        tvNumber.setText("문항 " + questionCount + (isAiLabeled ? " [AI]" : ""));
        tvNumber.setTextColor(isAiLabeled ? 0xFF6750A4
                : tvNumber.getContext().getResources().getColor(android.R.color.black, null));

        EditText etText = qView.findViewById(R.id.et_question_text);
        etText.setText(gq.getQuestionText());

        Spinner spinnerType = qView.findViewById(R.id.spinner_question_type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, QUESTION_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        int typePos = 0;
        for (int i = 0; i < QUESTION_TYPES.length; i++) {
            if (QUESTION_TYPES[i].equals(gq.getQuestionType())) { typePos = i; break; }
        }
        spinnerType.setSelection(typePos);

        View layoutCorrectOrder = qView.findViewById(R.id.layout_correct_order);
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                layoutCorrectOrder.setVisibility(
                        "지시".equals(QUESTION_TYPES[pos]) ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        if ("지시".equals(gq.getQuestionType()) && gq.getCorrectOptionOrder() != null) {
            layoutCorrectOrder.setVisibility(View.VISIBLE);
            EditText etCorrect = qView.findViewById(R.id.et_correct_order);
            etCorrect.setText(String.valueOf(gq.getCorrectOptionOrder()));
        }

        Spinner spinnerOptionSet = qView.findViewById(R.id.spinner_option_set);
        refreshOptionSetSpinner(spinnerOptionSet);

        int optSetPos = 0;
        for (int i = 0; i < optionSetList.size(); i++) {
            if (Objects.equals(optionSetList.get(i).getId(), gq.getOptionSetId())) { optSetPos = i; break; }
        }
        spinnerOptionSet.setSelection(optSetPos);

        LinearLayout previewContainer = qView.findViewById(R.id.container_option_preview);
        updateOptionPreview(previewContainer, optSetPos);
        spinnerOptionSet.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                updateOptionPreview(previewContainer, pos);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        qView.findViewById(R.id.btn_delete_question).setOnClickListener(v -> {
            int deletedIdx = containerQuestions.indexOfChild(qView);
            containerQuestions.removeView(qView);
            updateAiTagIndices(deletedIdx);
            renumberQuestions();
        });

        containerQuestions.addView(qView);
    }

    /** AI 문항 삭제 후 나머지 AI 태그의 basedOnOriginalIndex 재계산
     *  - 원본이 삭제된 경우(== deletedIdx): -1로 무효화
     *  - 삭제 위치보다 뒤쪽 원본을 참조하는 경우(> deletedIdx): 1 감소 */
    private void updateAiTagIndices(int deletedIdx) {
        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            Object tag = containerQuestions.getChildAt(i).getTag();
            if (tag instanceof PreviewReliabilityResponse.GeneratedQuestion) {
                PreviewReliabilityResponse.GeneratedQuestion gq =
                        (PreviewReliabilityResponse.GeneratedQuestion) tag;
                if (gq.getBasedOnOriginalIndex() == null) continue;
                if (gq.getBasedOnOriginalIndex() == deletedIdx) {
                    gq.setBasedOnOriginalIndex(-1);
                } else if (gq.getBasedOnOriginalIndex() > deletedIdx) {
                    gq.setBasedOnOriginalIndex(gq.getBasedOnOriginalIndex() - 1);
                }
            }
        }
    }

    private void refreshOptionSetSpinner(Spinner spinner) {
        List<String> names = new ArrayList<>();
        for (OptionSetResponse os : optionSetList) names.add(os.getName());
        if (names.isEmpty()) names.add("(불러오는 중...)");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void updateOptionPreview(LinearLayout container, int optionSetIndex) {
        container.removeAllViews();
        if (optionSetList.isEmpty() || optionSetIndex >= optionSetList.size()) return;

        OptionSetResponse selected = optionSetList.get(optionSetIndex);
        if (selected.getItems() == null) return;

        for (OptionSetResponse.ItemResponse item : selected.getItems()) {
            TextView tv = new TextView(this);
            tv.setText(item.getOptionOrder() + ". " + item.getOptionText() + "  ");
            tv.setTextSize(11f);
            tv.setTextColor(0xFF555555);
            container.addView(tv);
        }
    }

    private void refreshAllOptionSetSpinners(List<OptionSetResponse> sets) {
        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            View qView = containerQuestions.getChildAt(i);
            Spinner spinner = qView.findViewById(R.id.spinner_option_set);
            LinearLayout preview = qView.findViewById(R.id.container_option_preview);
            if (spinner != null) {
                int selected = spinner.getSelectedItemPosition();
                List<String> names = new ArrayList<>();
                for (OptionSetResponse os : sets) names.add(os.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                updateOptionPreview(preview, Math.max(0, selected));
            }
        }
    }

    private void renumberQuestions() {
        questionCount = 0;
        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            questionCount++;
            View child = containerQuestions.getChildAt(i);
            TextView tv = child.findViewById(R.id.tv_question_number);
            if (tv == null) continue;
            Object tag = child.getTag();
            boolean isAiLabeled = tag instanceof PreviewReliabilityResponse.GeneratedQuestion
                    && ("유사".equals(((PreviewReliabilityResponse.GeneratedQuestion) tag).getQuestionType())
                        || "반대".equals(((PreviewReliabilityResponse.GeneratedQuestion) tag).getQuestionType()));
            if (isAiLabeled) {
                tv.setText("문항 " + questionCount + " [AI]");
                tv.setTextColor(0xFF6750A4);
            } else {
                tv.setText("문항 " + questionCount);
                tv.setTextColor(tv.getContext().getResources()
                        .getColor(android.R.color.black, null));
            }
        }
    }

    private void requestAiPreview() {
        if (containerQuestions.getChildCount() == 0) {
            Toast.makeText(this, "원본 문항을 1개 이상 추가해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (optionSetList.isEmpty()) {
            Toast.makeText(this, "선택지 세트를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int similarCount = parseCount(etSimilarCount, 0);
        int reverseCount = parseCount(etReverseCount, 0);

        if (similarCount == 0 && reverseCount == 0) {
            Toast.makeText(this, "유사 또는 반대 문항 개수를 1 이상 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 일반 문항만 원본으로 사용 (지시/거짓/유사/반대 제외)
        List<PreviewReliabilityRequest.QuestionInput> inputs = new ArrayList<>();
        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            View qView = containerQuestions.getChildAt(i);
            Spinner spinnerType = qView.findViewById(R.id.spinner_question_type);
            if (spinnerType == null) continue;
            String type = QUESTION_TYPES[spinnerType.getSelectedItemPosition()];
            if (!"일반".equals(type)) continue;

            EditText etText = qView.findViewById(R.id.et_question_text);
            Spinner spinnerSet = qView.findViewById(R.id.spinner_option_set);

            String text = etText.getText().toString().trim();
            if (text.isEmpty()) { etText.setError("문항 내용을 입력하세요."); return; }

            int setPos = spinnerSet.getSelectedItemPosition();
            long optionSetId = (setPos >= 0 && setPos < optionSetList.size())
                    ? optionSetList.get(setPos).getId() : 1L;

            inputs.add(new PreviewReliabilityRequest.QuestionInput(text, optionSetId));
        }

        if (inputs.isEmpty()) {
            Toast.makeText(this, "일반 문항을 1개 이상 추가해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxCount = inputs.size();
        if (similarCount > maxCount || reverseCount > maxCount) {
            Toast.makeText(this, "유사/반대 개수는 원본(일반) 문항 수(" + maxCount + ")를 초과할 수 없습니다.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryName = categoryList.isEmpty() ? null
                : categoryList.get(spinnerCategory.getSelectedItemPosition()).getName();

        lastPreviewInputs = new ArrayList<>(inputs);
        adminViewModel.previewReliability(
                new PreviewReliabilityRequest(inputs, categoryName, similarCount, reverseCount));
    }

    private void showPreviewDialog(PreviewReliabilityResponse response) {
        if (response == null || response.getQuestions() == null || response.getQuestions().isEmpty()) {
            Toast.makeText(this, "생성된 문항이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PreviewReliabilityResponse.GeneratedQuestion> generated = response.getQuestions();

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        scrollView.addView(layout);

        for (int i = 0; i < generated.size(); i++) {
            PreviewReliabilityResponse.GeneratedQuestion q = generated.get(i);

            // 유사/반대인 경우 원본 문항 출처 표시
            boolean hasSource = ("유사".equals(q.getQuestionType()) || "반대".equals(q.getQuestionType()))
                    && q.getBasedOnOriginalIndex() != null;

            if (hasSource) {
                int origIdx = q.getBasedOnOriginalIndex();
                String origText = (origIdx >= 0 && origIdx < lastPreviewInputs.size())
                        ? lastPreviewInputs.get(origIdx).getQuestionText() : "";

                TextView tvSource = new TextView(this);
                tvSource.setText("원본: " + origText);
                tvSource.setTextSize(11f);
                tvSource.setTextColor(0xFF888888);
                tvSource.setBackgroundColor(0xFFF5F5F5);
                tvSource.setPadding(12, 6, 12, 6);
                layout.addView(tvSource);
            }

            TextView tvType = new TextView(this);
            tvType.setText("[" + q.getQuestionType() + "]");
            tvType.setTextSize(12f);
            tvType.setTextColor(typeColor(q.getQuestionType()));
            tvType.setTypeface(null, android.graphics.Typeface.BOLD);
            tvType.setPadding(0, hasSource ? 4 : 0, 0, 0);
            layout.addView(tvType);

            TextView tvText = new TextView(this);
            tvText.setText(q.getQuestionText());
            tvText.setTextSize(14f);
            tvText.setTextColor(0xFF333333);
            tvText.setPadding(0, 4, 0, 0);
            if (q.getCorrectOptionOrder() != null) {
                tvText.append("\n→ 정답 선택지: " + q.getCorrectOptionOrder() + "번");
            }
            layout.addView(tvText);

            if (i < generated.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                lp.setMargins(0, 12, 0, 12);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFFEEEEEE);
                layout.addView(divider);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("AI 생성 신뢰도 문항 미리보기")
                .setView(scrollView)
                .setNegativeButton("취소", null)
                .setPositiveButton("추가", (dialog, which) -> {
                    for (PreviewReliabilityResponse.GeneratedQuestion gq : generated) {
                        addGeneratedQuestion(gq);
                    }
                    Toast.makeText(this,
                            generated.size() + "개의 신뢰도 문항이 추가되었습니다.",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private int typeColor(String type) {
        switch (type) {
            case "지시": return 0xFF1976D2;
            case "거짓": return 0xFFE53935;
            case "유사": return 0xFF388E3C;
            case "반대": return 0xFFF57C00;
            default: return 0xFF555555;
        }
    }

    private int parseCount(EditText et, int defaultVal) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return defaultVal;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultVal; }
    }

    private void submitSurvey() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) { etTitle.setError("설문 제목을 입력하세요."); return; }

        String rewardStr = etReward.getText().toString().trim();
        if (rewardStr.isEmpty()) { etReward.setError("리워드 크레딧을 입력하세요."); return; }

        if (containerQuestions.getChildCount() == 0) {
            Toast.makeText(this, "문항을 1개 이상 추가하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (optionSetList.isEmpty()) {
            Toast.makeText(this, "선택지 세트를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int reward;
        try { reward = Integer.parseInt(rewardStr); }
        catch (NumberFormatException e) { etReward.setError("올바른 숫자를 입력하세요."); return; }

        String description = etDescription.getText().toString().trim();

        if (categoryList.isEmpty()) {
            Toast.makeText(this, "카테고리를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Long categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();

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

        List<CreateQuestionRequest> questions = buildQuestions();
        if (questions == null) return;

        adminViewModel.createSurvey(new CreateSurveyRequest(
                title, description.isEmpty() ? null : description,
                categoryId, reward, targetCount,
                null, null,
                targetGender, ageMin, ageMax,
                questions));
    }

    private List<CreateQuestionRequest> buildQuestions() {
        List<CreateQuestionRequest> questions = new ArrayList<>();
        int orderNum = 1;

        for (int i = 0; i < containerQuestions.getChildCount(); i++) {
            View qView = containerQuestions.getChildAt(i);
            EditText etText      = qView.findViewById(R.id.et_question_text);
            Spinner  spinnerType = qView.findViewById(R.id.spinner_question_type);
            Spinner  spinnerSet  = qView.findViewById(R.id.spinner_option_set);
            EditText etCorrect   = qView.findViewById(R.id.et_correct_order);

            String questionText = etText.getText().toString().trim();
            if (questionText.isEmpty()) { etText.setError("문항 내용을 입력하세요."); return null; }

            String questionType = QUESTION_TYPES[spinnerType.getSelectedItemPosition()];

            Short correctOrder = null;
            if ("지시".equals(questionType)) {
                String correctStr = etCorrect.getText().toString().trim();
                if (correctStr.isEmpty()) { etCorrect.setError("정답 번호를 입력하세요."); return null; }
                try { correctOrder = Short.parseShort(correctStr); }
                catch (NumberFormatException e) { etCorrect.setError("숫자를 입력하세요."); return null; }
            }

            int setPos = spinnerSet.getSelectedItemPosition();
            if (setPos < 0 || setPos >= optionSetList.size()) {
                Toast.makeText(this, (i + 1) + "번 문항의 선택지 세트를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return null;
            }
            long optionSetId = optionSetList.get(setPos).getId();

            // AI 생성 문항은 태그에서 similarId/reverseId 추출
            Long similarId = null;
            Long reverseId = null;
            Object tag = qView.getTag();
            if (tag instanceof PreviewReliabilityResponse.GeneratedQuestion) {
                PreviewReliabilityResponse.GeneratedQuestion gq =
                        (PreviewReliabilityResponse.GeneratedQuestion) tag;
                Integer origIdx = gq.getBasedOnOriginalIndex();
                if (origIdx != null && origIdx >= 0) {
                    if ("유사".equals(gq.getQuestionType())) {
                        similarId = (long) origIdx;
                    } else if ("반대".equals(gq.getQuestionType())) {
                        reverseId = (long) origIdx;
                    }
                }
            }

            questions.add(new CreateQuestionRequest(
                    questionText, questionType, (short) orderNum++, correctOrder, optionSetId,
                    similarId, reverseId));
        }

        return questions;
    }

    private void observeViewModel() {
        adminViewModel.getIsLoading().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!loading);
            btnAiPreview.setEnabled(!loading);
            btnAddInstruction.setEnabled(!loading);
            btnAddBogus.setEnabled(!loading);
        });

        adminViewModel.getOptionSets().observe(this, sets -> {
            optionSetList.clear();
            optionSetList.addAll(sets);
            refreshAllOptionSetSpinners(sets);
        });

        adminViewModel.getPreviewResult().observe(this, response -> {
            if (response != null) {
                adminViewModel.clearPreviewResult();
                showPreviewDialog(response);
            }
        });

        adminViewModel.getSingleQuestion().observe(this, question -> {
            if (question != null) {
                adminViewModel.clearSingleQuestion();
                addGeneratedQuestion(question);
                Toast.makeText(this,
                        "[" + question.getQuestionType() + "] 문항이 추가되었습니다.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        adminViewModel.getCreateSurveyResult().observe(this, response -> {
            if (response != null) {
                adminViewModel.clearCreateSurveyResult();
                Toast.makeText(this,
                        "설문이 등록되었습니다!\n제목: " + response.getTitle(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

        adminViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                adminViewModel.clearErrorMessage();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
