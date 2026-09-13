package assembly.general.api.dto;

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserSummary user;

    public LoginResponse(String accessToken, String tokenType, long expiresIn, UserSummary user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public UserSummary getUser() { return user; }
}