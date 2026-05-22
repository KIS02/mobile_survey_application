package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.R;

public class FragmentPointHistory extends Fragment {

    public FragmentPointHistory() {
        // Required empty public constructor
    }

    private static class PointHistory {
        String title;
        String date;
        String amount;

        PointHistory(String title, String date, String amount) {
            this.title = title;
            this.date = date;
            this.amount = amount;
        }
    }

    private final PointHistory[] histories = {
            new PointHistory("스타벅스 아메리카노 교환", "2026. 05. 14", "-1,500P"),
            new PointHistory("편의점 쿠폰 교환", "2026. 05. 10", "-3,000P"),
            new PointHistory("영화관 할인 쿠폰 교환", "2026. 05. 03", "-2,000P")
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_point_history, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        LinearLayout pointHistoryContainer = view.findViewById(R.id.pointHistoryContainer);

        for (PointHistory history : histories) {
            View historyView = inflater.inflate(R.layout.item_point_history, pointHistoryContainer, false);

            TextView txtTitle = historyView.findViewById(R.id.txtPointTitle);
            TextView txtDate = historyView.findViewById(R.id.txtPointDate);
            TextView txtAmount = historyView.findViewById(R.id.txtPointAmount);

            txtTitle.setText(history.title);
            txtDate.setText(history.date);
            txtAmount.setText(history.amount);

            pointHistoryContainer.addView(historyView);
        }

        return view;
    }
}