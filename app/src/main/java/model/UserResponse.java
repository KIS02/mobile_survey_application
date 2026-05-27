package model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// [추가] GET /api/users/me 응답 모델
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String telephone;
    private String nickname;
    private String gender;
    @SerializedName("birthDate")
    private String birthDate;
    private String region;
    private String location;
    private String occupation;
    private Integer credit;
    private String imagePath;
    private List<PreferenceResponse> preferences;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getNickname() { return nickname; }
    public String getGender() { return gender; }
    public String getBirthDate() { return birthDate; }
    public String getRegion() { return region; }
    public String getLocation() { return location; }
    public String getOccupation() { return occupation; }
    public Integer getCredit() { return credit; }
    public String getImagePath() { return imagePath; }
    public List<PreferenceResponse> getPreferences() { return preferences; }
}
