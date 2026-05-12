package com.example.mobile_survey_application;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {

    LinearLayout questionContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey);

        questionContainer = findViewById(R.id.question_container);

        addQuestionBox(1, "나는 하루 한 끼를 규칙적으로 먹는다.");
        addQuestionBox(2, "나는 아침 식사를 자주 한다.");
        addQuestionBox(3, "나는 패스트푸드를 자주 섭취한다.");
        addQuestionBox(4, "나는 야식을 자주 먹는다.");
        addQuestionBox(5, "나는 채소를 자주 먹는다.");
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
}