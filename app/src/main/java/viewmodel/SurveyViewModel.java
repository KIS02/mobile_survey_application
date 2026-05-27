package viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.repository.SurveyRepository;
import model.AnswerSubmitRequest;
import model.ReliabilityResponse;
import model.SurveyDetailResponse;
import model.SurveyQuestionResponse;

public class SurveyViewModel extends ViewModel {

    private static final String TAG_SURVEY_DEBUG = "SURVEY_DEBUG";

    private final SurveyRepository surveyRepository = new SurveyRepository();

    private final MutableLiveData<SurveyDetailResponse> surveyDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> submitDenied = new MutableLiveData<>(false);
    private final MutableLiveData<String> submitErrorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private Long currentSurveyId;
    private final Map<Long, Long> selectedOptionByContentId = new HashMap<>();

    public LiveData<SurveyDetailResponse> getSurveyDetail() {
        return surveyDetail;
    }

    public LiveData<Boolean> getSubmitSuccess() {
        return submitSuccess;
    }

    public LiveData<Boolean> getSubmitDenied() {
        return submitDenied;
    }

    public LiveData<String> getSubmitErrorMessage() {
        return submitErrorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadRandomSurvey(String accessToken) {
        isLoading.setValue(true);
        surveyRepository.getRandomSurvey(accessToken, new SurveyRepository.SurveyCallback() {
            @Override
            public void onSuccess(SurveyDetailResponse detail) {
                isLoading.postValue(false);
                currentSurveyId = detail.getId();
                selectedOptionByContentId.clear();
                surveyDetail.postValue(detail);
            }

            @Override
            public void onFailure(String errorMessage) {
                isLoading.postValue(false);
                submitErrorMessage.postValue(errorMessage);
            }
        });
    }

    public void selectOption(Long contentId, Long selectedOptionId) {
        selectedOptionByContentId.put(contentId, selectedOptionId);
        Log.d(TAG_SURVEY_DEBUG,
                "selectOption contentId=" + contentId + ", selectedOptionId=" + selectedOptionId);
    }

    public void submitSurvey(String accessToken) {
        SurveyDetailResponse detail = surveyDetail.getValue();
        if (detail == null || currentSurveyId == null) {
            submitErrorMessage.postValue("설문 정보가 없습니다.");
            return;
        }

        List<SurveyQuestionResponse> questions = detail.getQuestions();
        if (questions == null || questions.isEmpty()) {
            submitErrorMessage.postValue("설문 문항이 없습니다.");
            return;
        }

        boolean allAnswered = true;
        for (SurveyQuestionResponse q : questions) {
            if (q == null || q.getId() == null || !selectedOptionByContentId.containsKey(q.getId())) {
                allAnswered = false;
                break;
            }
        }

        if (!allAnswered) {
            submitDenied.setValue(true);
            return;
        }

        List<AnswerSubmitRequest.AnswerItem> answers = new ArrayList<>();
        for (SurveyQuestionResponse q : questions) {
            Long selectedOptionId = selectedOptionByContentId.get(q.getId());
            answers.add(new AnswerSubmitRequest.AnswerItem(q.getId(), selectedOptionId));
        }

        AnswerSubmitRequest request = new AnswerSubmitRequest(answers);
        submitDenied.setValue(false);
        Log.d(TAG_SURVEY_DEBUG,
                "submitSurvey request answersCount=" + answers.size() + ", surveyId=" + currentSurveyId);

        surveyRepository.submitSurvey(accessToken, currentSurveyId, request, new SurveyRepository.SubmitCallback() {
            @Override
            public void onSuccess(ReliabilityResponse response) {
                submitSuccess.postValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                submitErrorMessage.postValue(errorMessage);
            }
        });
    }

    public void doneSubmitSuccess() {
        submitSuccess.setValue(false);
    }

    public void doneSubmitDenied() {
        submitDenied.setValue(false);
    }
}