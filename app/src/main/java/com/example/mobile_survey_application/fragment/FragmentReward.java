package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.util.DateTimeFormatUtil;
import com.example.mobile_survey_application.util.HeaderSettingsHelper;

import java.util.ArrayList;
import java.util.List;

import data.local.TokenManager;
import model.CreditHistoryResponse;
import viewmodel.RewardViewModel;
import viewmodel.UserViewModel;

public class FragmentReward extends Fragment {

    private static final String TAG_REWARD_DEBUG = "REWARD_DEBUG";

    // [추가] 서버에서 받아온 크레딧을 표시할 TextView
    private TextView tvCurrentCredit;
    private TextView txtEmptyRewardHistory;
    private LinearLayout rewardHistoryContainer;
    // [추가] GET /api/user/me 호출을 위한 ViewModel
    private UserViewModel userViewModel;
    private RewardViewModel rewardViewModel;
    private TokenManager tokenManager;

    public FragmentReward() {}

    public static FragmentReward newInstance(String param1, String param2) {
        FragmentReward fragment = new FragmentReward();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reward, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HeaderSettingsHelper.bindNavigateToSetting(view, this);
        tvCurrentCredit = view.findViewById(R.id.tvCurrentCredit);
        txtEmptyRewardHistory = view.findViewById(R.id.txtEmptyRewardHistory);
        rewardHistoryContainer = view.findViewById(R.id.rewardHistoryContainer);

        view.findViewById(R.id.btnUsePoint).setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new FragmentPointStore())
                        .addToBackStack(null)
                        .commit()
        );

        // [추가] UserViewModel으로 GET /api/user/me 호출
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        rewardViewModel = new ViewModelProvider(this).get(RewardViewModel.class);
        tokenManager = new TokenManager(requireContext());

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getCredit() != null) {
                // [추가] 서버 응답의 credit 값을 포맷하여 표시
                tvCurrentCredit.setText(String.format("%,d P", user.getCredit()));
            }
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );

        rewardViewModel.getCreditHistories().observe(getViewLifecycleOwner(), this::renderEarningHistories);
        rewardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );

        loadRewardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userViewModel != null && rewardViewModel != null && tvCurrentCredit != null) {
            loadRewardData();
        }
    }

    private void loadRewardData() {
        loadMyCredit();
        loadCreditHistory();
    }

    private void loadMyCredit() {
        String token = tokenManager.getAccessToken();
        if (token != null) {
            userViewModel.loadMe(token);
        } else {
            tvCurrentCredit.setText("로그인 필요");
        }
    }

    private void loadCreditHistory() {
        String token = tokenManager.getAccessToken();
        if (token != null && !token.isEmpty()) {
            rewardViewModel.loadCreditHistory(token);
        } else {
            showEmptyHistory("로그인이 필요합니다.");
        }
    }

    private void renderEarningHistories(List<CreditHistoryResponse> histories) {
        List<CreditHistoryResponse> earnings = new ArrayList<>();

        if (histories != null) {
            for (CreditHistoryResponse history : histories) {
                if (isEarningHistory(history)) {
                    earnings.add(history);
                }
            }
        }

        Log.d(TAG_REWARD_DEBUG, "earning history filtered count=" + earnings.size());
        rewardHistoryContainer.removeAllViews();

        if (earnings.isEmpty()) {
            showEmptyHistory("아직 적립 내역이 없습니다.");
            return;
        }

        txtEmptyRewardHistory.setVisibility(View.GONE);
        rewardHistoryContainer.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CreditHistoryResponse history : earnings) {
            View historyView = inflater.inflate(R.layout.item_point_history, rewardHistoryContainer, false);
            TextView txtTitle = historyView.findViewById(R.id.txtPointTitle);
            TextView txtDate = historyView.findViewById(R.id.txtPointDate);
            TextView txtAmount = historyView.findViewById(R.id.txtPointAmount);

            txtTitle.setText(valueOrDefault(history.getDescription(), "설문 참여"));
            String formattedDate = DateTimeFormatUtil.formatDisplayDateTime(history.getCreatedAt());
            txtDate.setText(formattedDate.isEmpty() ? "날짜 정보 없음" : formattedDate);
            txtAmount.setText(formatEarningAmount(history.getAmount()));

            rewardHistoryContainer.addView(historyView);
        }
    }

    private boolean isEarningHistory(CreditHistoryResponse history) {
        if (history == null) {
            return false;
        }

        Integer amount = history.getAmount();
        String type = history.getType();
        return (type != null && "SURVEY".equalsIgnoreCase(type))
                || (amount != null && amount > 0);
    }

    private void showEmptyHistory(String message) {
        if (rewardHistoryContainer != null) {
            rewardHistoryContainer.removeAllViews();
            rewardHistoryContainer.setVisibility(View.GONE);
        }
        if (txtEmptyRewardHistory != null) {
            txtEmptyRewardHistory.setText(message);
            txtEmptyRewardHistory.setVisibility(View.VISIBLE);
        }
    }

    private String formatEarningAmount(Integer amount) {
        if (amount == null) {
            return "+0 p";
        }

        return String.format("+%,d p", Math.abs(amount));
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value;
    }
}
