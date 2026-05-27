package model;

import com.google.gson.annotations.SerializedName;

public class UserUpdateRequest {

    private String name;
    private String telephone;
    private String nickname;
    @SerializedName("birthDate")
    private String birthDate;
    private String location;
    private String region;

    public UserUpdateRequest(String name, String telephone, String nickname,
                             String birthDate, String location, String region) {
        this.name = name;
        this.telephone = telephone;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.location = location;
        this.region = region;
    }

    public String getBirthDate() {
        return birthDate;
    }
}
