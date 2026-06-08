package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import data.local.TokenManager;

public class AdminActivity extends AppCompatActivity {

    private static final String WEB_CLIENT_ID = "947790610190-k6bsq5uob4g82fh2nme2abo8o43npv83.apps.googleusercontent.com";

    private TokenManager tokenManager;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tokenManager = new TokenManager(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.card_create_survey).setOnClickListener(v ->
                startActivity(new Intent(this, CreateSurveyActivity.class)));

        findViewById(R.id.card_manage_survey).setOnClickListener(v ->
                startActivity(new Intent(this, SurveyManageActivity.class)));

        TextView tvLogout = findViewById(R.id.tv_admin_logout);
        tvLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        tokenManager.clearTokens();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(this, LoginTestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
