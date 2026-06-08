package model.admin;

public class CreateQuestionRequest {
    private String questionText;
    private String questionType;
    private short orderNum;
    private Short correctOptionOrder;
    private long optionSetId;
    /** 유사 문항일 때 기반한 원본 문항의 questions 리스트 인덱스 (0-based) */
    private Long similarId;
    /** 반대 문항일 때 기반한 원본 문항의 questions 리스트 인덱스 (0-based) */
    private Long reverseId;

    public CreateQuestionRequest(String questionText, String questionType,
                                 short orderNum, Short correctOptionOrder,
                                 long optionSetId) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.orderNum = orderNum;
        this.correctOptionOrder = correctOptionOrder;
        this.optionSetId = optionSetId;
    }

    public CreateQuestionRequest(String questionText, String questionType,
                                 short orderNum, Short correctOptionOrder,
                                 long optionSetId, Long similarId, Long reverseId) {
        this(questionText, questionType, orderNum, correctOptionOrder, optionSetId);
        this.similarId = similarId;
        this.reverseId = reverseId;
    }

    public String getQuestionText() { return questionText; }
    public String getQuestionType() { return questionType; }
    public short getOrderNum() { return orderNum; }
    public Short getCorrectOptionOrder() { return correctOptionOrder; }
    public long getOptionSetId() { return optionSetId; }
    public Long getSimilarId() { return similarId; }
    public Long getReverseId() { return reverseId; }
}
