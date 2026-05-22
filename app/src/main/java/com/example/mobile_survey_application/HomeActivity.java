package com.example.mobile_survey_application;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.mobile_survey_application.fragment.FragmentHome;
import com.example.mobile_survey_application.fragment.FragmentProfile;
import com.example.mobile_survey_application.fragment.FragmentReward;
import com.example.mobile_survey_application.fragment.FragmentSetting;
import com.example.mobile_survey_application.fragment.FragmentSurvey;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    // 자동저장 관련변수
    private SharedPreferences surveyPrefs;
    private static final String PREF_NAME = "survey_auto_save";
    private static final String KEY_DRAFT_JSON = "survey_draft_json";

    BottomNavigationView bottomNavigationView;

    Fragment fragment_home = new FragmentHome();
    Fragment fragment_reward = new FragmentReward();
    Fragment fragment_profile = new FragmentProfile();
    Fragment fragment_survey = new FragmentSurvey();
    Fragment fragment_setting = new FragmentSetting();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.v("test", "홈열림");

        bottomNavigationView = findViewById(R.id.bottom_nav);

        replaceFragment(fragment_home);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.survey) {
                replaceFragment(fragment_survey);
                return true;
            } else if (itemId == R.id.reward) {
                replaceFragment(fragment_reward);
                return true;
            } else if (itemId == R.id.home) {
                replaceFragment(fragment_home);
                return true;
            } else if (itemId == R.id.profile) {
                replaceFragment(fragment_profile);
                return true;
            } else if (itemId == R.id.setting) {
                replaceFragment(fragment_setting);
                return true;
            }

            return false;
        });

        //region ### 자동저장 확인 ###
        surveyPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (hasAutoSavedSurvey()) {
            findViewById(android.R.id.content).post(() -> {
                showResumeSurveyDialog();
            });
        }
        //endregion
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    //region ### 설문 자동저장 관련함수 ###
    private boolean hasAutoSavedSurvey() {
        String jsonString = surveyPrefs.getString(KEY_DRAFT_JSON, null);

        if (jsonString == null) {
            return false;
        }

        try {
            JSONObject draftJson = new JSONObject(jsonString);
            return draftJson.optBoolean("hasDraft", false);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void clearAutoSavedSurvey() {
        surveyPrefs.edit()
                .remove(KEY_DRAFT_JSON)
                .apply();
    }

    private void showResumeSurveyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("작성 중인 설문이 있습니다")
                .setMessage("이전에 자동저장된 설문을 이어서 작성하시겠습니까?")
                .setNegativeButton("아니오", (dialog, which) -> {
                    showDeleteDraftConfirmDialog();
                })
                .setPositiveButton("예", (dialog, which) -> {
                    Intent intent = new Intent(HomeActivity.this, SurveyActivity.class);
                    startActivity(intent);
                })
                .show();
    }

    private void showDeleteDraftConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("자동저장본을 삭제할까요?")
                .setMessage("삭제하면 이전에 작성하던 설문 내용을 복원할 수 없습니다.")
                .setNegativeButton("취소", (dialog, which) -> {
                    showResumeSurveyDialog();
                })
                .setPositiveButton("삭제", (dialog, which) -> {
                    clearAutoSavedSurvey();
                })
                .show();
    }
    //endregion
}