package psu.signlinguamobile.api.apiresponse;

import psu.signlinguamobile.models.User;

public class LoginResponse
{
    private String status;
    private String message;
    private String token;
    private User user;

    private boolean requireValidId;

    public boolean getRequireValidId()
    {
        return requireValidId;
    }

    public void setRequireValidId(boolean requireValidId)
    {
        this.requireValidId = requireValidId;
    }

    // Getters for token and user
    public String getStatus() { return status; }
    public String getToken()
    {
        return token;
    }

    public User getUser()
    {
        return user;
    }

    public String getMessage()
    {
        return message;
    }
}