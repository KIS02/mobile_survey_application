package model.admin;

import java.util.List;

public class CreateQuestionRequest {
    private String questionText;
    private String questionType;
    private short orderNum;
    private Short correctOptionOrder;
    private List<CreateOptionRequest> options;

    public CreateQuestionRequest(String questionText, String questionType,
                                 short orderNum, Short correctOptionOrder,
                                 List<CreateOptionRequest> options) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.orderNum = orderNum;
        this.correctOptionOrder = correctOptionOrder;
        this.options = options;
    }

    public String getQuestionText() { return questionText; }
    public String getQuestionType() { return questionType; }
    public short getOrderNum() { return orderNum; }
    public Short getCorrectOptionOrder() { return correctOptionOrder; }
    public List<CreateOptionRequest> getOptions() { return options; }
}
