package model;

// [추가] GET /api/rewards 응답 모델

public class RewardResponse {

    private Long id;
    private String name;
    private Integer requiredCredit;
    private String description;
    private String imagePath;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getRequiredCredit() { return requiredCredit; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
}
