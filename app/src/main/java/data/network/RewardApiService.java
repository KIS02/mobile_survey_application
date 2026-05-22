package data.network;

// [추가] 리워드 관련 API 인터페이스
import java.util.List;

import model.ApiResponse;
import model.RewardResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RewardApiService {

    @GET("api/rewards")
    Call<ApiResponse<List<RewardResponse>>> getRewards();
}
