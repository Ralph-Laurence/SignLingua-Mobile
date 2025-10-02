package psu.signlinguamobile.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.BookingManagementApiService;
import psu.signlinguamobile.api.apiservice.LearnerManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.LearnerDetailsJsBridge;
import psu.signlinguamobile.api.apiresponse.LearnerDetailsResponse;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnerDetailsActivity
        extends BaseWebViewActivity
        implements LearnerDetailsJsBridge.LearnerDetailsJsBridgeListener
{
    private LearnerManagementApiService learnerMgtApiService;
    private BookingManagementApiService bookingManagementApiService;
    private String learnerId;

    @Override
    public void onGoBack()
    {
        goBack();
    }

    @Override
    protected void onInitialize()
    {
        learnerId = getIntent().getStringExtra("learnerId");

        // Initialize Retrofit API Service
        learnerMgtApiService = ApiClient.getClient(this, true).create(LearnerManagementApiService.class);
        bookingManagementApiService = ApiClient.getClient(this, true).create(BookingManagementApiService.class);

        registerJsBridge(new LearnerDetailsJsBridge(this), JS_BRIDGE_NAME);
        renderView("learner_details");
    }

    @Override
    protected void onBackKey()
    {
        goBack();
    }

    @Override
    protected void onViewLoaded()
    {
        fetchDetails();
    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    private void goBack()
    {
        Intent intent = null;

        String launchedFrom = getIntent().getStringExtra("launchedFrom");

        if (launchedFrom.equals("myLearners"))
            intent = new Intent(LearnerDetailsActivity.this, MyLearnersActivity.class);
        else if (launchedFrom.equals("findLearners"))
            intent = new Intent(LearnerDetailsActivity.this, FindLearnersActivity.class);
        else if (launchedFrom.equals("hireRequests"))
            intent = new Intent(LearnerDetailsActivity.this, TutorHireRequestsActivity.class);

        if (intent != null)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void fetchDetails()
    {
        Intent intent = getIntent();
        String learnerId = intent.getStringExtra("learnerId");

        Log.d("MINE", "Learner ID -> " + learnerId);
        learnerMgtApiService.showLearner(learnerId).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<LearnerDetailsResponse> call, Response<LearnerDetailsResponse> response)
            {
                Log.d("MINE", String.valueOf(response.code()));

                if (!getVerificationMw().IsAllowed(response))
                    return;

                if (!response.isSuccessful() || response.body() == null)
                {
                    Log.e("RETROFIT_ERROR", "API failed with code: " + response.code());
                    alert(UXMessages.ERR_TECHNICAL, "Failure", (d,i) -> goBack());
                    return;
                }

                try
                {
                    String jsonResponse = encodeJson(response.body());
                    bridgeCall_execJavascriptFunction("renderDetails", jsonResponse);

                } catch (Exception e) {
                    launch(GlobalCrashHandler.class);
                }
            }

            @Override
            public void onFailure(Call<LearnerDetailsResponse> call, Throwable t)
            {
                alert(UXMessages.ERR_NETWORK, "Network Error", (d,i) -> goBack());
            }
        });
    }

    @Override
    public void onConfirmRequest()
    {
        bookingManagementApiService.confirmHiringRequest(learnerId).enqueue(new Callback<>()
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
                        CommonResponse<Void> errorResponse = gson.fromJson(errorJson, new TypeToken<CommonResponse<Void>>()
                        {
                        }.getType());

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

                bridgeCall_execJavascriptFunction("handleRequestConfirmed");
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onDeclineRequest()
    {
        bookingManagementApiService.declineHiringRequest(learnerId).enqueue(new Callback<>()
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

                bridgeCall_execJavascriptFunction("handleRequestDeclined");
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onDropLearner()
    {
        bookingManagementApiService.dropLearner(learnerId).enqueue(new Callback<>()
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

                bridgeCall_execJavascriptFunction("handleLearnerDropped");
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onAddLearner()
    {
        bookingManagementApiService.addLearner(learnerId).enqueue(new Callback<>()
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

                        if (response.code() == HttpCodes.VALIDATION_ERROR
                         || response.code() == HttpCodes.CONFLICT)
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

                bridgeCall_execJavascriptFunction("handleLearnerAdded");
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onCancelRequest()
    {
        bookingManagementApiService.cancelRequest(learnerId).enqueue(new Callback<>()
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

                bridgeCall_execJavascriptFunction("handleRequestCanceled");
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    private void alert(String message, String title, DialogInterface.OnClickListener onOK)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title.isEmpty() ? getString(R.string.app_name) : title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", onOK);
        builder.setIcon(R.drawable.app_logo_xl);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}