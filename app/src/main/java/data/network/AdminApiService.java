package data.network;

import model.ApiResponse;
import model.admin.AdminSurveyResponse;
import model.admin.CreateSurveyRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AdminApiService {

    @POST("api/admin/surveys")
    Call<ApiResponse<AdminSurveyResponse>> createSurvey(
            @Header("Authorization") String bearerToken,
            @Body CreateSurveyRequest request
    );
}
