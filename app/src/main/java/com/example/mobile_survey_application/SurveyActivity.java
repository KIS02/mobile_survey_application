package com.example.mobile_survey_application;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
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

import java.util.Arrays;
import androidx.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import viewmodel.SurveyViewModel;


public class SurveyActivity extends AppCompatActivity {
    private SurveyViewModel surveyViewModel;
    LinearLayout questionContainer;

    private SharedPreferences surveyPrefs;

    private static final String PREF_NAME = "survey_auto_save";
    private static final String KEY_DRAFT_JSON = "survey_draft_json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey);

        questionContainer = findViewById(R.id.question_container);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // 문항 자동저장용
        surveyPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // 문항 체크시 반영용
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);

        observeViewModel();

        btnSubmit.setOnClickListener(v -> {
            surveyViewModel.submitSurvey();
        });
    }

    private void observeViewModel() {
        surveyViewModel.getQuestionList().observe(this, questions -> {
            questionContainer.removeAllViews();

            for (int i = 0; i < questions.length; i++) {
                addQuestionBox(i + 1, questions[i]);
            }
        });

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
    }

    //region ### 설문 생성 및 제출 함수 모음 ###
//region ### 설문 생성 및 제출 함수 모음 ###
    private void addQuestionBox(int number, String questionText) {

        // 문항 박스 XML inflate
        View boxView = LayoutInflater.from(this)
                .inflate(R.layout.item_survey_question, questionContainer, false);

        TextView question = boxView.findViewById(R.id.tv_question_title);
        LinearLayout choiceRow = boxView.findViewById(R.id.choice_row);

        // 질문 텍스트 설정
        question.setText(number + ". " + questionText);

        RadioButton[] radioButtons = new RadioButton[5];

        // 선택지 1~5 생성
        for (int i = 1; i <= 5; i++) {
            createButton(i, number, radioButtons, choiceRow);
        }

        // 저장된 답변 복원
        int savedAnswer = loadSavedAnswer(number - 1);

        if (savedAnswer != -1) {
            radioButtons[savedAnswer - 1].setChecked(true);
            surveyViewModel.selectAnswer(number - 1, savedAnswer);
        }

        // 최종적으로 문항 박스 추가
        questionContainer.addView(boxView);
    }


    // RadioButton 생성 및 기능등록
    private void createButton(int idx, int number, RadioButton[] btn, LinearLayout choiceRow) {

        // 선택지 XML inflate
        View choiceView = LayoutInflater.from(this)
                .inflate(R.layout.item_survey_choice, choiceRow, false);

        TextView numberText = choiceView.findViewById(R.id.tv_choice_number);
        RadioButton radioButton = choiceView.findViewById(R.id.rb_choice);

        // 선택지 번호 설정
        numberText.setText(String.valueOf(idx));

        btn[idx - 1] = radioButton;
        int index = idx - 1;

        radioButton.setOnClickListener(v -> {

            // 같은 문항의 다른 라디오버튼 해제
            for (RadioButton rb : btn) {
                if (rb != null) {
                    rb.setChecked(false);
                }
            }

            // 현재 선택한 버튼만 체크
            btn[index].setChecked(true);

            // 선택 ViewModel에 저장
            surveyViewModel.selectAnswer(number - 1, index + 1);

            // 선택 자동저장
            saveAnswerAuto(number - 1, index + 1);
        });

        choiceRow.addView(choiceView);
    }
//endregion

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("설문을 완료할까요?")
                .setMessage("제출 후에는 답변을 수정할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("제출", (dialog, which) -> {
                    clearAutoSavedSurvey(); // 자동저장 삭제

                    showSubmitSuccessToast();
                    Intent intent = new Intent(SurveyActivity.this, HomeActivity.class);
                    startActivity(intent);
                })
                .show();
    }

    private void showDeniedDialog(){
        new AlertDialog.Builder(this)
                .setTitle("응답을 완료해주세요.")
                .setMessage("아직 선택되지 않은 문항이 있습니다.\n모든 문항에 응답한 후 제출해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }
    private void showSubmitSuccessToast() {
        Toast.makeText(SurveyActivity.this, "제출이 완료되었습니다", Toast.LENGTH_SHORT).show();
    }

    //endregion


    //region ### 자동저장 관련 함수 ###
//region JSON 자동저장 관련 함수

    private void saveAnswerAuto(int questionIndex, int answer) {
        try {
            JSONObject draftJson = getDraftJson();

            draftJson.put("hasDraft", true);

            JSONObject answersJson;

            if (draftJson.has("answers")) {
                answersJson = draftJson.getJSONObject("answers");
            } else {
                answersJson = new JSONObject();
            }

            answersJson.put(String.valueOf(questionIndex), answer);
            draftJson.put("answers", answersJson);

            surveyPrefs.edit()
                    .putString(KEY_DRAFT_JSON, draftJson.toString())
                    .apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int loadSavedAnswer(int questionIndex) {
        try {
            JSONObject draftJson = getDraftJson();

            if (!draftJson.has("answers")) {
                return -1;
            }

            JSONObject answersJson = draftJson.getJSONObject("answers");

            return answersJson.optInt(String.valueOf(questionIndex), -1);

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean hasAutoSavedSurvey() {
        try {
            JSONObject draftJson = getDraftJson();
            return draftJson.optBoolean("hasDraft", false);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void clearAutoSavedSurvey() {
        surveyPrefs.edit()
                .remove(KEY_DRAFT_JSON)
                .apply();
    }

    private JSONObject getDraftJson() throws JSONException {
        String jsonString = surveyPrefs.getString(KEY_DRAFT_JSON, null);

        if (jsonString == null) {
            return new JSONObject();
        }

        return new JSONObject(jsonString);
    }

//endregion
    //endregion

}