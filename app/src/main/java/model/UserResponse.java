package model;

// [추가] GET /api/user/me 응답 모델
public class UserResponse {
    private Long id;
    private String name;
    private String nickname;
    private String gender;
    private String region;
    private String occupation;
    private Integer credit;
    private String imagePath;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNickname() { return nickname; }
    public String getGender() { return gender; }
    public String getRegion() { return region; }
    public String getOccupation() { return occupation; }
    public Integer getCredit() { return credit; }
    public String getImagePath() { return imagePath; }
}
