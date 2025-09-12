package psu.signlinguamobile.pages;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import psu.signlinguamobile.api.apiresponse.LoginResponse;
import psu.signlinguamobile.api.apiservice.RegistrationApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.RegistrationJsBridge;
import psu.signlinguamobile.managers.LoginManager;
import psu.signlinguamobile.models.RegistrationDetails;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends BaseWebViewActivity
        implements RegistrationJsBridge.RegistrationBridgeListener
{
    private RegistrationApiService registrationApiService;
    private LoginManager loginManager;
    private int registrationMode;

    @Override
    protected void onAwake()
    {
        super.onAwake();
        shouldCheckAuth(false);
        useDarkStatusIcons(true);
    }

    @Override
    protected void onInitialize()
    {
        registerJsBridge(new RegistrationJsBridge(this), JS_BRIDGE_NAME);
        renderView("registration");

        registrationApiService = ApiClient.getClient(this).create(RegistrationApiService.class);
        loginManager = new LoginManager(this.getApplicationContext());

        String regMode = getIntent().getStringExtra("registrationMode");

        if (regMode == null || regMode.isEmpty())
            registrationMode = -1;

        else
            registrationMode = Integer.parseInt(regMode);
    }

    @Override
    protected void onBackKey()
    {

    }

    @Override
    public void onGoBack()
    {

    }

    @Override
    protected void onViewLoaded()
    {
        bridgeCall_function("setRegistrationMode", registrationMode);
    }

    @Override
    protected void onDispose()
    {
        unregisterJsBridge(JS_BRIDGE_NAME); // Prevents leaks
    }

    @Override
    public void onCancelRegistration()
    {
        launch(RegistrationLanding.class);
    }

    @Override
    public void onRegister(String jsonPayload)
    {
        Log.d("MINE", jsonPayload);

        try
        {
            RegistrationDetails payload = getRegistrationDetails(jsonPayload);

            registrationApiService.registerBasic(payload).enqueue(new Callback<>()
            {
                /**
                 * This is called as long as the server responds, regardless of whether the response
                 * is a success (200 OK) or an error (404, 422, 500, etc). Even if the server sends
                 * back a blank body with a 500, Retrofit still considers that a valid response and
                 * routes it to onResponse.
                 */
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response)
                {
                    bridgeCall_hideWebViewLoadingOverlay();

                    Log.e("MINE", "CODE: " + response.code());

                    if (response.isSuccessful() && response.body() != null)
                        handleSuccessfulRegistration(response);

                    else
                        handleFailedRegistration(response);
                }

                /**
                 * This is triggered only when Retrofit cannot complete the HTTP transaction, due to:
                 * <ul>
                 *  <li>Network issues (no internet, timeout, DNS failure)</li>
                 *  <li>SSL handshake errors</li>
                 *  <li>Serialization/deserialization failures (e.g. malformed JSON, unexpected types)</li>
                 *  <li>Retrofit misconfig (wrong base URL, missing converter).</li>
                 * </ul>
                 * <p>
                 * It does not catch exceptions thrown inside onResponse. If you crash inside onResponse,
                 * that exception bubbles up like any other runtime error — and you'll need to catch it yourself
                 * or let your crash handler/logging system deal with it.
                 * </p>
                 */
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t)
                {
                    bridgeCall_hideWebViewLoadingOverlay();

                    Toast.makeText(RegistrationActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("MINE", "WTF Error: " + t.getMessage());

                    bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
                }
            });
        }
        catch (JSONException e)
        {
            Log.d("MINE", e.getMessage());
        }
    }

    @NonNull
    private RegistrationDetails getRegistrationDetails(String jsonPayload) throws JSONException
    {
        JSONObject data = new JSONObject(jsonPayload);

        return new RegistrationDetails
        (
            data.optInt("role"),
            data.optString("firstname"),
            data.optString("lastname"),
            data.optInt("impairment"),
            data.optString("contact"),
            data.optString("address"),
            data.optString("username"),
            data.optString("email"),
            data.optString("password"),
            data.optString("confirm")
        );
    }

    private void handleSuccessfulRegistration(Response<LoginResponse> response)
    {
        if (!response.isSuccessful() || response.body() == null)
        {
            bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
            return;
        }

        LoginResponse registrationResponse = response.body();
        String token = registrationResponse.getToken();
        User user = registrationResponse.getUser();


        // Log the token for debugging
        Log.d("Login", "Token: " + token);
        /*
        // Log various user details
        Log.d("Login", "User ID: " + user.getId());
        Log.d("Login", "First Name: " + user.getFirstname());
        Log.d("Login", "Last Name: " + user.getLastname());
        Log.d("Login", "Username: " + user.getUsername());
        Log.d("Login", "Email: " + user.getEmail());
        Log.d("Login", "Role: " + user.getRole());     // raw role integer
        Log.d("Login", "Role Display: " + User.Role.fromInt(user.getRole()).getDisplayName());
        Log.d("Login", "Contact: " + user.getContact());
        Log.d("Login", "Address: " + user.getAddress());
        Log.d("Login", "Photo: " + user.getPhoto()); // Constructed photo URL
        */

        Log.d("Registration", new Gson().toJson(response.body()));

        // we only cache login details of verified users
        loginManager.cacheUser(user, token);

        // If the registration was for tutor, ask for their valid ID
        if (user.getRole() == User.Role.TUTOR.getValue())
        {
            if (registrationResponse.getRequireValidId())
            {
                launch(CaptureIDBoardingActivity.class);
                return;
            }

            launch(TutorHomeActivity.class);
        }
        else if (user.getRole() == User.Role.LEARNER.getValue())
            launch(LearnerHomeActivity.class);
    }

    private void handleFailedRegistration(Response<LoginResponse> response)
    {
        // Fallback message
        String errMessage = UXMessages.ERR_TECHNICAL;

        try (ResponseBody errorJson = response.errorBody())
        {
            if (errorJson != null)
            {
                JSONObject obj = new JSONObject(errorJson.string());
                String err = obj.optString("message");

                if (!err.isEmpty())
                    errMessage = err;
            }
        }
        catch (Exception ex) { errMessage = UXMessages.ERR_PARSE_RESPONSE; }
        finally { bridgeCall_alertWarn(errMessage); }
    }
}
