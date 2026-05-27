package model;

import java.util.List;

public class SurveyDetailResponse {

    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer reward;
    private List<SurveyQuestionResponse> questions;

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

    public List<SurveyQuestionResponse> getQuestions() {
        return questions;
    }
}

