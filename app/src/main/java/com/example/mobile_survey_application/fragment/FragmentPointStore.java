package com.example.mobile_survey_application.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.mobile_survey_application.R;
import android.app.AlertDialog;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentPointStore#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentPointStore extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int currentPoint = 10000;
    private TextView tvCurrentPoint;

    public FragmentPointStore() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentPointStore.
     */
    // TODO: Rename and change types and number of parameters
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_point_store, container, false);

        tvCurrentPoint = view.findViewById(R.id.tvCurrentPoint);
        updateCurrentPointText();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnExchangeStarbucks).setOnClickListener(v -> {
            showConfirmDialog("스타벅스 아메리카노", 5000);
        });

        view.findViewById(R.id.btnExchangeCU).setOnClickListener(v -> {
            showConfirmDialog("CU 5,000원 상품권", 5000);
        });

        view.findViewById(R.id.btnExchangeTwosome).setOnClickListener(v -> {
            showConfirmDialog("투썸플레이스 케이크", 8000);
        });

        view.findViewById(R.id.btnExchangeGS25).setOnClickListener(v -> {
            showConfirmDialog("GS25 3,000원 상품권", 3000);
        });

        return view;
    }

    private void updateCurrentPointText() {
        tvCurrentPoint.setText(String.format("%,d P", currentPoint));
    }

    private void showConfirmDialog(String productName, int requiredPoint) {
        new AlertDialog.Builder(requireContext())
                .setTitle("포인트를 사용하시겠어요?")
                .setMessage(productName + "를 " + String.format("%,d", requiredPoint) + "P로 교환합니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("교환하기", (dialog, which) -> {
                    if (currentPoint >= requiredPoint) {
                        currentPoint -= requiredPoint;
                        updateCurrentPointText();
                        showSuccessDialog();
                    } else {
                        showFailDialog();
                    }
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

    private void showFailDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("포인트가 부족합니다.")
                .setMessage("현재 포인트로는 교환할 수 없습니다.")
                .setPositiveButton("확인", null)
                .show();
    }
}