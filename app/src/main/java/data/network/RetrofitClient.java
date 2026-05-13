package data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = com.example.mobile_survey_application.BuildConfig.BASE_URL;

    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // [추가] Glide 이미지 로딩 시 BASE_URL 조합에 사용
    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static AuthApiService getAuthApiService() {
        return getInstance().create(AuthApiService.class);
    }

    public static CategoryApiService getCategoryApiService() {
        return getInstance().create(CategoryApiService.class);
    }

    // [추가] 리워드 목록 API
    public static RewardApiService getRewardApiService() {
        return getInstance().create(RewardApiService.class);
    }

    // [추가] 유저 정보 API (크레딧 조회 등)
    public static UserApiService getUserApiService() {
        return getInstance().create(UserApiService.class);
    }
}
