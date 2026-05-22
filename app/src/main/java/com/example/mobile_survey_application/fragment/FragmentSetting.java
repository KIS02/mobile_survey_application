package com.example.mobile_survey_application.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.LogoutActivity;
import com.example.mobile_survey_application.WithdrawActivity;
import com.example.mobile_survey_application.R;

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
                    startActivity(intent);
                })
                .show();
    }

    private void showWithdrawDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("정말 탈퇴하시겠어요?")
                .setMessage("탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("탈퇴하기", (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), WithdrawActivity.class);
                    startActivity(intent);
                })
                .show();
    }
}