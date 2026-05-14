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

public class SurveyActivity extends AppCompatActivity {

    LinearLayout questionContainer;

    private int num_question_box = 0;
    private int[] answered;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey);

        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            submitButtonClicked();
        });

        questionContainer = findViewById(R.id.question_container);

        // 임시로 만든 문제리스트
        String[] questions = {
                "나는 하루 한 끼를 규칙적으로 먹는다.",
                "나는 아침 식사를 자주 한다.",
                "나는 패스트푸드를 자주 섭취한다.",
                "나는 야식을 자주 먹는다.",
                "나는 채소를 자주 먹는다."
        };

        loadQuestions(5, questions);



    }

    private void loadQuestions(int number, String[] question_text) {
        num_question_box = number;
        answered = new int[num_question_box];
        Arrays.fill(answered, -1);

        for( int i = 0; i < number; i++){
            addQuestionBox(i+1, question_text[i]);
        }
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

                answered[number - 1] = index + 1;
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

    private void submitButtonClicked() {
        if ( isQuestionCompleted() )
            showConfirmDialog();
        else
            showDeniedDialog();
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
    private boolean isQuestionCompleted(){
        try {
            for( int i = 0; i < num_question_box; i++){
                if ( answered[i] == -1 )
                    return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}