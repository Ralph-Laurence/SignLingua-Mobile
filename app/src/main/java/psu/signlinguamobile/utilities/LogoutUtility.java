package psu.signlinguamobile.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import psu.signlinguamobile.api.apiservice.AuthApiService;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.pages.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoutUtility
{
    // SharedPreferences keys and file name – adjust these as needed.
    private static final String PREFS_NAME_AUTH = Constants.SharedPrefKeys.AUTH;
    private static final String PREFS_KEY_TOKEN = Constants.SharedPrefKeys.TOKEN;
    private static final String PREFS_KEY_USER_ID = Constants.SharedPrefKeys.USER_ID;
    private static final String PREFS_KEY_USER_DETAILS = Constants.SharedPrefKeys.USER_DETAILS;

    /**
     * Logs out the user by first attempting a network logout (if apiService is provided)
     * and then clearing the local session (both in memory and persisted SharedPreferences).
     *
     * @param context        The activity context.
     * @param authApiService The AuthApiService instance. If null, network logout is skipped.
     */
    public static void logout(final Context context, AuthApiService authApiService) {
        if (authApiService != null) {
            // Initiate network logout.
            authApiService.logout().enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Whether or not the server has successfully logged out,
                    // proceed to clear local session.
                    clearLocalSession(context);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // In case of failure, still clear the local session.
                    clearLocalSession(context);
                }
            });
        } else {
            // If no network logout is needed, directly clear the local session.
            clearLocalSession(context);
        }
    }

    /**
     * Overloaded convenience method to perform only local logout (no network call).
     *
     * @param context The activity context.
     */
    public static void logout(Context context) {
        logout(context, null);
    }

    /**
     * Clears all local data: clears the in-memory session in SessionManager
     * and removes the persisted authentication data from SharedPreferences.
     * Then, it redirects to the LoginActivity, clearing the back stack.
     *
     * @param context The activity context.
     */
    private static void clearLocalSession(Context context) {
        // Clear the in-memory session.
        SessionManager.getInstance().clearSession();

        // Clear SharedPreferences.
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME_AUTH, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(PREFS_KEY_TOKEN)
                .remove(PREFS_KEY_USER_ID)
                .remove(PREFS_KEY_USER_DETAILS)
                .apply();

        // Redirect to login screen and clear the back stack.
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
