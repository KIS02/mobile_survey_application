package data.network;

import java.util.List;
import java.util.Map;

import model.ApiResponse;
import model.admin.AdminSurveyListItem;
import model.admin.AdminSurveyResponse;
import model.admin.CreateSurveyRequest;
import model.admin.OptionSetResponse;
import model.admin.PreviewReliabilityRequest;
import model.admin.PreviewReliabilityResponse;
import model.admin.UpdateSurveyRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AdminApiService {

    @POST("api/admin/surveys")
    Call<ApiResponse<AdminSurveyResponse>> createSurvey(
            @Header("Authorization") String bearerToken,
            @Body CreateSurveyRequest request
    );

    @GET("api/admin/surveys")
    Call<ApiResponse<List<AdminSurveyListItem>>> getSurveys(
            @Header("Authorization") String bearerToken
    );

    @GET("api/admin/surveys/{id}")
    Call<ApiResponse<AdminSurveyResponse>> getSurvey(
            @Header("Authorization") String bearerToken,
            @Path("id") long surveyId
    );

    @PATCH("api/admin/surveys/{id}/status")
    Call<ApiResponse<Void>> updateStatus(
            @Header("Authorization") String bearerToken,
            @Path("id") long surveyId,
            @Body Map<String, String> body
    );

    @PATCH("api/admin/surveys/{id}")
    Call<ApiResponse<AdminSurveyResponse>> updateSurvey(
            @Header("Authorization") String bearerToken,
            @Path("id") long surveyId,
            @Body UpdateSurveyRequest request
    );

    @DELETE("api/admin/surveys/{id}")
    Call<ApiResponse<Void>> deleteSurvey(
            @Header("Authorization") String bearerToken,
            @Path("id") long surveyId
    );

    @GET("api/admin/reliability/instruction")
    Call<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> getRandomInstruction(
            @Header("Authorization") String bearerToken
    );

    @GET("api/admin/reliability/bogus")
    Call<ApiResponse<PreviewReliabilityResponse.GeneratedQuestion>> getRandomBogus(
            @Header("Authorization") String bearerToken
    );

    @POST("api/admin/surveys/preview-reliability")
    Call<ApiResponse<PreviewReliabilityResponse>> previewReliability(
            @Header("Authorization") String bearerToken,
            @Body PreviewReliabilityRequest request
    );

    @GET("api/admin/option-sets")
    Call<ApiResponse<List<OptionSetResponse>>> getOptionSets(
            @Header("Authorization") String bearerToken
    );
}
