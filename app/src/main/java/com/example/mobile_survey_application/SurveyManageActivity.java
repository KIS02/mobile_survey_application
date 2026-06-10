package com.example.mobile_survey_application;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_survey_application.util.EdgeToEdgeHelper;

import java.util.List;

import model.admin.AdminSurveyListItem;
import viewmodel.AdminViewModel;

public class SurveyManageActivity extends AppCompatActivity {

    private AdminViewModel adminViewModel;
    private LinearLayout containerSurveys;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_manage);
        EdgeToEdgeHelper.enableWithRootInsets(this, R.id.root_layout);

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        containerSurveys = findViewById(R.id.container_surveys);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adminViewModel.loadSurveys();
    }

    private void observeViewModel() {
        adminViewModel.getIsLoading().observe(this, loading ->
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        adminViewModel.getSurveyList().observe(this, this::renderList);

        adminViewModel.getActionSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                adminViewModel.clearActionSuccess();
                Toast.makeText(this, "처리되었습니다.", Toast.LENGTH_SHORT).show();
                adminViewModel.loadSurveys();
            }
        });

        adminViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                adminViewModel.clearErrorMessage();
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderList(List<AdminSurveyListItem> surveys) {
        containerSurveys.removeAllViews();
        if (surveys == null || surveys.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("등록된 설문이 없습니다.");
            tv.setPadding(0, 32, 0, 0);
            containerSurveys.addView(tv);
            return;
        }
        for (AdminSurveyListItem survey : surveys) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_survey_manage, containerSurveys, false);
            bindItem(itemView, survey);
            containerSurveys.addView(itemView);
        }
    }

    private void bindItem(View v, AdminSurveyListItem survey) {
        TextView tvTitle = v.findViewById(R.id.tv_survey_title);
        TextView tvBadge = v.findViewById(R.id.tv_status_badge);
        TextView tvMeta  = v.findViewById(R.id.tv_survey_meta);
        TextView tvCount = v.findViewById(R.id.tv_response_count);
        Button btnEdit         = v.findViewById(R.id.btn_edit);
        Button btnStatus       = v.findViewById(R.id.btn_status_change);
        Button btnCloseDraft   = v.findViewById(R.id.btn_close_draft);
        Button btnRevertDraft  = v.findViewById(R.id.btn_revert_draft);
        Button btnDelete       = v.findViewById(R.id.btn_delete);

        tvTitle.setText(survey.getTitle());

        // 상태 배지
        tvBadge.setText(survey.getStatus());
        switch (survey.getStatus()) {
            case "ACTIVE":
                tvBadge.getBackground().setTint(0xFF388E3C);
                break;
            case "CLOSED":
                tvBadge.getBackground().setTint(0xFFB71C1C);
                break;
            default: // DRAFT
                tvBadge.getBackground().setTint(0xFF9E9E9E);
        }

        // 메타 정보
        String cat = survey.getCategoryName() != null ? survey.getCategoryName() : "-";
        tvMeta.setText("카테고리: " + cat
                + "  |  문항: " + survey.getQuestionCount() + "개"
                + "  |  리워드: " + survey.getReward() + "P");

        // 응답 현황
        String targetStr = survey.getTargetCount() != null
                ? String.valueOf(survey.getTargetCount()) : "미설정";
        tvCount.setText("응답: " + survey.getResponseCount() + " / " + targetStr);

        // 수정 버튼 (CLOSED는 수정 불가 — 서버가 INVALID_STATUS_TRANSITION 반환)
        if ("CLOSED".equals(survey.getStatus())) {
            btnEdit.setEnabled(false);
            btnEdit.setAlpha(0.4f);
        } else {
            btnEdit.setOnClickListener(x -> {
                Intent intent = new Intent(this, SurveyEditActivity.class);
                intent.putExtra("surveyId", survey.getId());
                intent.putExtra("status", survey.getStatus());
                startActivity(intent);
            });
        }

        // 상태 변경 버튼
        if ("DRAFT".equals(survey.getStatus())) {
            btnRevertDraft.setVisibility(View.GONE);
            // 활성화 버튼
            btnStatus.setText("활성화");
            btnStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE8F5E9));
            btnStatus.setTextColor(0xFF2E7D32);
            btnStatus.setOnClickListener(x -> confirmStatusChange(survey, "ACTIVE", "활성화"));
            // DRAFT 전용 마감 버튼
            btnCloseDraft.setVisibility(View.VISIBLE);
            btnCloseDraft.setOnClickListener(x -> confirmStatusChange(survey, "CLOSED", "마감"));
        } else if ("ACTIVE".equals(survey.getStatus())) {
            btnCloseDraft.setVisibility(View.GONE);
            btnStatus.setText("마감");
            btnStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFFF3E0));
            btnStatus.setTextColor(0xFFE65100);
            btnStatus.setOnClickListener(x -> confirmStatusChange(survey, "CLOSED", "마감"));
            // ACTIVE → DRAFT 되돌리기 버튼 (기존 응답 유지)
            btnRevertDraft.setVisibility(View.VISIBLE);
            btnRevertDraft.setOnClickListener(x ->
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("설문 중지")
                            .setMessage("'" + survey.getTitle() + "' 설문을 중지하고 초안으로 되돌리시겠습니까?\n\n기존 응답 데이터는 유지됩니다.")
                            .setPositiveButton("확인", (d, w) -> adminViewModel.updateStatus(survey.getId(), "DRAFT"))
                            .setNegativeButton("취소", null)
                            .show());
        } else {
            btnCloseDraft.setVisibility(View.GONE);
            btnRevertDraft.setVisibility(View.GONE);
            btnStatus.setEnabled(false);
            btnStatus.setText("종료됨");
        }

        // 삭제 버튼 (DRAFT, CLOSED 허용 / ACTIVE 불가)
        if ("ACTIVE".equals(survey.getStatus())) {
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.4f);
        } else {
            btnDelete.setEnabled(true);
            btnDelete.setAlpha(1.0f);
            boolean isClosed = "CLOSED".equals(survey.getStatus());
            btnDelete.setOnClickListener(x -> confirmDelete(survey, isClosed));
        }
    }

    private void confirmStatusChange(AdminSurveyListItem survey, String newStatus, String label) {
        new AlertDialog.Builder(this)
                .setTitle("상태 변경")
                .setMessage("'" + survey.getTitle() + "' 설문을 " + label + "하시겠습니까?")
                .setPositiveButton("확인", (d, w) -> adminViewModel.updateStatus(survey.getId(), newStatus))
                .setNegativeButton("취소", null)
                .show();
    }

    private void confirmDelete(AdminSurveyListItem survey, boolean isClosed) {
        new AlertDialog.Builder(this)
                .setTitle("설문 삭제")
                .setMessage("'" + survey.getTitle() + "' 설문을 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> adminViewModel.deleteSurvey(survey.getId()))
                .setNegativeButton("취소", null)
                .show();
    }
}
