package viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.local.TokenManager;
import data.repository.AuthRepository;
import data.repository.CategoryRepository;
import data.network.RetrofitClient;
import model.ApiResponse;
import model.CategoryResponse;
import model.GoogleLoginResponse;
import model.PreferenceResponse;
import model.RegisterRequest;
import model.TokenResponse;
import model.UserCategoryUpdateRequest;
import model.UserResponse;
import model.UserUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG_CATEGORY_DEBUG = "CATEGORY_DEBUG";
    private static final String TAG_LOGIN_DEBUG = "LOGIN_DEBUG";
    private static final String TAG_PROFILE_DEBUG = "PROFILE_DEBUG";

    private final AuthRepository authRepository = new AuthRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final TokenManager tokenManager;

    private final MutableLiveData<GoogleLoginResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<TokenResponse> registerResult = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryResponse>> categories = new MutableLiveData<>();
    private final MutableLiveData<UserResponse> myProfile = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<String>> selectedCategories = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Long>> selectedCategoryIds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> categoryUpdateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> categoryUpdateError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileUpdateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> profileUpdateError = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
    }

    public LiveData<GoogleLoginResponse> getLoginResult() { return loginResult; }
    public LiveData<TokenResponse> getRegisterResult() { return registerResult; }
    public LiveData<List<CategoryResponse>> getCategories() { return categories; }
    public LiveData<UserResponse> getMyProfile() { return myProfile; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    private final MutableLiveData<Boolean> navigateToCategoryChange = new MutableLiveData<>();

    public void googleLogin(String idToken) {
        isLoading.setValue(true);
        authRepository.googleLogin(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(GoogleLoginResponse response) {
                isLoading.postValue(false);
                boolean accessTokenExists = response.getAccessToken() != null
                        && !response.getAccessToken().isEmpty();
                Log.d(TAG_LOGIN_DEBUG,
                        "login success, newUser=" + response.isNewUser()
                                + ", token exists=" + accessTokenExists);
                if (!response.isNewUser()) {
                    // 기존 회원 → 토큰 저장
                    tokenManager.saveTokens(response.getAccessToken(), response.getRefreshToken());
                    Log.d(TAG_LOGIN_DEBUG, "saved token exists=" + tokenManager.hasToken());
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
                Log.d(TAG_LOGIN_DEBUG, "register success, saved token exists=" + tokenManager.hasToken());
                registerResult.postValue(response);
            }

            @Override
            public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }


    public LiveData<List<String>> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(List<String> categories) {
        selectedCategories.setValue(categories);
    }
    public void loadCategories() {
        categoryRepository.getCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<CategoryResponse> result) {
                Log.d(TAG_CATEGORY_DEBUG, "loadCategories success, count=" + result.size());
                for (CategoryResponse category : result) {
                    Log.d(TAG_CATEGORY_DEBUG,
                            "category id=" + category.getId() + ", name=" + category.getName());
                }
                categories.postValue(result);
            }

            @Override
            public void onFailure(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public LiveData<Boolean> getNavigateToCategoryChange() {
        return navigateToCategoryChange;
    }

    public void onCategoryClick() {
        navigateToCategoryChange.setValue(true);
    }

    public void doneNavigateToCategoryChange() {
        navigateToCategoryChange.setValue(false);
    }

    public LiveData<List<Long>> getSelectedCategoryIds() {
        return selectedCategoryIds;
    }

    public void setSelectedCategoryIds(List<Long> ids) {
        selectedCategoryIds.setValue(ids);
    }

    public LiveData<Boolean> getCategoryUpdateSuccess() {
        return categoryUpdateSuccess;
    }

    public LiveData<String> getCategoryUpdateError() {
        return categoryUpdateError;
    }

    public void resetCategoryUpdateResult() {
        categoryUpdateSuccess.setValue(false);
        categoryUpdateError.setValue(null);
    }

    public LiveData<Boolean> getProfileUpdateSuccess() {
        return profileUpdateSuccess;
    }

    public LiveData<String> getProfileUpdateError() {
        return profileUpdateError;
    }

    public void resetProfileUpdateResult() {
        profileUpdateSuccess.setValue(false);
        profileUpdateError.setValue(null);
    }

    public void toggleCategory(long categoryId, boolean isChecked) {
        List<Long> current = selectedCategoryIds.getValue();

        if (current == null) {
            current = new ArrayList<>();
        } else {
            current = new ArrayList<>(current);
        }

        if (isChecked) {
            if (!current.contains(categoryId)) {
                current.add(categoryId);
            }
        } else {
            current.remove(categoryId);
        }

        selectedCategoryIds.setValue(current);
    }

    public void loadMyProfile() {
        String accessToken = tokenManager.getAccessToken();
        Log.d(TAG_PROFILE_DEBUG, "profile auth token exists=" + (accessToken != null && !accessToken.isEmpty()));

        if (accessToken == null || accessToken.isEmpty()) {
            errorMessage.postValue("로그인이 필요합니다.");
            return;
        }

        Log.d(TAG_PROFILE_DEBUG, "loadMyProfile request");
        RetrofitClient.getUserApiService()
                .getMe("Bearer " + accessToken)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call,
                                           Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            UserResponse user = response.body().getData();
                            Log.d(TAG_PROFILE_DEBUG,
                                    "loadMyProfile success");
                            Log.d(TAG_PROFILE_DEBUG,
                                    "profile data name=" + safeLogValue(user.getName())
                                            + ", email exists=" + hasValue(user.getEmail()));
                            Log.d(TAG_PROFILE_DEBUG,
                                    "loadMyProfile success birthDate exists="
                                            + hasValue(user.getBirthDate()));
                            Log.d(TAG_PROFILE_DEBUG,
                                    "loadMyProfile birthDate=" + safeLogValue(user.getBirthDate()));
                            myProfile.postValue(user);
                            List<Long> ids = new ArrayList<>();

                            if (user.getPreferences() != null) {
                                for (PreferenceResponse preference : user.getPreferences()) {
                                    ids.add(preference.getCategoryId());
                                }
                            }

                            selectedCategoryIds.postValue(ids);
                        } else {
                            Log.e(TAG_PROFILE_DEBUG,
                                    "loadMyProfile failed code=" + response.code()
                                            + ", errorBody=" + readErrorBody(response));
                            errorMessage.postValue("프로필 조회 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        Log.e(TAG_PROFILE_DEBUG, "loadMyProfile network error=" + t.getMessage(), t);
                        errorMessage.postValue("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void updateMyProfile(UserUpdateRequest request) {
        String accessToken = tokenManager.getAccessToken();
        Log.d(TAG_PROFILE_DEBUG, "profile auth token exists=" + (accessToken != null && !accessToken.isEmpty()));

        if (accessToken == null || accessToken.isEmpty()) {
            profileUpdateError.postValue("로그인이 필요합니다.");
            return;
        }

        Log.d(TAG_PROFILE_DEBUG, "updateMyProfile request");
        Log.d(TAG_PROFILE_DEBUG,
                "updateMyProfile request birthDate=" + safeLogValue(request.getBirthDate()));
        RetrofitClient.getUserApiService()
                .updateMe("Bearer " + accessToken, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call,
                                           Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            Log.d(TAG_PROFILE_DEBUG,
                                    "updateMyProfile success code=" + response.code());
                            UserResponse user = response.body().getData();
                            Log.d(TAG_PROFILE_DEBUG,
                                    "updateMyProfile response birthDate exists="
                                            + hasValue(user.getBirthDate()));
                            Log.d(TAG_PROFILE_DEBUG,
                                    "updateMyProfile response birthDate="
                                            + safeLogValue(user.getBirthDate()));
                            myProfile.postValue(user);
                            profileUpdateSuccess.postValue(true);
                        } else {
                            Log.e(TAG_PROFILE_DEBUG,
                                    "updateMyProfile failed code=" + response.code()
                                            + ", errorBody=" + readErrorBody(response));
                            profileUpdateError.postValue("프로필 저장 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        Log.e(TAG_PROFILE_DEBUG, "updateMyProfile network error=" + t.getMessage(), t);
                        profileUpdateError.postValue("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    public void updateMyCategories(List<Long> categoryIds) {
        String accessToken = tokenManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            categoryUpdateError.postValue("로그인이 필요합니다.");
            return;
        }
        Log.d(TAG_LOGIN_DEBUG, "auth header will be attached=true");

        UserCategoryUpdateRequest request = new UserCategoryUpdateRequest(categoryIds);
        Log.d(TAG_CATEGORY_DEBUG, "updateMyCategories request categoryIds=" + categoryIds);

        RetrofitClient.getUserApiService()
                .updateCategories("Bearer " + accessToken, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call,
                                           Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG_CATEGORY_DEBUG,
                                    "updateMyCategories success code=" + response.code());
                            setSelectedCategoryIds(categoryIds);
                            categoryUpdateSuccess.postValue(true);
                        } else {
                            String errorBody = readErrorBody(response);

                            Log.e(TAG_CATEGORY_DEBUG,
                                    "updateMyCategories failed code=" + response.code()
                                            + ", errorBody=" + errorBody);
                            categoryUpdateError.postValue("카테고리 저장 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        Log.e(TAG_CATEGORY_DEBUG,
                                "updateMyCategories network error=" + t.getMessage(), t);
                        categoryUpdateError.postValue("네트워크 오류: " + t.getMessage());
                    }
                });
    }

    private String readErrorBody(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (IOException e) {
            return "read failed: " + e.getMessage();
        }

        return null;
    }

    private boolean hasValue(String value) {
        return value != null && !value.isEmpty();
    }

    private String safeLogValue(String value) {
        return hasValue(value) ? value : "(empty)";
    }
}
