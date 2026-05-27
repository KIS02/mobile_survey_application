package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_survey_application.R;

import java.util.List;

import data.local.TokenManager;
import model.CreditHistoryResponse;
import viewmodel.RewardViewModel;
import viewmodel.UserViewModel;

public class FragmentPointHistory extends Fragment {
    private static final String TAG_POINT_HISTORY_DEBUG = "POINT_HISTORY_DEBUG";

    private TokenManager tokenManager;
    private UserViewModel userViewModel;
    private RewardViewModel rewardViewModel;
    private LinearLayout pointHistoryContainer;
    private TextView txtCurrentPoint;

    public FragmentPointHistory() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_point_history, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        txtCurrentPoint = view.findViewById(R.id.txtCurrentPoint);
        pointHistoryContainer = view.findViewById(R.id.pointHistoryContainer);

        tokenManager = new TokenManager(requireContext());
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        rewardViewModel = new ViewModelProvider(this).get(RewardViewModel.class);
        observeViewModel();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentCredit();
        loadPointHistory();
    }

    private void observeViewModel() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getCredit() != null) {
                Log.d(TAG_POINT_HISTORY_DEBUG, "loadMyCredit success credit=" + user.getCredit());
                if (txtCurrentPoint != null) {
                    txtCurrentPoint.setText(String.format("%,d P", user.getCredit()));
                }
            }
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty()) return;
            Log.e(TAG_POINT_HISTORY_DEBUG, "loadMyCredit failed: " + message);
            if (txtCurrentPoint != null && (txtCurrentPoint.getText() == null || txtCurrentPoint.getText().toString().trim().isEmpty())) {
                txtCurrentPoint.setText("0 P");
            }
        });

        rewardViewModel.getCreditHistories().observe(getViewLifecycleOwner(), this::renderPointHistories);

        rewardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty()) return;

            Log.e(TAG_POINT_HISTORY_DEBUG, "loadCreditHistory failed: " + message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCurrentCredit() {
        if (tokenManager == null) return;

        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            if (txtCurrentPoint != null) {
                txtCurrentPoint.setText("0 P");
            }
            return;
        }

        Log.d(TAG_POINT_HISTORY_DEBUG, "loadMyCredit request");
        userViewModel.loadMe(accessToken);
    }

    private void loadPointHistory() {
        if (tokenManager == null) return;

        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG_POINT_HISTORY_DEBUG, "loadCreditHistory request");
        rewardViewModel.loadCreditHistory(accessToken);
    }

    private void renderPointHistories(List<CreditHistoryResponse> histories) {
        if (pointHistoryContainer == null) return;

        pointHistoryContainer.removeAllViews();

        if (histories == null || histories.isEmpty()) {
            pointHistoryContainer.addView(createEmptyView());
            return;
        }

        Log.d(TAG_POINT_HISTORY_DEBUG, "loadCreditHistory success count=" + histories.size());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CreditHistoryResponse history : histories) {
            Log.d(TAG_POINT_HISTORY_DEBUG,
                    "history type=" + history.getType()
                            + ", description=" + history.getDescription()
                            + ", amount=" + history.getAmount()
                            + ", createdAt=" + history.getCreatedAt());

            View historyView = inflater.inflate(R.layout.item_point_history, pointHistoryContainer, false);

            TextView txtTitle = historyView.findViewById(R.id.txtPointTitle);
            TextView txtDate = historyView.findViewById(R.id.txtPointDate);
            TextView txtAmount = historyView.findViewById(R.id.txtPointAmount);

            txtTitle.setText(valueOrEmpty(history.getDescription()));
            txtDate.setText(valueOrEmpty(history.getCreatedAt()));
            txtAmount.setText(formatAmount(history.getAmount()));

            pointHistoryContainer.addView(historyView);
        }
    }

    private TextView createEmptyView() {
        TextView empty = new TextView(requireContext());
        empty.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        empty.setPadding(0, 40, 0, 40);
        empty.setText("포인트 내역이 없습니다.");
        empty.setTextColor(0xFF777777);
        empty.setTextSize(14f);
        return empty;
    }

    private String formatAmount(Integer amount) {
        if (amount == null) return "0p";
        if (amount > 0) return "+" + amount + "p";
        return amount + "p";
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}