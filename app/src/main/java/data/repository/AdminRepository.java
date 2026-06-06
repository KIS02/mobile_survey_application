package data.repository;

import android.util.Log;

import java.io.IOException;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.admin.AdminSurveyResponse;
import model.admin.CreateSurveyRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private static final String TAG = "AdminRepository";

    public interface CreateSurveyCallback {
        void onSuccess(AdminSurveyResponse response);
        void onFailure(String message);
    }

    public void createSurvey(String accessToken, CreateSurveyRequest request,
                             CreateSurveyCallback callback) {
        RetrofitClient.getAdminApiService()
                .createSurvey("Bearer " + accessToken, request)
                .enqueue(new Callback<ApiResponse<AdminSurveyResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AdminSurveyResponse>> call,
                                           Response<ApiResponse<AdminSurveyResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            Log.d(TAG, "createSurvey success id=" + response.body().getData().getId());
                            callback.onSuccess(response.body().getData());
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG, "createSurvey failed code=" + response.code()
                                    + " body=" + errorBody);
                            callback.onFailure("설문 등록 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AdminSurveyResponse>> call, Throwable t) {
                        Log.e(TAG, "createSurvey network error=" + t.getMessage(), t);
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
            return "read failed";
        }
        return null;
    }
}
