package data.repository;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import data.network.RetrofitClient;
import model.AnswerSubmitRequest;
import model.ApiResponse;
import model.ReliabilityResponse;
import model.SurveyDetailResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyRepository {

    private static final String TAG_SURVEY_DEBUG = "SURVEY_DEBUG";

    public interface SurveyCallback {
        void onSuccess(SurveyDetailResponse detail);
        void onFailure(String errorMessage);
    }

    public interface SubmitCallback {
        void onSuccess(ReliabilityResponse response);
        void onFailure(String errorMessage);
    }

    public void getRandomSurvey(String accessToken, SurveyCallback callback) {
        Log.d(TAG_SURVEY_DEBUG, "getRandomSurvey request");
        RetrofitClient.getSurveyApiService()
                .getRandomSurvey("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<SurveyDetailResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SurveyDetailResponse>> call,
                                           Response<ApiResponse<SurveyDetailResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            SurveyDetailResponse detail = response.body().getData();
                            Log.d(TAG_SURVEY_DEBUG,
                                    "getRandomSurvey success surveyId=" + detail.getId()
                                            + ", questionCount=" + (detail.getQuestions() == null ? 0 : detail.getQuestions().size()));
                            if (detail.getQuestions() != null) {
                                for (model.SurveyQuestionResponse q : detail.getQuestions()) {
                                    Log.d(TAG_SURVEY_DEBUG,
                                            "question contentId=" + q.getId()
                                                    + ", options=" + (q.getOptions() == null ? 0 : q.getOptions().size()));
                                    if (q.getOptions() != null) {
                                        for (model.SurveyOptionResponse opt : q.getOptions()) {
                                            Log.d(TAG_SURVEY_DEBUG,
                                                    "option id=" + opt.getId() + ", text=" + opt.getText());
                                        }
                                    }
                                }
                            }
                            callback.onSuccess(detail);
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG_SURVEY_DEBUG,
                                    "getRandomSurvey failed code=" + response.code()
                                            + ", errorBody=" + errorBody);
                            callback.onFailure("설문 조회 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SurveyDetailResponse>> call, Throwable t) {
                        Log.e(TAG_SURVEY_DEBUG, "getRandomSurvey network error=" + t.getMessage(), t);
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void submitSurvey(String accessToken,
                              Long surveyId,
                              AnswerSubmitRequest request,
                              SubmitCallback callback) {
        Log.d(TAG_SURVEY_DEBUG, "submitResponses request surveyId=" + surveyId);
        if (request != null && request.getAnswers() != null) {
            Log.d(TAG_SURVEY_DEBUG, "submitResponses request body answersCount=" + request.getAnswers().size());
        }

        RetrofitClient.getSurveyApiService()
                .submitResponses("Bearer " + accessToken, surveyId, request)
                .enqueue(new Callback<ApiResponse<ReliabilityResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReliabilityResponse>> call,
                                           Response<ApiResponse<ReliabilityResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            Log.d(TAG_SURVEY_DEBUG,
                                    "submitResponses success code=" + response.code());
                            callback.onSuccess(response.body().getData());
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG_SURVEY_DEBUG,
                                    "submitResponses failed code=" + response.code()
                                            + ", errorBody=" + errorBody);
                            callback.onFailure("제출 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReliabilityResponse>> call, Throwable t) {
                        Log.e(TAG_SURVEY_DEBUG, "submitResponses network error=" + t.getMessage(), t);
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

