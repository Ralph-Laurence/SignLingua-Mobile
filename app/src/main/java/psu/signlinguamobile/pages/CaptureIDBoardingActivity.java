package psu.signlinguamobile.pages;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.AuthApiService;
import psu.signlinguamobile.api.apiservice.RegistrationApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.delegates.CaptureIDBoardingJsBridge;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptureIDBoardingActivity
        extends BaseWebViewActivity
        implements CaptureIDBoardingJsBridge.CaptureIDBoardingJsBridgeListener
{
    private String userId;

    private RegistrationApiService registrationApiService;
    private AuthApiService authApiService;

    @Override
    protected void onAwake()
    {
        shouldCheckAuth(false);
    }

    @Override
    protected void onInitialize()
    {
        authApiService = ApiClient.getClient(this).create(AuthApiService.class);
        registrationApiService = ApiClient.getClient(this, true).create(RegistrationApiService.class);

        registerJsBridge(new CaptureIDBoardingJsBridge(this), JS_BRIDGE_NAME);
        userId = getIntent().getStringExtra("userId");
        renderView("registration_boarding.html");
    }

    @Override
    protected void onBackKey()
    {
        bridgeCall_execJavascriptFunction("promptCancelRegistration");
    }

    @Override
    protected void onViewLoaded()
    {
    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    @Override
    public void onCancelRegistration()
    {
        registrationApiService.cancelRegistration().enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<Void>> call, Response<CommonResponse<Void>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.d("MINE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null)
                {
                    CommonResponse<Void> res = response.body();
                    String msg = res.getMessage();

                    if (res.getStatus() != HttpCodes.SUCCESS)
                    {
                        bridgeCall_alertWarn(msg);
                        return;
                    }

                    clearLocalSession();
                }
                else
                {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onProceedRegistration()
    {
        // Toast.makeText(this, "PROCEED REGISTRATION", Toast.LENGTH_SHORT).show();
        launch(CaptureIDMainActivity.class);
    }

    @Override
    public void onTerminateAppOnCriticalError()
    {

    }

    private void clearLocalSession()
    {
        Context context = getApplicationContext();

        // Clear the in-memory session.
        SessionManager.getInstance().clearSession();

        // Clear SharedPreferences.
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPrefKeys.AUTH, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(Constants.SharedPrefKeys.TOKEN)
                .remove(Constants.SharedPrefKeys.USER_ID)
                .remove(Constants.SharedPrefKeys.USER_DETAILS)
                .apply();

        // Redirect to login screen and clear the back stack.
        launch(LoginActivity.class);
    }
}
