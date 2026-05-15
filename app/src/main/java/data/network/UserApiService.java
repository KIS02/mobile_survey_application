package data.network;

// [추가] 유저 관련 API 인터페이스 (크레딧 조회 등)
import model.ApiResponse;
import model.UserResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserApiService {

    @GET("api/user/me")
    Call<ApiResponse<UserResponse>> getMe(@Header("Authorization") String bearerToken);
}
