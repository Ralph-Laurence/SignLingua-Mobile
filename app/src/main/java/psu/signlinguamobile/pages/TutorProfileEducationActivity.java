package psu.signlinguamobile.pages;

import android.util.Log;

import com.google.gson.Gson;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiresponse.TutorProfileDetailsResponse;
import psu.signlinguamobile.delegates.TutorProfileEducationJsBridge;
import psu.signlinguamobile.models.CertificationDocument;
import psu.signlinguamobile.models.EducationDocument;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfileEducationActivity
        extends BaseTutorProfileActivity
        implements TutorProfileEducationJsBridge.TutorProfileEducationJsBridgeListener
{

    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        registerJsBridge(new TutorProfileEducationJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_education");
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
        fetchProfileDetails(getApiService().fetchEducation(), null);
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
    public void onEditEducation(String institution, String degree, int from, int to)
    {
        EducationDocument request = new EducationDocument(institution, degree, from, to);

        String data = encodeJson(request);
        Log.w("MINE", "Education :: " + data);

        getApiService().updateEducation(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<EducationDocument>> call, Response<CommonResponse<EducationDocument>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.d("MINE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null)
                {
                    CommonResponse<EducationDocument> res = response.body();
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
            public void onFailure(Call<CommonResponse<EducationDocument>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onRemoveEducation(String docId)
    {
        EducationDocument request = new EducationDocument(null, null, null, docId);

        Log.w("MINE", "DOC ID -> " + request.getDocId());

        getApiService().deleteEducation(request).enqueue(new Callback<>()
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