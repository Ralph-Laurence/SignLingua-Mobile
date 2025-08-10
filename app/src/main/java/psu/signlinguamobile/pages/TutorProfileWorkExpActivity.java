package psu.signlinguamobile.pages;

import android.util.Log;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiresponse.TutorProfileDetailsResponse;
import psu.signlinguamobile.delegates.TutorProfileWorkExpJsBridge;
import psu.signlinguamobile.models.EducationDocument;
import psu.signlinguamobile.models.WorkDocument;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfileWorkExpActivity
        extends BaseTutorProfileActivity
        implements TutorProfileWorkExpJsBridge.TutorProfileWorkExpJsBridgeListener
{

    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        registerJsBridge(new TutorProfileWorkExpJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_workexp");
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
        fetchProfileDetails(getApiService().fetchWorkExp(), null);
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
    public void onEditWorkExp(String company, String role, int from, int to)
    {
        WorkDocument request = new WorkDocument(company, role, from, to);

        String data = encodeJson(request);
        Log.w("MINE", "Education :: " + data);

        getApiService().updateWorkExperience(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<WorkDocument>> call, Response<CommonResponse<WorkDocument>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.d("MINE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null)
                {
                    CommonResponse<WorkDocument> res = response.body();
                    String msg = res.getMessage();

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
            public void onFailure(Call<CommonResponse<WorkDocument>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onRemoveWorkExp(String docId)
    {
        WorkDocument request = new WorkDocument(null, null, null, docId);

        Log.w("MINE", "DOC ID -> " + request.getDocId());

        getApiService().deleteWorkExp(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<TutorProfileDetailsResponse>> call, Response<CommonResponse<TutorProfileDetailsResponse>> response)
            {
                Log.w("MINE", "It works here");

                bridgeCall_hideWebViewLoadingOverlay();

                Log.d("MINE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null)
                {
                    CommonResponse<TutorProfileDetailsResponse> res = response.body();
                    String msg = res.getMessage();

                    if (res.getStatus() != HttpCodes.SUCCESS)
                    {
                        bridgeCall_function("notifyFailedDelete", msg);
                        return;
                    }

                    String jsonResponse = encodeJson(response.body());
                    bridgeCall_execJavascriptFunction("notifySuccessfulDelete", jsonResponse);
                }
                else
                {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse<TutorProfileDetailsResponse>> call, Throwable t)
            {
                Log.w("MINE", "It fails here");
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    //=================================================
    // </editor-fold>
    //=================================================


}