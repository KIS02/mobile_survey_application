package model.admin;

import java.util.List;

public class AdminSurveyResponse {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer reward;
    private Integer targetCount;
    private String status;
    private String startAt;
    private String endAt;
    private String targetGender;
    private Short targetAgeMin;
    private Short targetAgeMax;
    private List<QuestionResponse> questions;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Integer getReward() { return reward; }
    public Integer getTargetCount() { return targetCount; }
    public String getStatus() { return status; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public String getTargetGender() { return targetGender; }
    public Short getTargetAgeMin() { return targetAgeMin; }
    public Short getTargetAgeMax() { return targetAgeMax; }
    public List<QuestionResponse> getQuestions() { return questions; }

    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String questionType;
        private Short orderNum;
        private Short correctOptionOrder;
        private List<OptionResponse> options;

        public Long getId() { return id; }
        public String getQuestionText() { return questionText; }
        public String getQuestionType() { return questionType; }
        public Short getOrderNum() { return orderNum; }
        public Short getCorrectOptionOrder() { return correctOptionOrder; }
        public List<OptionResponse> getOptions() { return options; }
    }

    public static class OptionResponse {
        private Long id;
        private String optionText;
        private Short optionOrder;

        public Long getId() { return id; }
        public String getOptionText() { return optionText; }
        public Short getOptionOrder() { return optionOrder; }
    }
}
