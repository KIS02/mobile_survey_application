package data.repository;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;

import data.network.RetrofitClient;
import model.AnswerSubmitRequest;
import model.ApiResponse;
import model.ReliabilityResponse;
import model.SurveyDetailResponse;
import model.SurveyResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyRepository {

    private static final String TAG_SURVEY_DEBUG = "SURVEY_DEBUG";
    private static final String TAG_SURVEY_LIST_DEBUG = "SURVEY_LIST_DEBUG";
    private static final Gson GSON = new Gson();

    public interface SurveyListCallback {
        void onSuccess(List<SurveyResponse> surveys);
        void onFailure(String errorMessage);
    }

    public interface SurveyCallback {
        void onSuccess(SurveyDetailResponse detail);
        void onFailure(String errorMessage);
    }

    public interface SubmitCallback {
        void onSuccess(ReliabilityResponse response);
        void onFailure(String errorMessage);
    }

    public void getSurveys(String accessToken, SurveyListCallback callback) {
        Log.d(TAG_SURVEY_LIST_DEBUG, "getSurveys request");
        RetrofitClient.getSurveyApiService()
                .getSurveys("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<List<SurveyResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<SurveyResponse>>> call,
                                           Response<ApiResponse<List<SurveyResponse>>> response) {
                        Log.d(TAG_SURVEY_LIST_DEBUG, "getSurveys response code=" + response.code());

                        ApiResponse<List<SurveyResponse>> body = response.body();
                        if (body != null) {
                            List<SurveyResponse> data = body.getData();
                            int dataSize = data == null ? -1 : data.size();
                            Log.d(TAG_SURVEY_LIST_DEBUG,
                                    "response.body success=" + body.isSuccess()
                                            + ", message=" + body.getMessage()
                                            + ", dataSize=" + dataSize);
                            logParsedBodyJson(body);

                            if (data == null) {
                                Log.w(TAG_SURVEY_LIST_DEBUG,
                                        "response.body data is null - ApiResponse는 data 필드를 사용합니다. "
                                                + "서버가 result 필드만 내려주면 Gson 매핑이 되지 않습니다.");
                            } else if (data.isEmpty()) {
                                Log.w(TAG_SURVEY_LIST_DEBUG, "server returned empty survey list");
                            }
                        } else {
                            Log.w(TAG_SURVEY_LIST_DEBUG, "response.body() is null");
                        }

                        if (response.isSuccessful()
                                && body != null
                                && body.getData() != null) {
                            List<SurveyResponse> surveys = body.getData();
                            Log.d(TAG_SURVEY_LIST_DEBUG,
                                    "getSurveys success count=" + surveys.size());
                            for (SurveyResponse survey : surveys) {
                                if (survey == null) {
                                    continue;
                                }
                                Log.d(TAG_SURVEY_LIST_DEBUG,
                                        "survey id=" + survey.getId()
                                                + ", title=" + survey.getTitle()
                                                + ", categoryName=" + survey.getCategoryName()
                                                + ", reward=" + survey.getReward());
                            }
                            callback.onSuccess(surveys);
                        } else {
                            if (!response.isSuccessful() || body == null) {
                                String errorBody = readErrorBody(response);
                                Log.e(TAG_SURVEY_LIST_DEBUG,
                                        "getSurveys failed errorBody=" + errorBody);
                            }
                            String failureMessage = body != null && body.getMessage() != null
                                    ? body.getMessage()
                                    : "설문 목록 조회 실패: " + response.code();
                            Log.e(TAG_SURVEY_LIST_DEBUG, "getSurveys failed: " + failureMessage);
                            callback.onFailure(failureMessage);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<SurveyResponse>>> call, Throwable t) {
                        Log.e(TAG_SURVEY_LIST_DEBUG,
                                "getSurveys network error=" + t.getMessage(), t);
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void getSurveyById(String accessToken, Long surveyId, SurveyCallback callback) {
        Log.d(TAG_SURVEY_LIST_DEBUG, "getSurveyById request surveyId=" + surveyId);
        RetrofitClient.getSurveyApiService()
                .getSurvey("Bearer " + accessToken, surveyId)
                .enqueue(new Callback<ApiResponse<SurveyDetailResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SurveyDetailResponse>> call,
                                           Response<ApiResponse<SurveyDetailResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            SurveyDetailResponse detail = response.body().getData();
                            Log.d(TAG_SURVEY_LIST_DEBUG,
                                    "getSurveyById success surveyId=" + detail.getId()
                                            + ", questionCount="
                                            + (detail.getQuestions() == null ? 0 : detail.getQuestions().size()));
                            callback.onSuccess(detail);
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG_SURVEY_LIST_DEBUG,
                                    "getSurveyById failed code=" + response.code()
                                            + ", errorBody=" + errorBody
                                            + ", message=" + (response.body() != null
                                            ? response.body().getMessage() : null));
                            callback.onFailure("설문 조회 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SurveyDetailResponse>> call, Throwable t) {
                        Log.e(TAG_SURVEY_LIST_DEBUG,
                                "getSurveyById network error=" + t.getMessage(), t);
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
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
                            ReliabilityResponse submitResponse = response.body().getData();
                            Log.d(TAG_SURVEY_DEBUG,
                                    "submitResponses success code=" + response.code()
                                            + ", earnedCredit=" + submitResponse.getEarnedCredit());
                            callback.onSuccess(submitResponse);
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

    private void logParsedBodyJson(ApiResponse<List<SurveyResponse>> body) {
        try {
            Log.d(TAG_SURVEY_LIST_DEBUG, "response.body json=" + GSON.toJson(body));
        } catch (Exception e) {
            Log.d(TAG_SURVEY_LIST_DEBUG,
                    "response.body json serialize failed: " + e.getMessage());
        }
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

