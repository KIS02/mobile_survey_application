package model;

public class GoogleLoginResponse {

    private boolean newUser;

    // 기존 회원
    private String accessToken;
    private String refreshToken;
    private String role;

    // 신규 회원
    private String tempToken;
    private String name;

    public boolean isNewUser() { return newUser; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getRole() { return role; }
    public String getTempToken() { return tempToken; }
    public String getName() { return name; }
}
