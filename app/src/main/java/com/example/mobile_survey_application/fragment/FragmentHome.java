package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobile_survey_application.R;

import java.util.Arrays;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class FragmentHome extends Fragment {

    private ViewPager2 randomSurveySlideView;
    private TextView randomSurveyProgressText;
    private Button nextQuestionButton;

    private final String[] questions = {
            "나는 아침 식사를 자주 한다.",
            "나는 하루 한 끼를 규칙적으로 먹는다.",
            "나는 패스트푸드를 자주 섭취한다.",
            "나는 야식을 자주 먹는다.",
            "나는 채소를 자주 먹는다."
    };

    private int[] answers;

    public FragmentHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        answers = new int[questions.length];
        Arrays.fill(answers, -1);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        TextView randomSurveyButton = view.findViewById(R.id.randomSurveyButton);
        LinearLayout questionLayout = view.findViewById(R.id.questionLayout);

        randomSurveySlideView = view.findViewById(R.id.randomSurveySlideView);
        randomSurveyProgressText = view.findViewById(R.id.randomSurveyProgressText);
        nextQuestionButton = view.findViewById(R.id.nextQuestionButton);

        RandomSurveyAdapter adapter = new RandomSurveyAdapter();
        randomSurveySlideView.setAdapter(adapter);

        updateProgressText(0);

        randomSurveyButton.setOnClickListener(v -> {
            randomSurveyButton.setVisibility(View.GONE);
            questionLayout.setVisibility(View.VISIBLE);
        });

        randomSurveySlideView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                updateProgressText(position);

                if (position == questions.length - 1) {
                    nextQuestionButton.setText("완료");
                } else {
                    nextQuestionButton.setText("다음 항목");
                }
            }
        });


        nextQuestionButton.setOnClickListener(v -> {
            int currentIndex = randomSurveySlideView.getCurrentItem();

            if (currentIndex < questions.length - 1) {
                randomSurveySlideView.setCurrentItem(currentIndex + 1, true);
            } else {
                checkRandomSurveyFinished();
            }
        });


        resizeRandomSurveyBox(view);

        return view;
    }

    private void updateProgressText(int position) {
        randomSurveyProgressText.setText((position + 1) + " / " + questions.length);
    }

    private void checkRandomSurveyFinished() {
        int latestUnansweredIndex = findFirstUnansweredQuestionIndex();

        if (latestUnansweredIndex != -1) {
            Toast.makeText(
                    requireContext(),
                    "아직 응답하지 않은 문항이 있습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            randomSurveySlideView.setCurrentItem(latestUnansweredIndex, true);
            return;
        }

        Toast.makeText(
                requireContext(),
                "랜덤 설문이 완료되었습니다.",
                Toast.LENGTH_SHORT
        ).show();

        // 여기서 서버 전송, 포인트 지급, 저장 처리
    }

    private int findFirstUnansweredQuestionIndex() {
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] == -1) {
                return i;
            }
        }
        return -1;
    }

    private class RandomSurveyAdapter extends RecyclerView.Adapter<RandomSurveyAdapter.RandomSurveyViewHolder> {

        @NonNull
        @Override
        public RandomSurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_survey_option, parent, false);

            return new RandomSurveyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RandomSurveyViewHolder holder, int position) {
            holder.questionText.setText("Q. " + questions[position]);

            holder.ratingGroup.setOnCheckedChangeListener(null);
            holder.ratingGroup.clearCheck();

            if (answers[position] != -1) {
                holder.ratingGroup.check(getRadioButtonIdByAnswer(answers[position]));
            }

            holder.ratingGroup.setOnCheckedChangeListener((group, checkedId) -> {
                int answer = getAnswerByRadioButtonId(checkedId);
                answers[position] = answer;

                // 선택 후 다음 슬라이드로 자동 이동
                if (position < questions.length - 1) {
                    randomSurveySlideView.postDelayed(() -> {
                        randomSurveySlideView.setCurrentItem(position + 1, true);
                    }, 300);
                } else {
                    checkRandomSurveyFinished();
                }
            });
        }

        @Override
        public int getItemCount() {
            return questions.length;
        }

        private class RandomSurveyViewHolder extends RecyclerView.ViewHolder {

            TextView questionText;
            RadioGroup ratingGroup;

            public RandomSurveyViewHolder(@NonNull View itemView) {
                super(itemView);

                questionText = itemView.findViewById(R.id.questionText);
                ratingGroup = itemView.findViewById(R.id.ratingGroup);
            }
        }
    }

    private int getRadioButtonIdByAnswer(int answer) {
        if (answer == 1) return R.id.rating1;
        if (answer == 2) return R.id.rating2;
        if (answer == 3) return R.id.rating3;
        if (answer == 4) return R.id.rating4;
        if (answer == 5) return R.id.rating5;

        return -1;
    }

    private int getAnswerByRadioButtonId(int checkedId) {
        if (checkedId == R.id.rating1) return 1;
        if (checkedId == R.id.rating2) return 2;
        if (checkedId == R.id.rating3) return 3;
        if (checkedId == R.id.rating4) return 4;
        if (checkedId == R.id.rating5) return 5;

        return -1;
    }

    //region ### 화면 크기 자동 조절 함수 ###
    private void resizeRandomSurveyBox(View root) {
        View contentArea = root.findViewById(R.id.contentArea);
        View randomHeader = root.findViewById(R.id.randomHeader);
        View selectHeader = root.findViewById(R.id.selectHeader);
        View sectionDivider = root.findViewById(R.id.sectionDivider);
        FrameLayout randomSurveyBox = root.findViewById(R.id.randomSurveyBox);

        contentArea.post(() -> {

            // randomSurveyBox의 가로는 항상 match_parent 기준
            int parentWidth = contentArea.getWidth();

            // 기본 비율: 16:9
            // height = width * 9 / 16
            int desiredRandomBoxHeight = parentWidth * 3 / 4;

            // 아래 ScrollView 안에 최소 설문 카드 1개가 보이도록 확보할 높이
            // 카드 높이 92dp + 여유 공간
            int minScrollAreaHeight = dp(80);

            int fixedHeight =
                    randomHeader.getHeight()
                            + getVerticalMargins(randomSurveyBox)
                            + sectionDivider.getHeight()
                            + getVerticalMargins(sectionDivider)
                            + selectHeader.getHeight()
                            + dp(12) // ScrollView marginTop
                            + minScrollAreaHeight;

            int maxRandomBoxHeight = contentArea.getHeight() - fixedHeight;

            // 너무 작아지는 것 방지
            if (maxRandomBoxHeight < dp(80)) {
                maxRandomBoxHeight = dp(80);
            }

            // 기본은 16:9 높이,
            // 하지만 아래 선택 설문 카드 1개가 안 들어가면 높이만 줄임
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