package com.example.mobile_survey_application;

import static androidx.core.content.ContentProviderCompat.requireContext;

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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.mobile_survey_application.fragment.FragmentSurvey;

public class HomeActivity extends AppCompatActivity {

    // 자동저장 관련변수
    private static final String PREF_SURVEY_DRAFT = "survey_draft";
    private static final String KEY_HAS_DRAFT = "has_draft";
    private static final String KEY_SURVEY_ID = "survey_id";
    private boolean resumeDialogShown = false;

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
        checkSurveyDraft();
        //endregion
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToSetting() {
        bottomNavigationView.setSelectedItemId(R.id.setting);
    }

    //region ### 설문 자동저장 관련함수 ###
    private void checkSurveyDraft() {
        SharedPreferences prefs = getSharedPreferences(PREF_SURVEY_DRAFT, MODE_PRIVATE);

        boolean hasDraft = prefs.getBoolean(KEY_HAS_DRAFT, false);
        long surveyId = prefs.getLong(KEY_SURVEY_ID, -1L);

        if (!hasDraft || surveyId == -1L || resumeDialogShown) {
            return;
        }

        resumeDialogShown = true;

        new AlertDialog.Builder(this)
                .setTitle("작성 중인 설문이 있습니다.")
                .setMessage("이전에 작성하던 설문을 이어서 작성할까요?")
                .setNegativeButton("삭제", (dialog, which) -> {
                    clearSurveyDraftFromHome();
                })
                .setPositiveButton("이어하기", (dialog, which) -> {
                    Intent intent = new Intent(HomeActivity.this, SurveyActivity.class);
                    intent.putExtra(FragmentSurvey.EXTRA_SURVEY_ID, surveyId);
                    startActivity(intent);
                })
                .show();
    }

    private void clearSurveyDraftFromHome() {
        SharedPreferences prefs = getSharedPreferences(PREF_SURVEY_DRAFT, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    //endregion
}