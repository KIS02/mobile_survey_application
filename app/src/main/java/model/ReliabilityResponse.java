package model;

import java.math.BigDecimal;

public class ReliabilityResponse {

    private BigDecimal trustWeight;
    private Boolean instructionPassed;
    private Boolean bogusPassed;
    private Boolean isReverseFailed;
    private Boolean isSimilarFailed;
    private Boolean isTooFast;
    private Integer responseTimeSec;

    public BigDecimal getTrustWeight() {
        return trustWeight;
    }

    public Boolean getInstructionPassed() {
        return instructionPassed;
    }

    public Boolean getBogusPassed() {
        return bogusPassed;
    }

    public Boolean getIsReverseFailed() {
        return isReverseFailed;
    }

    public Boolean getIsSimilarFailed() {
        return isSimilarFailed;
    }

    public Boolean getIsTooFast() {
        return isTooFast;
    }

    public Integer getResponseTimeSec() {
        return responseTimeSec;
    }
}

