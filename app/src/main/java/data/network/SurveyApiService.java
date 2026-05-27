package data.network;

import java.util.List;

import model.AnswerSubmitRequest;
import model.ApiResponse;
import model.ReliabilityResponse;
import model.SurveyDetailResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SurveyApiService {

    @GET("api/surveys/random")
    Call<ApiResponse<SurveyDetailResponse>> getRandomSurvey(
            @Header("Authorization") String bearerToken
    );

    @GET("api/surveys/{surveyId}")
    Call<ApiResponse<SurveyDetailResponse>> getSurvey(
            @Header("Authorization") String bearerToken,
            @Path("surveyId") Long surveyId
    );

    @POST("api/surveys/{surveyId}/responses")
    Call<ApiResponse<ReliabilityResponse>> submitResponses(
            @Header("Authorization") String bearerToken,
            @Path("surveyId") Long surveyId,
            @Body AnswerSubmitRequest request
    );
}

