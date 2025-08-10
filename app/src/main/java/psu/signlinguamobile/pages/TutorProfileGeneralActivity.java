package psu.signlinguamobile.pages;

import android.util.Log;

import com.google.gson.Gson;

import psu.signlinguamobile.api.apirequest.UpdateProfileGeneralRequest;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.delegates.TutorProfileGeneralJsBridge;
import psu.signlinguamobile.models.UpdateProfileGeneral;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfileGeneralActivity
        extends BaseTutorProfileActivity
        implements TutorProfileGeneralJsBridge.TutorProfileGeneralJsBridgeListener
{

    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        registerJsBridge(new TutorProfileGeneralJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_general");
    }

    @Override
    protected void onBackKey()
    {
        goBackToProfileLanding();
    }

    @Override
    protected void onViewLoaded()
    {
        super.onViewLoaded();
        fetchProfileDetails(getApiService().fetchGeneralDetails(), null);
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

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: BUSINESS LOGIC">
    //=================================================
    @Override
    public void onEditGeneral(String firstname, String lastname, String contact, String address, String about)
    {
        UpdateProfileGeneralRequest request = new UpdateProfileGeneralRequest(firstname, lastname, contact, address, about);
        String test = encodeJson(request);
        Log.d("MINE", "Should send to: " + test);

        getApiService().updateGeneral(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<UpdateProfileGeneral>> call, Response<CommonResponse<UpdateProfileGeneral>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.d("MINE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null)
                {
                    CommonResponse<UpdateProfileGeneral> res = response.body();
                    String msg = res.getMessage();

                    if (res.getStatus() == HttpCodes.NOT_MODIFIED)
                    {
                        bridgeCall_alertWarn(msg);
                        return;
                    }

                    if (res.getStatus() != HttpCodes.SUCCESS)
                    {
                        bridgeCall_function("notifyFailedUpdate", msg);
                        return;
                    }

                    String jsonResponse = encodeJson(response.body());
                    bridgeCall_execJavascriptFunction("notifySuccessfulUpdate", jsonResponse);
                }
                else
                {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse<UpdateProfileGeneral>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    //=================================================
    // </editor-fold>
    //=================================================
}
