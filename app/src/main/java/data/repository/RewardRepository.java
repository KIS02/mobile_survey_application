package data.repository;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.CouponResponse;
import model.CreditHistoryResponse;
import model.ExchangeRequest;
import model.RewardResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardRepository {

    private static final String TAG_REWARD_DEBUG = "REWARD_DEBUG";

    public interface RewardCallback {
        void onSuccess(List<RewardResponse> rewards);
        void onFailure(String errorMessage);
    }

    public interface ExchangeCallback {
        void onSuccess(CouponResponse coupon);
        void onFailure(String errorMessage);
    }

    public interface CreditHistoryCallback {
        void onSuccess(List<CreditHistoryResponse> histories);
        void onFailure(String errorMessage);
    }

    public void getRewards(RewardCallback callback) {
        RetrofitClient.getRewardApiService().getRewards().enqueue(new Callback<ApiResponse<List<RewardResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RewardResponse>>> call,
                                   Response<ApiResponse<List<RewardResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Log.d(TAG_REWARD_DEBUG,
                            "loadRewards success count=" + response.body().getData().size());
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<RewardResponse>>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void exchangeReward(String accessToken, Long rewardId, ExchangeCallback callback) {
        Log.d(TAG_REWARD_DEBUG, "exchange request rewardId=" + rewardId);
        ExchangeRequest request = new ExchangeRequest(rewardId);

        RetrofitClient.getRewardApiService()
                .exchangeReward("Bearer " + accessToken, request)
                .enqueue(new Callback<ApiResponse<CouponResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CouponResponse>> call,
                                           Response<ApiResponse<CouponResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            CouponResponse coupon = response.body().getData();
                            Log.d(TAG_REWARD_DEBUG,
                                    "exchange success rewardName=" + coupon.getRewardName());
                            callback.onSuccess(coupon);
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG_REWARD_DEBUG,
                                    "exchange failed code=" + response.code()
                                            + ", errorBody=" + errorBody);
                            callback.onFailure(resolveExchangeErrorMessage(errorBody, response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CouponResponse>> call, Throwable t) {
                        Log.e(TAG_REWARD_DEBUG, "exchange network error=" + t.getMessage(), t);
                        callback.onFailure("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void getCreditHistory(String accessToken, CreditHistoryCallback callback) {
        Log.d(TAG_REWARD_DEBUG, "loadCreditHistory request");
        RetrofitClient.getUserApiService()
                .getCreditHistory("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<List<CreditHistoryResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<CreditHistoryResponse>>> call,
                                           Response<ApiResponse<List<CreditHistoryResponse>>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            List<CreditHistoryResponse> histories = response.body().getData();
                            Log.d(TAG_REWARD_DEBUG,
                                    "loadCreditHistory success count=" + histories.size());
                            callback.onSuccess(histories);
                        } else {
                            String errorBody = readErrorBody(response);
                            Log.e(TAG_REWARD_DEBUG,
                                    "loadCreditHistory failed code=" + response.code()
                                            + ", errorBody=" + errorBody);
                            callback.onFailure("포인트 내역 조회 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CreditHistoryResponse>>> call, Throwable t) {
                        Log.e(TAG_REWARD_DEBUG, "loadCreditHistory network error=" + t.getMessage(), t);
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

    private String resolveExchangeErrorMessage(String errorBody, int code) {
        if (errorBody != null && errorBody.contains("포인트가 부족")) {
            return "포인트가 부족합니다.";
        }

        return "교환 실패: " + code;
    }
}
