package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.R;

public class FragmentCouponBox extends Fragment {

    public FragmentCouponBox() {
        // Required empty public constructor
    }

    private static class Coupon {
        String title;
        String status;
        String date;
        String code;

        Coupon(String title, String status, String date, String code) {
            this.title = title;
            this.status = status;
            this.date = date;
            this.code = code;
        }
    }

    private final Coupon[] coupons = {
            new Coupon("스타벅스 아메리카노", "사용 가능", "2026. 12. 31까지", "SBUX-1234-5678"),
            new Coupon("편의점 3,000원 교환권", "사용 가능", "2026. 11. 30까지", "CVS-5678-1234"),
            new Coupon("영화관 할인 쿠폰", "사용 가능", "2026. 10. 15까지", "MOVIE-2468-1357")
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_coupon_box, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        LinearLayout couponContainer = view.findViewById(R.id.couponContainer);

        for (Coupon coupon : coupons) {
            View couponView = inflater.inflate(R.layout.item_coupon, couponContainer, false);

            TextView txtTitle = couponView.findViewById(R.id.txtCouponTitle);
            TextView txtStatus = couponView.findViewById(R.id.txtCouponStatus);
            TextView txtDate = couponView.findViewById(R.id.txtCouponDate);
            TextView txtCode = couponView.findViewById(R.id.txtCouponCode);
            TextView btnBarcode = couponView.findViewById(R.id.btnBarcode);

            txtTitle.setText(coupon.title);
            txtStatus.setText(coupon.status);
            txtDate.setText(coupon.date);
            txtCode.setText(coupon.code);

            btnBarcode.setOnClickListener(v -> {
                Toast.makeText(getContext(), coupon.code, Toast.LENGTH_SHORT).show();
            });

            couponContainer.addView(couponView);
        }

        return view;
    }
}