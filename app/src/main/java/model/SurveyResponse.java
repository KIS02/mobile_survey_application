package model;

public class SurveyResponse {

    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer reward;
    private Integer questionCount;
    private String targetGender;
    private Integer targetAgeMin;
    private Integer targetAgeMax;
    private String startAt;
    private String endAt;
    private Integer matchScore;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Integer getReward() {
        return reward;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public String getTargetGender() {
        return targetGender;
    }

    public Integer getTargetAgeMin() {
        return targetAgeMin;
    }

    public Integer getTargetAgeMax() {
        return targetAgeMax;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public Integer getMatchScore() {
        return matchScore;
    }
}
