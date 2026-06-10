package model.admin;

import java.util.List;

public class PreviewReliabilityRequest {

    private List<QuestionInput> questions;
    private String categoryName;
    private int similarCount;
    private int reverseCount;

    public PreviewReliabilityRequest(List<QuestionInput> questions, String categoryName,
                                     int similarCount, int reverseCount) {
        this.questions = questions;
        this.categoryName = categoryName;
        this.similarCount = similarCount;
        this.reverseCount = reverseCount;
    }

    public List<QuestionInput> getQuestions() { return questions; }
    public String getCategoryName() { return categoryName; }
    public int getSimilarCount() { return similarCount; }
    public int getReverseCount() { return reverseCount; }

    public static class QuestionInput {
        private String questionText;
        private Long optionSetId;

        public QuestionInput(String questionText, Long optionSetId) {
            this.questionText = questionText;
            this.optionSetId = optionSetId;
        }

        public String getQuestionText() { return questionText; }
        public Long getOptionSetId() { return optionSetId; }
    }
}
