package data.repository;

import java.util.List;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.RewardResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardRepository {

    public interface RewardCallback {
        void onSuccess(List<RewardResponse> rewards);
        void onFailure(String errorMessage);
    }

    public void getRewards(RewardCallback callback) {
        RetrofitClient.getRewardApiService().getRewards().enqueue(new Callback<ApiResponse<List<RewardResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RewardResponse>>> call,
                                   Response<ApiResponse<List<RewardResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
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
}
