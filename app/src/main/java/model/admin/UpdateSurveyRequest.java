package model.admin;

import java.util.List;

public class UpdateSurveyRequest {
    private String title;
    private String description;
    private Long categoryId;
    private Integer reward;
    private Integer targetCount;
    private String startAt;
    private String endAt;
    private String targetGender;
    private Short targetAgeMin;
    private Short targetAgeMax;
    private List<CreateQuestionRequest> questions;

    public UpdateSurveyRequest(String title, String description, Long categoryId,
                                Integer reward, Integer targetCount,
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

    /** ACTIVE 상태 — 날짜만 수정 */
    public UpdateSurveyRequest(String startAt, String endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
