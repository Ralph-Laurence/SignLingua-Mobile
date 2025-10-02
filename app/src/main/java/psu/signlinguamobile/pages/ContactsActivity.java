package psu.signlinguamobile.pages;

import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;

import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.ChatApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.ContactsJsBridge;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.models.ContactResponse;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.UXMessages;
import psu.signlinguamobile.utilities.VerificationMiddlewareChecker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsActivity extends BaseWebViewActivity implements ContactsJsBridge.ContactsJsBridgeListener
{
    private ChatApiService chatApiClient;
    private VerificationMiddlewareChecker verificationMw;
    private User m_user;

    @Override
    protected void onAwake()
    {
        super.onAwake();
        useDarkStatusIcons(true);
    }

    @Override
    protected void onInitialize()
    {
        // Initialize Retrofit API Service
        chatApiClient  = ApiClient.getClient(this, true).create(ChatApiService.class);
        verificationMw = new VerificationMiddlewareChecker(this);
        m_user = SessionManager.getInstance().getCurrentUser();

        registerJsBridge(new ContactsJsBridge(this), JS_BRIDGE_NAME);
        renderView("contacts");
    }

    @Override
    protected void onBackKey()
    {
        if (m_user == null)
            return;

        if (m_user.getRole() == User.Role.TUTOR.getValue())
            launch(TutorHomeActivity.class);

        else if (m_user.getRole() == User.Role.LEARNER.getValue())
            launch(LearnerHomeActivity.class);
    }

    @Override
    protected void onViewLoaded()
    {
        runOnUiThread(this::fetchContacts);
    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    private void fetchContacts()
    {
        chatApiClient.getContacts().enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<ContactResponse>> call, Response<CommonResponse<ContactResponse>> response)
            {
                if (!verificationMw.IsAllowed(response))
                    return;

                try
                {
                    // Convert the parsed object back to JSON safely
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    bridgeCall_execJavascriptFunction("renderDetails", jsonResponse);

                } catch (Exception e) {
                    Log.e("RETROFIT_ERROR", "Error reading response body: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<CommonResponse<ContactResponse>> call, Throwable t)
            {
                Log.e("MINE", "Has error -> " + t.getMessage());
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onProfileLink()
    {
        Log.d("MINE", "Clicked here");

        if (m_user == null)
            return;

        if (m_user.getRole() == User.Role.TUTOR.getValue())
            launch(TutorProfileAccountActivity.class);

        else if (m_user.getRole() == User.Role.LEARNER.getValue())
            launch(LearnerProfileActivity.class);
    }

    @Override
    public void onContactItemSelected(String contactId)
    {
        if (contactId == null)
        {
            bridgeCall_alertWarn("Sorry, we're unable to load the contact.");
            return;
        }

        // Log.d("MINE", "The contact was selected: " + contactId);

        HashMap<String, String> param = new HashMap<>();
        param.put("contactHashId", contactId);

        launchWith(ChatActivity.class, param);
    }
}
