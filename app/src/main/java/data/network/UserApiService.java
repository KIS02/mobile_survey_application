package data.network;

import java.util.List;

import model.ApiResponse;
import model.CouponResponse;
import model.CreditHistoryResponse;
import model.UserCategoryUpdateRequest;
import model.UserResponse;
import model.UserUpdateRequest;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface UserApiService {

    @GET("api/users/me")
    Call<ApiResponse<UserResponse>> getMe(@Header("Authorization") String bearerToken);

    @PUT("api/users/me")
    Call<ApiResponse<UserResponse>> updateMe(@Header("Authorization") String bearerToken,
                                             @Body UserUpdateRequest request);

    @Multipart
    @POST("api/users/me/image")
    Call<ApiResponse<String>> uploadImage(@Header("Authorization") String bearerToken,
                                          @Part MultipartBody.Part file);

    @PUT("api/users/me/categories")
    Call<ApiResponse<UserResponse>> updateCategories(@Header("Authorization") String bearerToken,
                                                     @Body UserCategoryUpdateRequest request);

    @GET("api/users/me/coupons")
    Call<ApiResponse<List<CouponResponse>>> getCoupons(@Header("Authorization") String bearerToken);

    @GET("api/users/me/credits")
    Call<ApiResponse<List<CreditHistoryResponse>>> getCreditHistory(@Header("Authorization") String bearerToken);
}
