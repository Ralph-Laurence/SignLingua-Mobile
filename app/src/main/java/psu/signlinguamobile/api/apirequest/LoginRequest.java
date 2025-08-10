package psu.signlinguamobile.api.apirequest;

public class LoginRequest {
    private String umail;
    private String password;

    public LoginRequest(String umail, String password) {
        this.umail = umail;
        this.password = password;
    }
}