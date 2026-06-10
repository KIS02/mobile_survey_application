package model.admin;

import java.util.List;

public class PreviewReliabilityResponse {

    private List<GeneratedQuestion> questions;

    public List<GeneratedQuestion> getQuestions() { return questions; }

    public static class GeneratedQuestion {
        private String questionType;
        private String questionText;
        private Short correctOptionOrder;
        private Integer basedOnOriginalIndex;
        private Long optionSetId;

        public String getQuestionType() { return questionType; }
        public String getQuestionText() { return questionText; }
        public Short getCorrectOptionOrder() { return correctOptionOrder; }
        public Integer getBasedOnOriginalIndex() { return basedOnOriginalIndex; }
        public Long getOptionSetId() { return optionSetId; }
        public void setBasedOnOriginalIndex(Integer index) { this.basedOnOriginalIndex = index; }
    }
}
