package data.repository;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.admin.AdminSurveyListItem;
import model.admin.AdminSurveyResponse;
import model.admin.CreateSurveyRequest;
import model.admin.OptionSetResponse;
import model.admin.PreviewReliabilityRequest;
import model.admin.PreviewReliabilityResponse;
import model.admin.UpdateSurveyRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private static final String TAG = "AdminRepository";

    public interface CreateSurveyCallback {
        void onSuccess(AdminSurveyResponse response);
        void onFailure(String message);
    }

    public interface OptionSetCallback {
        void onSuccess(List<OptionSetResponse> optionSets);
        void onFailure(String message);
    }

    public interface PreviewReliabilityCallback {
        void onSuccess(PreviewReliabilityResponse response);
        void onFailure(String message);
    }

    public interface SurveyListCallback {
        void onSuccess(List<AdminSurveyListItem> surveys);
        void onFailure(String message);
    }

    public interface SurveyDetailCallback {
        void onSuccess(AdminSurveyResponse survey);
        void onFailure(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface UpdateSurveyCallback {
        void onSuccess(AdminSurveyResponse survey);
        void onFailure(String message);
    }

    public interface SingleQuestionCallback {
        void onSuccess(PreviewReliabilityResponse.GeneratedQuestion question);
        void onFailure(String message);
    }

    public void loadOptionSets(String accessToken, OptionSetCallback callback) {
        RetrofitClient.getAdminApiService()
                .getOptionSets("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<List<OptionSetResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<OptionSetResponse>>> call,
                                           Response<ApiResponse<List<OptionSetResponse>>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onFailure("옵션 세트 로딩 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<OptionSetResponse>>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void previewReliability(String accessToken, PreviewReliabilityRequest request,
                                   PreviewReliabilityCallback callback) {
        RetrofitClient.getAdminApiService()
                .previewReliability("Bearer " + accessToken, request)
                .enqueue(new Callback<ApiResponse<PreviewReliabilityResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreviewReliabilityResponse>> call,
                                           Response<ApiResponse<PreviewReliabilityResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG, "previewReliability failed code=" + response.code()
                                    + " body=" + errorBody);
                            callback.onFailure("AI 문항 생성 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PreviewReliabilityResponse>> call, Throwable t) {
                        Log.e(TAG, "previewReliability network error=" + t.getMessage(), t);
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
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

    public void getRandomInstruction(String accessToken, SingleQuestionCallback callback) {
        RetrofitClient.getAdminApiService()
                .getRandomInstruction("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> call,
                                           Response<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onFailure("지시 문항 조회 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void getRandomBogus(String accessToken, SingleQuestionCallback callback) {
        RetrofitClient.getAdminApiService()
                .getRandomBogus("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> call,
                                           Response<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onFailure("거짓 문항 조회 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void getSurveys(String accessToken, SurveyListCallback callback) {
        RetrofitClient.getAdminApiService()
                .getSurveys("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<List<AdminSurveyListItem>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AdminSurveyListItem>>> call,
                                           Response<ApiResponse<List<AdminSurveyListItem>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onFailure("목록 조회 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<AdminSurveyListItem>>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void getSurvey(String accessToken, long surveyId, SurveyDetailCallback callback) {
        RetrofitClient.getAdminApiService()
                .getSurvey("Bearer " + accessToken, surveyId)
                .enqueue(new Callback<ApiResponse<AdminSurveyResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AdminSurveyResponse>> call,
                                           Response<ApiResponse<AdminSurveyResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onFailure("상세 조회 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<AdminSurveyResponse>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void updateStatus(String accessToken, long surveyId, String status, SimpleCallback callback) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        RetrofitClient.getAdminApiService()
                .updateStatus("Bearer " + accessToken, surveyId, body)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("상태 변경 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void updateSurvey(String accessToken, long surveyId, UpdateSurveyRequest request,
                             UpdateSurveyCallback callback) {
        RetrofitClient.getAdminApiService()
                .updateSurvey("Bearer " + accessToken, surveyId, request)
                .enqueue(new Callback<ApiResponse<AdminSurveyResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AdminSurveyResponse>> call,
                                           Response<ApiResponse<AdminSurveyResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onFailure("수정 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<AdminSurveyResponse>> call, Throwable t) {
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void deleteSurvey(String accessToken, long surveyId, SimpleCallback callback) {
        RetrofitClient.getAdminApiService()
                .deleteSurvey("Bearer " + accessToken, surveyId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("삭제 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
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
