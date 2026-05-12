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

public class HomeActivity extends AppCompatActivity {

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
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}