package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import data.local.TokenManager;
import data.repository.AdminRepository;
import model.admin.AdminSurveyResponse;
import model.admin.CreateSurveyRequest;

public class AdminViewModel extends AndroidViewModel {

    private final AdminRepository adminRepository = new AdminRepository();
    private final TokenManager tokenManager;

    private final MutableLiveData<AdminSurveyResponse> createSurveyResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AdminViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
    }

    public LiveData<AdminSurveyResponse> getCreateSurveyResult() { return createSurveyResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void createSurvey(CreateSurveyRequest request) {
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            errorMessage.postValue("로그인이 필요합니다.");
            return;
        }

        isLoading.setValue(true);
        adminRepository.createSurvey(accessToken, request, new AdminRepository.CreateSurveyCallback() {
            @Override
            public void onSuccess(AdminSurveyResponse response) {
                isLoading.postValue(false);
                createSurveyResult.postValue(response);
            }

            @Override
            public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}
