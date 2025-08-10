package psu.signlinguamobile.api.apirequest;

public class UpdatePasswordRequest
{
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    public UpdatePasswordRequest(String oldPassword, String newPassword, String confirmPassword)
    {
        this.oldPassword        = oldPassword;
        this.newPassword        = newPassword;
        this.confirmPassword    = confirmPassword;
    }
}