package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_survey_application.R;

import data.local.TokenManager;
import viewmodel.UserViewModel;

public class FragmentReward extends Fragment {

    // [추가] 서버에서 받아온 크레딧을 표시할 TextView
    private TextView tvCurrentCredit;
    // [추가] GET /api/user/me 호출을 위한 ViewModel
    private UserViewModel userViewModel;

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

        tvCurrentCredit = view.findViewById(R.id.tvCurrentCredit);

        view.findViewById(R.id.btnUsePoint).setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new FragmentPointStore())
                        .addToBackStack(null)
                        .commit()
        );

        // [추가] UserViewModel으로 GET /api/user/me 호출
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getCredit() != null) {
                // [추가] 서버 응답의 credit 값을 포맷하여 표시
                tvCurrentCredit.setText(String.format("%,d P", user.getCredit()));
            }
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );

        // [추가] TokenManager에서 저장된 JWT 꺼내 API 호출
        String token = new TokenManager(requireContext()).getAccessToken();
        if (token != null) {
            userViewModel.loadMe(token);
        } else {
            tvCurrentCredit.setText("로그인 필요");
        }
    }
}
