package viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;

public class SurveyViewModel extends ViewModel {

    private final String[] questions = {
            "나는 하루 한 끼를 규칙적으로 먹는다.",
            "나는 아침 식사를 자주 한다.",
            "나는 패스트푸드를 자주 섭취한다.",
            "나는 야식을 자주 먹는다.",
            "나는 채소를 자주 먹는다."
    };

    private final int[] answers = new int[questions.length];

    private final MutableLiveData<String[]> questionList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> submitDenied = new MutableLiveData<>(false);

    public SurveyViewModel() {
        Arrays.fill(answers, -1);
        questionList.setValue(questions);
    }

    public LiveData<String[]> getQuestionList() {
        return questionList;
    }

    public LiveData<Boolean> getSubmitSuccess() {
        return submitSuccess;
    }

    public LiveData<Boolean> getSubmitDenied() {
        return submitDenied;
    }

    public void selectAnswer(int questionIndex, int answerValue) {
        answers[questionIndex] = answerValue;
    }

    public void submitSurvey() {
        if (isQuestionCompleted()) {
            submitSuccess.setValue(true);
        } else {
            submitDenied.setValue(true);
        }
    }

    public void doneSubmitSuccess() {
        submitSuccess.setValue(false);
    }

    public void doneSubmitDenied() {
        submitDenied.setValue(false);
    }

    private boolean isQuestionCompleted() {
        for (int answer : answers) {
            if (answer == -1) {
                return false;
            }
        }
        return true;
    }

    public int[] getAnswers() {
        return answers;
    }
}