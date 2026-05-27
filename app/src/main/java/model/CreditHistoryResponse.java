package model;

public class CreditHistoryResponse {

    private Long id;
    private String type;
    private String description;
    private Integer amount;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
