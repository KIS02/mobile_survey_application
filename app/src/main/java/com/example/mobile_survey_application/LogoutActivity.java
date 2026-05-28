package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import data.local.TokenManager;

public class LogoutActivity extends AppCompatActivity {

    private static final String WEB_CLIENT_ID =
            "947790610190-k6bsq5uob4g82fh2nme2abo8o43npv83.apps.googleusercontent.com";

    private Button btnGoogleLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnGoogleLogin.setEnabled(false);

        TokenManager tokenManager = new TokenManager(this);
        tokenManager.clearTokens();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> btnGoogleLogin.setEnabled(true));

        btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LogoutActivity.this, LoginTestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
