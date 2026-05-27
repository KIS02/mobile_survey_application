package data.repository;

import android.util.Log;

import java.io.IOException;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.GoogleLoginRequest;
import model.GoogleLoginResponse;
import model.RegisterRequest;
import model.TokenResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String TAG_REGISTER_DEBUG = "REGISTER_DEBUG";

    public interface AuthCallback {
        void onSuccess(GoogleLoginResponse response);
        void onFailure(String errorMessage);
    }

    public interface RegisterCallback {
        void onSuccess(TokenResponse response);
        void onFailure(String errorMessage);
    }

    public void googleLogin(String idToken, AuthCallback callback) {
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);

        RetrofitClient.getAuthApiService().googleLogin(request).enqueue(new Callback<ApiResponse<GoogleLoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<GoogleLoginResponse>> call,
                                   Response<ApiResponse<GoogleLoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GoogleLoginResponse>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void register(String tempToken, RegisterRequest request, RegisterCallback callback) {
        Log.d(TAG_REGISTER_DEBUG, "register request tempToken exists="
                + (tempToken != null && !tempToken.isEmpty()));
        RetrofitClient.getAuthApiService().register("Bearer " + tempToken, request).enqueue(new Callback<ApiResponse<TokenResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TokenResponse>> call,
                                   Response<ApiResponse<TokenResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    Log.e(TAG_REGISTER_DEBUG,
                            "register failed code=" + response.code()
                                    + ", errorBody=" + readErrorBody(response));
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TokenResponse>> call, Throwable t) {
                Log.e(TAG_REGISTER_DEBUG, "register network error=" + t.getMessage(), t);
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    private String readErrorBody(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (IOException e) {
            return "read failed: " + e.getMessage();
        }

        return null;
    }
}
