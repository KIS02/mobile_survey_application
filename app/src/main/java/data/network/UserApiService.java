package data.network;

// [추가] 유저 관련 API 인터페이스 (크레딧 조회 등)
import java.util.List;

import model.ApiResponse;
import model.CreditHistoryResponse;
import model.UserCategoryUpdateRequest;
import model.UserResponse;
import model.UserUpdateRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface UserApiService {

    @GET("api/users/me")
    Call<ApiResponse<UserResponse>> getMe(@Header("Authorization") String bearerToken);

    @GET("api/users/me/credits")
    Call<ApiResponse<List<CreditHistoryResponse>>> getCreditHistory(
            @Header("Authorization") String bearerToken
    );

    @PUT("api/users/me")
    Call<ApiResponse<UserResponse>> updateMe(
            @Header("Authorization") String bearerToken,
            @Body UserUpdateRequest request
    );

    @PUT("api/users/me/categories")
    Call<ApiResponse<UserResponse>> updateCategories(
            @Header("Authorization") String bearerToken,
            @Body UserCategoryUpdateRequest request
    );
}
