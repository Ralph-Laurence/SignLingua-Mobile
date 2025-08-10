package psu.signlinguamobile.pages;

import android.util.Log;

import psu.signlinguamobile.api.apirequest.UpdatePasswordRequest;
import psu.signlinguamobile.api.apirequest.UpdateSkillsRequest;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.delegates.TutorProfileSkillsJsBridge;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfileSkillsActivity
        extends BaseTutorProfileActivity
        implements TutorProfileSkillsJsBridge.TutorProfileSkillsJsBridgeListener
{

    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        registerJsBridge(new TutorProfileSkillsJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_skills");
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
        fetchProfileDetails(getApiService().fetchSkills(), null);
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
    public void onEditSkills(int disability, String[] skills)
    {
        runOnUiThread(() -> handleSave(disability, skills));
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: BUSINESS LOGIC">
    //=================================================

    private void handleSave(int disability, String[] skills)
    {
        String debug = String.format("Disability -> %s, Skills -> [%s]", disability, String.join(",", skills));
        Log.d("MINE", debug);

        UpdateSkillsRequest request = new UpdateSkillsRequest(disability, skills);

        getApiService().updateSkills(request).enqueue(new Callback<>()
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

                    bridgeCall_showSnackbar(msg);
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