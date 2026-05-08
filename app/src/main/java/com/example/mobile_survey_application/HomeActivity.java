package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

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

import viewmodel.survey_viewmodel;

public class HomeActivity extends AppCompatActivity {

    Fragment screen_home = new FragmentHome();
    Fragment screen_reward = new FragmentReward();
    Fragment screen_profile = new FragmentProfile();
    Fragment screen_survey = new FragmentSurvey();
    Fragment screen_setting = new FragmentSetting();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.home);


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = screen_home;

            int itemId = item.getItemId();

            if (itemId == R.id.survey) {
                selectedFragment = screen_survey;
            } else if (itemId == R.id.search) {
                selectedFragment = screen_reward;
            } else if (itemId == R.id.home) {
                selectedFragment = screen_home;
            } else if (itemId == R.id.profile) {
                selectedFragment = screen_profile;
            } else if (itemId == R.id.setting) {
                selectedFragment = screen_setting;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, screen_home)
                .commit();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.v("test", "홈열림" );
    }
}
