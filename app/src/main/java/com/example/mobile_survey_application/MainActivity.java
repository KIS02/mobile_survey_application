package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import viewmodel.survey_viewmodel;

public class MainActivity extends AppCompatActivity {

    private short autoDelayTime = 5000;

    private Handler autoHomeHandler = new Handler();
    private Runnable runable_delay = this::startApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.v("test", "test : " + survey_viewmodel.request_user_id() );

        // >> 화면을 클릭하거나 가만히 5초간 있으면 다음화면으로 넘어감, 이때 로그인 정보가 있다면 Home으로 그렇지 않으면 튜토리얼로
        LinearLayout root = findViewById(R.id.root_layout);

        // 클릭 시 넘어가는 기능
        root.setOnClickListener(v -> {
            v.setEnabled(false); // 연타방지
            startApplication();
        });

        // 가만히 있으면 넘어가는 기능
        autoHomeHandler.postDelayed(runable_delay, autoDelayTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoHomeHandler.removeCallbacks(runable_delay);
    }

    // Application 처음 시작 시 Home으로 이동하는 함수
    private void startApplication(){
        autoHomeHandler.removeCallbacks(runable_delay);

        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);

        finish();
    }


}