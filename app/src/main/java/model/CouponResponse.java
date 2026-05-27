package model;

public class CouponResponse {

    private Long id;
    private String rewardName;
    private String barcode;
    private Boolean isUsed;
    private String expiredAt;
    private String exchangedAt;

    public Long getId() {
        return id;
    }

    public String getRewardName() {
        return rewardName;
    }

    public String getBarcode() {
        return barcode;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public String getExchangedAt() {
        return exchangedAt;
    }
}
