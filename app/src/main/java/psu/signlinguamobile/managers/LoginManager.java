package psu.signlinguamobile.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.models.User;

public class LoginManager
{
    private static final String PREFS_NAME_AUTH = Constants.SharedPrefKeys.AUTH;
    private static final String PREFS_KEY_TOKEN = Constants.SharedPrefKeys.TOKEN;
    private static final String PREFS_KEY_USER_ID = Constants.SharedPrefKeys.USER_ID;
    private static final String PREFS_KEY_USER_DETAILS = Constants.SharedPrefKeys.USER_DETAILS;

    private final Context context;

    public LoginManager(Context context)
    {
        this.context = context;
    }

    public void cacheUser(User user, String token)
    {
        // Assume you have a Gson instance;
        Gson gson = new Gson();

        // Inside performLogin() after a successful login:
        // User user = loginResponse.getUser(); // Already contains name, photo, etc.
        String userJson = gson.toJson(user);

        // Save token and user JSON together:
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME_AUTH, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PREFS_KEY_TOKEN, token)
                .putString(PREFS_KEY_USER_ID, user.getId()) // or part of the userJson, but saving separately if desired
                .putString(PREFS_KEY_USER_DETAILS, userJson)
                .commit();

        // SharedPreferences prefsx = getSharedPreferences(PREFS_NAME_AUTH, Context.MODE_PRIVATE);
        // String savedToken = prefsx.getString(PREFS_KEY_TOKEN, null);
        // String savedUserId = prefsx.getString(PREFS_KEY_USER_ID, null);
        // Log.d("Login", String.format("Stored Token: %s, Stored ID: %s", savedToken, savedUserId));

        // Also cache in your session manager:
        SessionManager.getInstance().saveSession(user, token);

    }
}
