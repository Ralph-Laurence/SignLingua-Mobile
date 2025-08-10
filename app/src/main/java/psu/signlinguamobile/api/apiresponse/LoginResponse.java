package psu.signlinguamobile.api.apiresponse;

import psu.signlinguamobile.models.User;

public class LoginResponse
{
    private String message;
    private String token;
    private User user;

    // Getters for token and user
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