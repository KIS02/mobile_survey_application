package viewmodel;

// [추가] 유저 정보 로드 ViewModel - GET /api/user/me

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import data.network.RetrofitClient;
import model.ApiResponse;
import model.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends ViewModel {

    private static final String TAG_REWARD_DEBUG = "REWARD_DEBUG";

    private final MutableLiveData<UserResponse> user = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<UserResponse> getUser() { return user; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadMe(String token) {
        RetrofitClient.getUserApiService().getMe("Bearer " + token)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call,
                                           Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse data = response.body().getData();
                            if (data != null && data.getCredit() != null) {
                                Log.d(TAG_REWARD_DEBUG,
                                        "loadMyCredit success credit=" + data.getCredit());
                            }
                            user.postValue(response.body().getData());
                        } else {
                            errorMessage.postValue("사용자 정보를 불러오지 못했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        errorMessage.postValue("네트워크 오류: " + t.getMessage());
                    }
                });
    }
}
