package data.network;

import model.ApiResponse;
import model.GoogleLoginRequest;
import model.GoogleLoginResponse;
import model.RegisterRequest;
import model.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/auth/google")
    Call<ApiResponse<GoogleLoginResponse>> googleLogin(@Body GoogleLoginRequest request);

    @POST("api/auth/register")
    Call<ApiResponse<TokenResponse>> register(@Header("Authorization") String bearerToken,
                                              @Body RegisterRequest request);
}
