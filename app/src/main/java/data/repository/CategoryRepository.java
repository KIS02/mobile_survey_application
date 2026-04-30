package data.repository;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.CategoryResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class CategoryRepository {

    public interface CategoryCallback {
        void onSuccess(List<CategoryResponse> categories);
        void onFailure(String errorMessage);
    }

    public void getCategories(CategoryCallback callback) {
        RetrofitClient.getCategoryApiService().getCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CategoryResponse>>> call,
                                   Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CategoryResponse>>> call, Throwable t) {
                callback.onFailure("네트워크 오류: " + t.getMessage());
            }
        });
    }
}
