package com.example.mobile_survey_application.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.R;
import com.example.mobile_survey_application.util.DateTimeFormatUtil;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

import data.local.TokenManager;
import data.repository.UserRepository;
import model.CouponResponse;

public class FragmentCouponBox extends Fragment {

    public FragmentCouponBox() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_coupon_box, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        LinearLayout couponContainer = view.findViewById(R.id.couponContainer);
        TokenManager tokenManager = new TokenManager(requireContext());
        String accessToken = tokenManager.getAccessToken();

        new UserRepository().getCoupons(accessToken, new UserRepository.CouponCallback() {
            @Override
            public void onSuccess(List<CouponResponse> coupons) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    couponContainer.removeAllViews();
                    if (coupons.isEmpty()) {
                        TextView empty = new TextView(getContext());
                        empty.setText("보유한 쿠폰이 없습니다.");
                        empty.setTextSize(14);
                        empty.setPadding(0, 48, 0, 0);
                        couponContainer.addView(empty);
                        return;
                    }
                    for (CouponResponse coupon : coupons) {
                        renderCouponItem(inflater, couponContainer, coupon);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });

        return view;
    }

    private void renderCouponItem(LayoutInflater inflater, LinearLayout container, CouponResponse coupon) {
        View couponView = inflater.inflate(R.layout.item_coupon, container, false);

        TextView txtTitle  = couponView.findViewById(R.id.txtCouponTitle);
        TextView txtStatus = couponView.findViewById(R.id.txtCouponStatus);
        TextView txtDate   = couponView.findViewById(R.id.txtCouponDate);
        TextView txtCode   = couponView.findViewById(R.id.txtCouponCode);
        TextView btnBarcode = couponView.findViewById(R.id.btnBarcode);
        ImageView ivBarcode = couponView.findViewById(R.id.ivBarcode);

        txtTitle.setText(coupon.getRewardName());
        txtStatus.setText(Boolean.TRUE.equals(coupon.getIsUsed()) ? "사용 완료" : "사용 가능");
        txtDate.setText(DateTimeFormatUtil.formatDisplayDate(coupon.getExpiredAt()) + "까지");
        txtCode.setText(coupon.getBarcode());

        btnBarcode.setOnClickListener(v -> {
            if (ivBarcode.getVisibility() == View.VISIBLE) {
                ivBarcode.setVisibility(View.GONE);
                btnBarcode.setText("바코드 표시");
            } else {
                Bitmap bitmap = generateBarcode(coupon.getBarcode());
                if (bitmap != null) {
                    ivBarcode.setImageBitmap(bitmap);
                    ivBarcode.setVisibility(View.VISIBLE);
                    btnBarcode.setText("바코드 닫기");
                }
            }
        });

        container.addView(couponView);
    }

    private Bitmap generateBarcode(String content) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(content, BarcodeFormat.CODE_128, 800, 200);
        } catch (Exception e) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "바코드 생성 실패", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

}
