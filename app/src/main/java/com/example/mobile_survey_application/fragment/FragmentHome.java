package com.example.mobile_survey_application.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobile_survey_application.R;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView randomSurveyButton = view.findViewById(R.id.randomSurveyButton);
        LinearLayout questionLayout = view.findViewById(R.id.questionLayout);
        TextView questionText = view.findViewById(R.id.questionText);
        RadioGroup ratingGroup = view.findViewById(R.id.ratingGroup);
        Button nextQuestionButton = view.findViewById(R.id.nextQuestionButton);

        String[] questions = {
                "Q. 나는 아침 식사를 자주 한다.",
                "Q. 나는 하루 한 끼를 규칙적으로 먹는다."
        };

        final int[] currentQuestionIndex = {0};

        randomSurveyButton.setOnClickListener(v -> {
            randomSurveyButton.setVisibility(View.GONE);
            questionLayout.setVisibility(View.VISIBLE);
        });

        nextQuestionButton.setOnClickListener(v -> {
            currentQuestionIndex[0] = (currentQuestionIndex[0] + 1) % questions.length;
            questionText.setText(questions[currentQuestionIndex[0]]);
            ratingGroup.clearCheck();
        });

        return view;
    }
}