package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import data.local.TokenManager;
import data.repository.AuthRepository;
import data.repository.CategoryRepository;
import model.CategoryResponse;
import model.GoogleLoginResponse;
import model.RegisterRequest;
import model.TokenResponse;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final TokenManager tokenManager;

    private final MutableLiveData<GoogleLoginResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<TokenResponse> registerResult = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryResponse>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
    }

    public LiveData<GoogleLoginResponse> getLoginResult() { return loginResult; }
    public LiveData<TokenResponse> getRegisterResult() { return registerResult; }
    public LiveData<List<CategoryResponse>> getCategories() { return categories; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void googleLogin(String idToken) {
        isLoading.setValue(true);
        authRepository.googleLogin(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(GoogleLoginResponse response) {
                isLoading.postValue(false);
                if (!response.isNewUser()) {
                    // 기존 회원 → 토큰 저장
                    tokenManager.saveTokens(response.getAccessToken(), response.getRefreshToken());
                }
                loginResult.postValue(response);
            }

            @Override
            public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void register(String tempToken, RegisterRequest request) {
        isLoading.setValue(true);
        authRepository.register(tempToken, request, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess(TokenResponse response) {
                isLoading.postValue(false);
                // 신규 회원 가입 완료 → 토큰 저장
                tokenManager.saveTokens(response.getAccessToken(), response.getRefreshToken());
                registerResult.postValue(response);
            }

            @Override
            public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void loadCategories() {
        categoryRepository.getCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<CategoryResponse> result) {
                categories.postValue(result);
            }

            @Override
            public void onFailure(String message) {
                errorMessage.postValue(message);
            }
        });
    }
}
