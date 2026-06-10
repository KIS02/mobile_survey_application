package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import data.local.TokenManager;
import data.repository.AdminRepository;
import model.admin.AdminSurveyListItem;
import model.admin.AdminSurveyResponse;
import model.admin.CreateSurveyRequest;
import model.admin.OptionSetResponse;
import model.admin.PreviewReliabilityRequest;
import model.admin.PreviewReliabilityResponse;
import model.admin.UpdateSurveyRequest;

public class AdminViewModel extends AndroidViewModel {

    private final AdminRepository adminRepository = new AdminRepository();
    private final TokenManager tokenManager;

    private final MutableLiveData<AdminSurveyResponse> createSurveyResult = new MutableLiveData<>();
    private final MutableLiveData<List<OptionSetResponse>> optionSets = new MutableLiveData<>();
    private final MutableLiveData<PreviewReliabilityResponse> previewResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<AdminSurveyListItem>> surveyList = new MutableLiveData<>();
    private final MutableLiveData<AdminSurveyResponse> surveyDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
    private final MutableLiveData<PreviewReliabilityResponse.GeneratedQuestion> singleQuestion = new MutableLiveData<>();

    public AdminViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
    }

    public LiveData<AdminSurveyResponse> getCreateSurveyResult() { return createSurveyResult; }
    public LiveData<List<OptionSetResponse>> getOptionSets() { return optionSets; }
    public LiveData<PreviewReliabilityResponse> getPreviewResult() { return previewResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<List<AdminSurveyListItem>> getSurveyList() { return surveyList; }
    public LiveData<AdminSurveyResponse> getSurveyDetail() { return surveyDetail; }
    public LiveData<Boolean> getActionSuccess() { return actionSuccess; }
    public LiveData<PreviewReliabilityResponse.GeneratedQuestion> getSingleQuestion() { return singleQuestion; }

    public void clearPreviewResult() { previewResult.setValue(null); }
    public void clearSingleQuestion() { singleQuestion.setValue(null); }
    public void clearActionSuccess() { actionSuccess.setValue(null); }
    public void clearErrorMessage() { errorMessage.setValue(null); }
    public void clearCreateSurveyResult() { createSurveyResult.setValue(null); }

    public void loadOptionSets() {
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) return;

        adminRepository.loadOptionSets(accessToken, new AdminRepository.OptionSetCallback() {
            @Override
            public void onSuccess(List<OptionSetResponse> result) {
                optionSets.postValue(result);
            }

            @Override
            public void onFailure(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void previewReliability(PreviewReliabilityRequest request) {
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            errorMessage.postValue("로그인이 필요합니다.");
            return;
        }

        isLoading.setValue(true);
        adminRepository.previewReliability(accessToken, request, new AdminRepository.PreviewReliabilityCallback() {
            @Override
            public void onSuccess(PreviewReliabilityResponse response) {
                isLoading.postValue(false);
                previewResult.postValue(response);
            }

            @Override
            public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void addRandomInstruction() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        adminRepository.getRandomInstruction(token, new AdminRepository.SingleQuestionCallback() {
            @Override public void onSuccess(PreviewReliabilityResponse.GeneratedQuestion question) {
                singleQuestion.postValue(question);
            }
            @Override public void onFailure(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void addRandomBogus() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        adminRepository.getRandomBogus(token, new AdminRepository.SingleQuestionCallback() {
            @Override public void onSuccess(PreviewReliabilityResponse.GeneratedQuestion question) {
                singleQuestion.postValue(question);
            }
            @Override public void onFailure(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void loadSurveys() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        isLoading.setValue(true);
        adminRepository.getSurveys(token, new AdminRepository.SurveyListCallback() {
            @Override public void onSuccess(List<AdminSurveyListItem> surveys) {
                isLoading.postValue(false);
                surveyList.postValue(surveys);
            }
            @Override public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void loadSurvey(long surveyId) {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        isLoading.setValue(true);
        adminRepository.getSurvey(token, surveyId, new AdminRepository.SurveyDetailCallback() {
            @Override public void onSuccess(AdminSurveyResponse survey) {
                isLoading.postValue(false);
                surveyDetail.postValue(survey);
            }
            @Override public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void updateStatus(long surveyId, String status) {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        isLoading.setValue(true);
        adminRepository.updateStatus(token, surveyId, status, new AdminRepository.SimpleCallback() {
            @Override public void onSuccess() {
                isLoading.postValue(false);
                actionSuccess.postValue(true);
            }
            @Override public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void updateSurvey(long surveyId, UpdateSurveyRequest request) {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        isLoading.setValue(true);
        adminRepository.updateSurvey(token, surveyId, request, new AdminRepository.UpdateSurveyCallback() {
            @Override public void onSuccess(AdminSurveyResponse survey) {
                isLoading.postValue(false);
                actionSuccess.postValue(true);
            }
            @Override public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void deleteSurvey(long surveyId) {
        String token = tokenManager.getAccessToken();
        if (token == null) return;
        isLoading.setValue(true);
        adminRepository.deleteSurvey(token, surveyId, new AdminRepository.SimpleCallback() {
            @Override public void onSuccess() {
                isLoading.postValue(false);
                actionSuccess.postValue(true);
            }
            @Override public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

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
