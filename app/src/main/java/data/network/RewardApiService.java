package data.network;

// [추가] 리워드 관련 API 인터페이스
import java.util.List;

import model.ApiResponse;
import model.CouponResponse;
import model.ExchangeRequest;
import model.RewardResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RewardApiService {

    @GET("api/rewards")
    Call<ApiResponse<List<RewardResponse>>> getRewards();

    @POST("api/rewards/exchange")
    Call<ApiResponse<CouponResponse>> exchangeReward(
            @Header("Authorization") String bearerToken,
            @Body ExchangeRequest request
    );
}
