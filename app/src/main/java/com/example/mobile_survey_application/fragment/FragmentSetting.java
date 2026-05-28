package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.LogoutActivity;
import com.example.mobile_survey_application.WithdrawActivity;
import com.example.mobile_survey_application.R;

import data.local.TokenManager;
import data.repository.UserRepository;

public class FragmentSetting extends Fragment {

    private LinearLayout layoutLogout;
    private LinearLayout layoutWithdraw;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        layoutLogout = view.findViewById(R.id.layoutLogout);
        layoutWithdraw = view.findViewById(R.id.layoutWithdraw);

        layoutLogout.setOnClickListener(v -> showLogoutDialog());
        layoutWithdraw.setOnClickListener(v -> showWithdrawDialog());

        return view;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃 하시겠어요?")
                .setMessage("현재 계정에서 로그아웃됩니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), LogoutActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }

    private void showWithdrawDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("정말 탈퇴하시겠어요?")
                .setMessage("탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("탈퇴하기", (dialog, which) -> requestWithdraw())
                .show();
    }

    private void requestWithdraw() {
        TokenManager tokenManager = new TokenManager(requireContext());
        String accessToken = tokenManager.getAccessToken();

        if (accessToken == null) {
            Toast.makeText(getContext(), "로그인 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new UserRepository().withdraw(accessToken, new UserRepository.WithdrawCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    tokenManager.clearTokens();
                    Intent intent = new Intent(requireContext(), WithdrawActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "탈퇴 실패: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
