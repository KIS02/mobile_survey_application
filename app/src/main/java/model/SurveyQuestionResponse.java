package model;

import java.util.List;

public class SurveyQuestionResponse {

    private Long id;
    private String content;
    private String type;
    private List<SurveyOptionResponse> options;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public List<SurveyOptionResponse> getOptions() {
        return options;
    }
}

