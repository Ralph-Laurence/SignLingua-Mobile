package psu.signlinguamobile.pages;

import android.util.Log;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiresponse.TutorProfileDetailsResponse;
import psu.signlinguamobile.delegates.TutorProfileCertificationsJsBridge;
import psu.signlinguamobile.models.CertificationDocument;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfileCertificationsActivity
        extends BaseTutorProfileActivity
        implements TutorProfileCertificationsJsBridge.TutorProfileCertificationsJsBridgeListener
{

    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        registerJsBridge(new TutorProfileCertificationsJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_certs");
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
        fetchProfileDetails(getApiService().fetchCertifications(), null);
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
    public void onEditCertification(String from, String certification, String description)
    {
//        String data = String.format("From: %s, Title: %s, Desc: %s", from, certification, description);
//        Log.w("MINE", "Certs :: " + data);

        CertificationDocument request = new CertificationDocument(from, certification, description, null);

        getApiService().updateCertification(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<CertificationDocument>> call, Response<CommonResponse<CertificationDocument>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.d("MINE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null)
                {
                    CommonResponse<CertificationDocument> res = response.body();
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
            public void onFailure(Call<CommonResponse<CertificationDocument>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onRemoveCertification(String docId)
    {
        CertificationDocument request = new CertificationDocument(null, null, null, docId);

        Log.w("MINE", "DOC ID -> " + request.getDocId());

        getApiService().deleteCertification(request).enqueue(new Callback<>()
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