package psu.signlinguamobile.api.apirequest;

public class UpdateProfileAccountRequest
{
    private String bio;
    private String username;
    private String email;

    public UpdateProfileAccountRequest(String email, String username, String bio)
    {
        this.email      = email;
        this.username   = username;
        this.bio        = bio;
    }
}
