package psu.signlinguamobile.pages;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.BookingManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.TutorHireRequestsActivityJsBridge;
import psu.signlinguamobile.models.HiringRequestItem;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This is a "Friend Requests" activity for tutors
 */
public class TutorHireRequestsActivity
        extends BaseWebViewActivity
        implements TutorHireRequestsActivityJsBridge.TutorHireRequestsActivityJsBridgeListener
{
    private BookingManagementApiService apiService;

    @Override
    protected void onInitialize()
    {
        apiService = ApiClient.getClient(this, true).create(BookingManagementApiService.class);
        registerJsBridge(new TutorHireRequestsActivityJsBridge(this), JS_BRIDGE_NAME);
        renderView("tutor_friend_requests");
    }

    @Override
    protected void onBackKey()
    {
        launch(TutorHomeActivity.class);
    }

    @Override
    protected void onViewLoaded()
    {
        fetchHireRequests(apiService);
    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    @Override
    public void onFindLearners()
    {
        launch(FindLearnersActivity.class);
    }

    @Override
    public void onGoBack()
    {
        launch(TutorHomeActivity.class);
    }

    @Override
    public void onShowLearnerInfo(String learnerId)
    {
        HashMap<String, String> extras = new HashMap<>();
        extras.put("launchedFrom", "hireRequests");
        extras.put("learnerId", String.valueOf(learnerId));

        launchWith(LearnerDetailsActivity.class, extras);
    }

    @Override
    public void onConfirmRequest(String learnerId)
    {
        apiService.confirmHiringRequest(learnerId).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<Void>> call, Response<CommonResponse<Void>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.wtf("MINE", "WTF -> " + response.code());

                if (!getVerificationMw().IsAllowed(response))
                    return;

                if (!response.isSuccessful())
                {
                    try
                    {
                        String errorJson = response.errorBody().string(); // Only call .string() once
                        Log.d("MINE", "Raw error: " + errorJson);

                        Gson gson = new Gson();
                        CommonResponse<Void> errorResponse = gson.fromJson(errorJson, new TypeToken<CommonResponse<Void>>(){}.getType());

                        if (response.code() == HttpCodes.VALIDATION_ERROR)
                            bridgeCall_alertWarn(errorResponse.getMessage());

                        else
                            bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);

                    }
                    catch (IOException e)
                    {
                        Log.e("MINE", "Error parsing response", e);
                        bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                    }

                    return;
                }

                CommonResponse<Void> res = response.body();
                String msg = res.getMessage();
                bridgeCall_function("dequeueRequestItem", learnerId);
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onDeclineRequest(String learnerId)
    {
        apiService.declineHiringRequest(learnerId).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<Void>> call, Response<CommonResponse<Void>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.wtf("MINE", "WTF -> " + response.code());

                if (!getVerificationMw().IsAllowed(response))
                    return;

                if (!response.isSuccessful())
                {
                    try
                    {
                        String errorJson = response.errorBody().string(); // Only call .string() once
                        Log.d("MINE", "Raw error: " + errorJson);

                        Gson gson = new Gson();
                        CommonResponse<Void> errorResponse = gson.fromJson(errorJson, new TypeToken<CommonResponse<Void>>(){}.getType());

                        if (response.code() == HttpCodes.VALIDATION_ERROR)
                            bridgeCall_alertWarn(errorResponse.getMessage());

                        else
                            bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);

                    }
                    catch (IOException e)
                    {
                        Log.e("MINE", "Error parsing response", e);
                        bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                    }

                    return;
                }

                CommonResponse<Void> res = response.body();
                String msg = res.getMessage();
                bridgeCall_function("dequeueRequestItem", learnerId);
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    protected void fetchHireRequests(BookingManagementApiService apiService)
    {
        apiService.getHiringRequests().enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<List<HiringRequestItem>>> call, Response<CommonResponse<List<HiringRequestItem>>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.wtf("MINE", "WTF -> " + response.code());

                if (!getVerificationMw().IsAllowed(response))
                    return;

                if (!response.isSuccessful() || response.body() == null) {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                    return;
                }

                CommonResponse<List<HiringRequestItem>> data = response.body();
                List<HiringRequestItem> requests = data.getContent();

                if (requests.isEmpty())
                {
                    bridgeCall_execJavascriptFunction("showEmptyContentWrapper");
                    return;
                }

                bridgeCall_execJavascriptFunction("renderContent", encodeJson(requests));
            }

            @Override
            public void onFailure(Call<CommonResponse<List<HiringRequestItem>>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }
}
