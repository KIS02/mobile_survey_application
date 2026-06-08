package model.admin;

public class AdminSurveyListItem {
    private Long id;
    private String title;
    private String status;
    private String categoryName;
    private Integer reward;
    private Integer targetCount;
    private long responseCount;
    private int questionCount;
    private String startAt;
    private String endAt;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getCategoryName() { return categoryName; }
    public Integer getReward() { return reward; }
    public Integer getTargetCount() { return targetCount; }
    public long getResponseCount() { return responseCount; }
    public int getQuestionCount() { return questionCount; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
}
