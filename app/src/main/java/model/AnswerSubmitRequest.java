package model;

import java.util.List;

public class AnswerSubmitRequest {

    private List<AnswerItem> answers;

    public AnswerSubmitRequest(List<AnswerItem> answers) {
        this.answers = answers;
    }

    public List<AnswerItem> getAnswers() {
        return answers;
    }

    public static class AnswerItem {
        private Long contentId;
        private Long selectedOptionId;

        public AnswerItem(Long contentId, Long selectedOptionId) {
            this.contentId = contentId;
            this.selectedOptionId = selectedOptionId;
        }

        public Long getContentId() {
            return contentId;
        }

        public Long getSelectedOptionId() {
            return selectedOptionId;
        }
    }
}

