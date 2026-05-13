package model;

public class GoogleLoginRequest {

    private String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
}
