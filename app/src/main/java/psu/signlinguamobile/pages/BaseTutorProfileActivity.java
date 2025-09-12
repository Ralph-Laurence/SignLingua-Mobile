package psu.signlinguamobile.pages;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

import psu.signlinguamobile.api.apiresponse.LoginResponse;
import psu.signlinguamobile.api.apiresponse.TutorProfileDetailsResponse;
import psu.signlinguamobile.api.apiservice.TutorManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.data.TutorProfileBannerCache;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.TutorProfileNavController;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseTutorProfileActivity extends BaseWebViewActivity
{
    protected TutorProfileNavController navController;
    private TutorManagementApiService tutorMgtApiService;
    private boolean m_updateRibbonOnLoad = true;
    private boolean m_cacheRibbonDetailsOnFetch = false;

    @Override
    protected void onInitialize()
    {
        navController = new TutorProfileNavController(this, getWebView());

        // Initialize Retrofit API Service
        tutorMgtApiService = ApiClient.getClient(this, true).create(TutorManagementApiService.class);
    }

    @Override
    protected void onViewLoaded()
    {
        if (m_updateRibbonOnLoad)
        {
            String ribbon = TutorProfileBannerCache.get();

            bridgeCall_execJavascriptFunction("updateRibbon", ribbon);
        }
    }


    protected void goBackToProfileLanding()
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            // already on main thread
            launch(TutorProfileAccountActivity.class);
        }
        else
        {
            runOnUiThread(() -> launch(TutorProfileAccountActivity.class));
        }
    }

    protected TutorManagementApiService getApiService() {
        return tutorMgtApiService;
    }

    /**
     * Controls whether the profile ribbon (username and photo) auto-updates when the page loads.
     * By default, this applies to sections other than 'UserAccount', which fetches full user details
     * and stores them in TutorProfileBannerCache on its own.
     *
     * @param update TRUE  -> Auto-update ribbon on load (default)
     *               FALSE -> Skip auto-update; assume details are already cached
     */
    protected void shouldUpdateRibbonOnLoad(boolean update) {
        m_updateRibbonOnLoad = update;
    }

    /**
     * Caches ribbon details (username and photo) to avoid redundant database reads.
     * Since the UserAccount section is the landing page, caching should occur there.
     * Other sections can reuse the cached data, but should not trigger caching.
     * Set to TRUE only in UserAccount; default to FALSE elsewhere.
     *
     * @param shouldCacheDetails whether ribbon data should be cached on fetch
     */
    protected void cacheRibbonDetailsOnFetch(boolean shouldCacheDetails) {
        m_cacheRibbonDetailsOnFetch = shouldCacheDetails;
    }

    protected void fetchProfileDetails(Call<TutorProfileDetailsResponse> apiRequest, Consumer<String> out)
    {
        apiRequest.enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<TutorProfileDetailsResponse> call, Response<TutorProfileDetailsResponse> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                Log.wtf("MINE", "WTF -> " + response.code());
                // Pending registrations throw 403
//                if (response.code() == HttpCodes.FORBIDDEN)
//                {
//                    try
//                    {
//                        String errorJson = response.errorBody().string();
//                        Gson gson = new Gson();
//                        LoginResponse resp = gson.fromJson(errorJson, LoginResponse.class);
//                        Log.e("MINE", errorJson); // Confirm payload is present
//
//                        HashMap<String, String> extra = new HashMap<>();
//                        extra.put("userId", resp.getUser().getId());
//
//                        // Toast.makeText(BaseTutorProfileActivity.this, resp.getUser().getId(), Toast.LENGTH_SHORT).show();
//                        launchWith(CaptureIDBoardingActivity.class, extra);
//                        return;
//                    }
//                    catch (IOException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }

                if (!getVerificationMw().IsAllowed(response))
                    return;

                if (!response.isSuccessful() || response.body() == null) {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                    return;
                }

                /*
                    Across all the tutor pages, there is always a user details ribbon to display
                    the current user's photo and username. This must only be called after the
                    WebView's successful load eg onViewLoaded(). With that, we store it in a
                    non-persistent (temporary) cache
                 */
                if (m_cacheRibbonDetailsOnFetch)
                    TutorProfileBannerCache.store(response.body().getUsername(), response.body().getPhoto());

                try {
                    String jsonResponse = encodeJson(response.body());

                    bridgeCall_execJavascriptFunction("renderDetails", jsonResponse);

                    if (out != null)
                        out.accept(jsonResponse);

                } catch (Exception e) {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);

                    if (out != null)
                        out.accept("");
                }
            }

            @Override
            public void onFailure(Call<TutorProfileDetailsResponse> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }
}
