package viewmodel;

// [추가] 리워드 목록 로드 ViewModel - GET /api/rewards

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import data.repository.RewardRepository;
import model.RewardResponse;

public class RewardViewModel extends ViewModel {

    private final RewardRepository rewardRepository = new RewardRepository();

    private final MutableLiveData<List<RewardResponse>> rewards = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<List<RewardResponse>> getRewards() { return rewards; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadRewards() {
        isLoading.setValue(true);
        rewardRepository.getRewards(new RewardRepository.RewardCallback() {
            @Override
            public void onSuccess(List<RewardResponse> result) {
                isLoading.postValue(false);
                rewards.postValue(result);
            }

            @Override
            public void onFailure(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}
