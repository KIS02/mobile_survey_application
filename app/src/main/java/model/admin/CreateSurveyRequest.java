package model.admin;

import java.util.List;

public class CreateSurveyRequest {
    private String title;
    private String description;
    private Long categoryId;
    private int reward;
    private Integer targetCount;
    private String startAt;
    private String endAt;
    private String targetGender;
    private Short targetAgeMin;
    private Short targetAgeMax;
    private List<CreateQuestionRequest> questions;

    public CreateSurveyRequest(String title, String description, Long categoryId,
                               int reward, Integer targetCount,
                               String startAt, String endAt,
                               String targetGender, Short targetAgeMin, Short targetAgeMax,
                               List<CreateQuestionRequest> questions) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.reward = reward;
        this.targetCount = targetCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetGender = targetGender;
        this.targetAgeMin = targetAgeMin;
        this.targetAgeMax = targetAgeMax;
        this.questions = questions;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getCategoryId() { return categoryId; }
    public int getReward() { return reward; }
    public Integer getTargetCount() { return targetCount; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public String getTargetGender() { return targetGender; }
    public Short getTargetAgeMin() { return targetAgeMin; }
    public Short getTargetAgeMax() { return targetAgeMax; }
    public List<CreateQuestionRequest> getQuestions() { return questions; }
}
