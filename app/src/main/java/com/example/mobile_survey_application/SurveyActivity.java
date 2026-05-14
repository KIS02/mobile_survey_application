package com.example.mobile_survey_application;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
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

import viewmodel.SurveyViewModel;


public class SurveyActivity extends AppCompatActivity {
    private SurveyViewModel surveyViewModel;
    LinearLayout questionContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey);

        questionContainer = findViewById(R.id.question_container);
        Button btnSubmit = findViewById(R.id.btnSubmit);

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

    private void addQuestionBox(int number, String questionText) {

        // 문항 박스
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.survey_box);
        box.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(125)
        );
        boxParams.topMargin = dp(18);
        box.setLayoutParams(boxParams);

        // 질문 텍스트
        TextView question = new TextView(this);
        question.setText(number + ". " + questionText);
        question.setTextSize(11);
        question.setTextColor(0xFF333333);

        box.addView(question);

        // 선택지 전체 가로 줄
        LinearLayout choiceRow = new LinearLayout(this);
        choiceRow.setOrientation(LinearLayout.HORIZONTAL);
        choiceRow.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams choiceRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        choiceRowParams.topMargin = dp(28);
        choiceRow.setLayoutParams(choiceRowParams);

        RadioButton[] radioButtons = new RadioButton[5];

        for (int i = 1; i <= 5; i++) {

            // 선택지 하나: 숫자 + 라디오버튼
            LinearLayout choiceColumn = new LinearLayout(this);
            choiceColumn.setOrientation(LinearLayout.VERTICAL);
            choiceColumn.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1
            );
            choiceColumn.setLayoutParams(columnParams);

            // 숫자
            TextView numberText = new TextView(this);
            numberText.setText(String.valueOf(i));
            numberText.setTextSize(11);
            numberText.setTextColor(0xFF333333);
            numberText.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            numberParams.gravity = Gravity.CENTER;
            numberText.setLayoutParams(numberParams);

            // 라디오 버튼
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText("");

            // RadioButton 기본 여백 제거
            radioButton.setMinWidth(0);
            radioButton.setMinimumWidth(0);
            radioButton.setMinHeight(0);
            radioButton.setMinimumHeight(0);
            radioButton.setPadding(0, 0, 0, 0);

            LinearLayout.LayoutParams radioParams = new LinearLayout.LayoutParams(
                    dp(32),
                    dp(32)
            );
            radioParams.gravity = Gravity.CENTER;
            radioButton.setLayoutParams(radioParams);

            radioButtons[i - 1] = radioButton;
            int index = i - 1;

            radioButton.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) {
                    rb.setChecked(false);
                }

                radioButtons[index].setChecked(true);

                surveyViewModel.selectAnswer(number - 1, index + 1);
            });

            choiceColumn.addView(numberText);
            choiceColumn.addView(radioButton);

            choiceRow.addView(choiceColumn);
        }

        box.addView(choiceRow);

        questionContainer.addView(box);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("설문을 완료할까요?")
                .setMessage("제출 후에는 답변을 수정할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("제출", (dialog, which) -> {
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

}