package com.example.mobile_survey_application;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import com.example.mobile_survey_application.fragment.FragmentSurvey;

import data.local.TokenManager;
import model.SurveyDetailResponse;
import model.SurveyOptionResponse;
import model.SurveyQuestionResponse;
import viewmodel.SurveyViewModel;


public class SurveyActivity extends AppCompatActivity {
    private static final String TAG_SURVEY_DEBUG = "SURVEY_DEBUG";
    private static final String TAG_SURVEY_LIST_DEBUG = "SURVEY_LIST_DEBUG";

    private SurveyViewModel surveyViewModel;
    private Long intentSurveyId;
    private LinearLayout questionContainer;
    private TokenManager tokenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey);

        questionContainer = findViewById(R.id.question_container);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        tokenManager = new TokenManager(this);
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);

        if (getIntent() != null && getIntent().hasExtra(FragmentSurvey.EXTRA_SURVEY_ID)) {
            intentSurveyId = getIntent().getLongExtra(FragmentSurvey.EXTRA_SURVEY_ID, -1L);
            if (intentSurveyId != null && intentSurveyId < 0) {
                intentSurveyId = null;
            }
        }
        Log.d(TAG_SURVEY_LIST_DEBUG, "intent surveyId=" + intentSurveyId);

        observeViewModel();

        btnSubmit.setOnClickListener(v -> {
            String token = tokenManager.getAccessToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(SurveyActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            surveyViewModel.submitSurvey(token);
        });

        loadSurvey();
    }

    private void observeViewModel() {
        surveyViewModel.getSurveyDetail().observe(this, this::renderSurvey);

        surveyViewModel.getSubmitSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                showConfirmDialog();
                surveyViewModel.doneSubmitSuccess();
            }
        });

        surveyViewModel.getSubmitDenied().observe(this, denied -> {
            if (Boolean.TRUE.equals(denied)) {
                showDeniedDialog();
                surveyViewModel.doneSubmitDenied();
            }
        });

        surveyViewModel.getSubmitErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(SurveyActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSurvey() {
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (intentSurveyId != null) {
            Log.d(TAG_SURVEY_LIST_DEBUG,
                    "loadSurveyById request surveyId=" + intentSurveyId);
            surveyViewModel.loadSurveyById(token, intentSurveyId);
        } else {
            Log.d(TAG_SURVEY_LIST_DEBUG, "loadRandomSurvey request (no surveyId in intent)");
            surveyViewModel.loadRandomSurvey(token);
        }
    }

    private void renderSurvey(SurveyDetailResponse detail) {
        if (detail == null) {
            return;
        }

        Log.d(TAG_SURVEY_DEBUG, "renderSurvey surveyId=" + detail.getId()
                + ", questionCount=" + (detail.getQuestions() == null ? 0 : detail.getQuestions().size()));

        questionContainer.removeAllViews();

        List<SurveyQuestionResponse> questions = detail.getQuestions();
        if (questions == null) {
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            addQuestionBox(i + 1, questions.get(i));
        }
    }

    //region ### 설문 생성 및 제출 ###
    private void addQuestionBox(int number, SurveyQuestionResponse question) {
        View boxView = LayoutInflater.from(this)
                .inflate(R.layout.item_survey_question, questionContainer, false);

        TextView questionTitle = boxView.findViewById(R.id.tv_question_title);
        LinearLayout choiceRow = boxView.findViewById(R.id.choice_row);

        questionTitle.setText(number + ". " + question.getContent());

        List<SurveyOptionResponse> options = question.getOptions();
        if (options == null || options.isEmpty()) {
            questionContainer.addView(boxView);
            return;
        }

        RadioButton[] radioButtons = new RadioButton[options.size()];
        for (int i = 0; i < options.size(); i++) {
            createOptionButton(i, question, options.get(i), radioButtons, choiceRow);
        }

        questionContainer.addView(boxView);
    }

    private void createOptionButton(int idx,
                                    SurveyQuestionResponse question,
                                    SurveyOptionResponse option,
                                    RadioButton[] btn,
                                    LinearLayout choiceRow) {

        View choiceView = LayoutInflater.from(this)
                .inflate(R.layout.item_survey_choice, choiceRow, false);

        TextView numberText = choiceView.findViewById(R.id.tv_choice_number);
        RadioButton radioButton = choiceView.findViewById(R.id.rb_choice);

        numberText.setText(option.getText());

        btn[idx] = radioButton;

        radioButton.setOnClickListener(v -> {
            for (RadioButton rb : btn) {
                if (rb != null) {
                    rb.setChecked(false);
                }
            }

            btn[idx].setChecked(true);

            surveyViewModel.selectOption(question.getId(), option.getId());
        });

        choiceRow.addView(choiceView);
    }
    //endregion

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("설문을 완료할까요?")
                .setMessage("제출 후에는 답변을 수정할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("제출", (dialog, which) -> {
                    showSubmitSuccessToast();
                    Intent intent = new Intent(SurveyActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("응답을 완료해주세요.")
                .setMessage("아직 선택되지 않은 문항이 있습니다.\n모든 문항에 응답한 후 제출해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }

    private void showSubmitSuccessToast() {
        Toast.makeText(SurveyActivity.this, "제출이 완료되었습니다", Toast.LENGTH_SHORT).show();
    }
}
