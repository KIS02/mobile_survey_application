package data.network;

import model.ApiResponse;
import model.CategoryResponse;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface CategoryApiService {

    @GET("api/categories")
    Call<ApiResponse<List<CategoryResponse>>> getCategories();
}
