package psu.signlinguamobile.pages;

import android.util.Log;

import psu.signlinguamobile.api.apirequest.UpdatePasswordRequest;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.delegates.TutorProfilePasswordsJsBridge;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfilePasswordsActivity
       extends BaseTutorProfileActivity
       implements TutorProfilePasswordsJsBridge.TutorProfilePasswordsJsBridgeListener
{

    //private TutorManagementApiService tutorMgtApiService;

    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        this.registerJsBridge(new TutorProfilePasswordsJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_passwords");
    }

    @Override
    protected void onBackKey()
    {
        goBackToProfileLanding();
    }


    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: JS BRIDGE METHODS">
    //=================================================

    @Override
    public void onGoBack()
    {
        goBackToProfileLanding();
    }

    @Override
    public void onSavePassword(String oldPassword, String newPassword, String confirmPassword)
    {
        runOnUiThread(() -> performUpdate(oldPassword, newPassword, confirmPassword));
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: BUSINESS LOGIC">
    //=================================================

    private void performUpdate(String oldPassword, String newPassword, String confirmPassword)
    {
        Log.d("MINE", oldPassword + " " + newPassword + " " + confirmPassword);
        UpdatePasswordRequest request = new UpdatePasswordRequest(oldPassword, newPassword, confirmPassword);

        getApiService().updatePassword(request).enqueue(new Callback<>()
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

                    // bridgeCall_showSnackbar(msg);
                    bridgeCall_execJavascriptFunction("notifySuccessfulPasswordUpdate(\"" + msg + "\")");
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

    //=================================================
    // </editor-fold>
    //=================================================
}