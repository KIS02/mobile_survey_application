package data.repository;

import java.util.List;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.UserCategoryUpdateRequest;
import model.CouponResponse;
import model.CreditHistoryResponse;
import model.UserResponse;
import model.UserUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    public interface UserCallback {
        void onSuccess(UserResponse user);
        void onFailure(String errorMessage);
    }

    public interface CouponCallback {
        void onSuccess(List<CouponResponse> coupons);
        void onFailure(String errorMessage);
    }

    public interface CreditHistoryCallback {
        void onSuccess(List<CreditHistoryResponse> history);
        void onFailure(String errorMessage);
    }

    public void getMe(String accessToken, UserCallback callback) {
        RetrofitClient.getUserApiService().getMe("Bearer " + accessToken).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call,
                                   Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void updateMe(String accessToken, UserUpdateRequest request, UserCallback callback) {
        RetrofitClient.getUserApiService().updateMe("Bearer " + accessToken, request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call,
                                   Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void updateCategories(String accessToken, UserCategoryUpdateRequest request, UserCallback callback) {
        RetrofitClient.getUserApiService().updateCategories("Bearer " + accessToken, request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call,
                                   Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void getCoupons(String accessToken, CouponCallback callback) {
        RetrofitClient.getUserApiService().getCoupons("Bearer " + accessToken).enqueue(new Callback<ApiResponse<List<CouponResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CouponResponse>>> call,
                                   Response<ApiResponse<List<CouponResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CouponResponse>>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void getCreditHistory(String accessToken, CreditHistoryCallback callback) {
        RetrofitClient.getUserApiService().getCreditHistory("Bearer " + accessToken).enqueue(new Callback<ApiResponse<List<CreditHistoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CreditHistoryResponse>>> call,
                                   Response<ApiResponse<List<CreditHistoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CreditHistoryResponse>>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }
}
