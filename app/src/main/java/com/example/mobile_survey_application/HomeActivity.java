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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.v("test", "홈열림" );
    }
}
