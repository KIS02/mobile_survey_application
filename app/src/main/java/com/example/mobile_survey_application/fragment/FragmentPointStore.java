package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.mobile_survey_application.R;
import android.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;
import data.network.RetrofitClient;

import java.util.List;

import model.RewardResponse;
import viewmodel.RewardViewModel;

public class FragmentPointStore extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    // [AS-IS] 하드코딩 버전
//    private int currentPoint = 10000;
//    private TextView tvCurrentPoint;

    // [추가] 리워드 API 연동을 위한 ViewModel
    private RewardViewModel rewardViewModel;

    private TextView tvCurrentPoint;
    // [추가] 서버 응답으로 동적 바인딩할 뷰 배열
    private final ImageView[] imageViews = new ImageView[4];
    private final TextView[] nameViews = new TextView[4];
    private final TextView[] priceViews = new TextView[4];
    private final TextView[] exchangeButtons = new TextView[4];

    public FragmentPointStore() {}

    public static FragmentPointStore newInstance(String param1, String param2) {
        FragmentPointStore fragment = new FragmentPointStore();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_point_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCurrentPoint = view.findViewById(R.id.tvCurrentPoint);
//        updateCurrentPointText(); // [AS-IS] 하드코딩 버전

        // [추가] 서버 이미지 로딩용 ImageView (XML: ivRewardImage1~4)
        imageViews[0] = view.findViewById(R.id.ivRewardImage1);
        imageViews[1] = view.findViewById(R.id.ivRewardImage2);
        imageViews[2] = view.findViewById(R.id.ivRewardImage3);
        imageViews[3] = view.findViewById(R.id.ivRewardImage4);

        // [추가] 리워드 이름/가격 TextView (XML: tvRewardName1~4, tvRewardPrice1~4)
        nameViews[0] = view.findViewById(R.id.tvRewardName1);
        nameViews[1] = view.findViewById(R.id.tvRewardName2);
        nameViews[2] = view.findViewById(R.id.tvRewardName3);
        nameViews[3] = view.findViewById(R.id.tvRewardName4);

        priceViews[0] = view.findViewById(R.id.tvRewardPrice1);
        priceViews[1] = view.findViewById(R.id.tvRewardPrice2);
        priceViews[2] = view.findViewById(R.id.tvRewardPrice3);
        priceViews[3] = view.findViewById(R.id.tvRewardPrice4);

        exchangeButtons[0] = view.findViewById(R.id.btnExchangeStarbucks);
        exchangeButtons[1] = view.findViewById(R.id.btnExchangeTwosome);
        exchangeButtons[2] = view.findViewById(R.id.btnExchangeCU);
        exchangeButtons[3] = view.findViewById(R.id.btnExchangeGS25);

                    for (TextView btn : exchangeButtons) {
                        btn.setEnabled(false);
                        btn.setAlpha(0.5f);
                    }

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        // [AS-IS] 하드코딩 버전 - 교환 버튼 클릭 리스너
//        view.findViewById(R.id.btnExchangeStarbucks).setOnClickListener(v -> {
//            showConfirmDialog("스타벅스 아메리카노", 5000);
//        });
//        view.findViewById(R.id.btnExchangeCU).setOnClickListener(v -> {
//            showConfirmDialog("CU 5,000원 상품권", 5000);
//        });
//        view.findViewById(R.id.btnExchangeTwosome).setOnClickListener(v -> {
//            showConfirmDialog("투썸플레이스 케이크", 8000);
//        });
//        view.findViewById(R.id.btnExchangeGS25).setOnClickListener(v -> {
//            showConfirmDialog("GS25 3,000원 상품권", 3000);
//        });

        // [추가] GET /api/rewards 호출 → bindRewards()에서 UI 반영
        rewardViewModel = new ViewModelProvider(this).get(RewardViewModel.class);

        rewardViewModel.getRewards().observe(getViewLifecycleOwner(), this::bindRewards);
        rewardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        );

        rewardViewModel.loadRewards();
    }

    // [추가] API 응답으로 리워드 카드 UI 동적 바인딩
    private void bindRewards(List<RewardResponse> rewards) {
        String baseUrl = RetrofitClient.getBaseUrl();

        for (int i = 0; i < exchangeButtons.length; i++) {
            if (i < rewards.size()) {
                RewardResponse reward = rewards.get(i);
                imageViews[i].setVisibility(View.VISIBLE);
                nameViews[i].setVisibility(View.VISIBLE);
                priceViews[i].setVisibility(View.VISIBLE);
                exchangeButtons[i].setVisibility(View.VISIBLE);

                // [추가] Glide로 서버 이미지 로딩 (BASE_URL + imagePath)
                String imageUrl = baseUrl.replaceAll("/$", "") + reward.getImagePath();
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(android.R.color.darker_gray)
                        .error(android.R.color.darker_gray)
                        .into(imageViews[i]);

                nameViews[i].setText(reward.getName());
                priceViews[i].setText(String.format("%,d P", reward.getRequiredCredit()));
                exchangeButtons[i].setEnabled(true);
                exchangeButtons[i].setAlpha(1.0f);
                final RewardResponse r = reward;
                exchangeButtons[i].setOnClickListener(v ->
                        showConfirmDialog(r.getName(), r.getRequiredCredit())
                );
            } else {
                imageViews[i].setVisibility(View.GONE);
                nameViews[i].setVisibility(View.GONE);
                priceViews[i].setVisibility(View.GONE);
                exchangeButtons[i].setVisibility(View.GONE);
            }
        }
    }

    // 하드코딩 버전
//    private void updateCurrentPointText() {
//        tvCurrentPoint.setText(String.format("%,d P", currentPoint));
//    }

    private void showConfirmDialog(String productName, int requiredPoint) {
        new AlertDialog.Builder(requireContext())
                .setTitle("포인트를 사용하시겠어요?")
                .setMessage(productName + "를 " + String.format("%,d", requiredPoint) + "P로 교환합니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("교환하기", (dialog, which) -> {
                    // TODO: 교환 API 연동
                    showSuccessDialog();
                })
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("교환이 완료되었습니다!")
                .setMessage("쿠폰이 지급되었습니다.\n마이페이지에서 확인해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }

    // 하드코딩 버전
//    private void showFailDialog() {
//        new AlertDialog.Builder(requireContext())
//                .setTitle("포인트가 부족합니다.")
//                .setMessage("현재 포인트로는 교환할 수 없습니다.")
//                .setPositiveButton("확인", null)
//                .show();
//    }
}
