package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import viewmodel.AuthViewModel;

public class LoginTestActivity extends AppCompatActivity {

    // 구글 클라이언트 아이디
    private static final String WEB_CLIENT_ID = "947790610190-k6bsq5uob4g82fh2nme2abo8o43npv83.apps.googleusercontent.com";

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    private Button btnGoogleLogin;
    private ProgressBar progressBar;
    private TextView tvResult;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    String idToken = account.getIdToken();
                    authViewModel.googleLogin(idToken);
                } catch (ApiException e) {
                    tvResult.setText("구글 로그인 실패: " + e.getStatusCode());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnGoogleLogin = findViewById(R.id.btn_google_login);
        progressBar = findViewById(R.id.progress_bar);
        tvResult = findViewById(R.id.tv_result);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleLogin.setOnClickListener(v ->
                googleSignInClient.signOut().addOnCompleteListener(task ->
                        signInLauncher.launch(googleSignInClient.getSignInIntent())
                )
        );

        observeViewModel();
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnGoogleLogin.setEnabled(!isLoading);
        });

        authViewModel.getLoginResult().observe(this, response -> {
            if (response.isNewUser()) {
                // 신규 회원 → 회원가입 화면으로 이동
                tvResult.setText("신규 회원 감지\n회원가입 화면으로 이동합니다...");
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.putExtra(RegisterActivity.EXTRA_TEMP_TOKEN, response.getTempToken());
                intent.putExtra(RegisterActivity.EXTRA_NAME, response.getName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        authViewModel.getRegisterResult().observe(this, response ->
                tvResult.setText("회원가입 완료!\naccessToken: " + response.getAccessToken())
        );

        authViewModel.getErrorMessage().observe(this, message -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            tvResult.setText("오류: " + message);
        });
    }
}
