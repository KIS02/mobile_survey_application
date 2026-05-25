package model;

import java.util.List;

public class RegisterRequest {

    private String name;
    private String telephone;
    private String nickname;
    private String gender;
    private String birthDate;
    private String location;
    private String region;
    private String occupation;
    private List<Long> categoryIds;

    public RegisterRequest(String name, String telephone, String nickname, String gender,
                           String birthDate, String location, String region,
                           String occupation, List<Long> categoryIds) {
        this.name = name;
        this.telephone = telephone;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.location = location;
        this.region = region;
        this.occupation = occupation;
        this.categoryIds = categoryIds;
    }
}
