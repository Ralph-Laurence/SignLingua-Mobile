package psu.signlinguamobile.pages;

import android.util.Log;
import android.view.KeyEvent;

import com.google.gson.Gson;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.ChatApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.ChatJsBridge;
import psu.signlinguamobile.delegates.ContactsJsBridge;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.models.ChatConvResponse;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.UXMessages;
import psu.signlinguamobile.utilities.VerificationMiddlewareChecker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity
        extends BaseWebViewActivity
        implements ChatJsBridge.ChatJsBridgeListener
{
    private ChatApiService chatApiClient;
    private VerificationMiddlewareChecker verificationMw;
    // private User m_user;
    private String m_contactId;

    @Override
    protected void onAwake()
    {
        super.onAwake();

        setLayoutResource(R.layout.activity_chat_web_layout);
        setWebviewResource(R.id.emojiFreeWebView);
        useDarkStatusIcons(true);
    }

    @Override
    protected void onInitialize()
    {
        // Disable system emojis, because we will use our custom emoji pickers
        getWebView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                char c = (char) event.getUnicodeChar();
                if (isEmoji(c)) {
                    return true; // Block emoji key
                }
            }
            return false;
        });
        verificationMw = new VerificationMiddlewareChecker(this);
        chatApiClient  = ApiClient.getClient(this, true).create(ChatApiService.class);
        //m_user         = SessionManager.getInstance().getCurrentUser();
        m_contactId    = getIntent().getStringExtra("contactHashId");

        registerJsBridge(new ChatJsBridge(this), JS_BRIDGE_NAME);
        renderView("chat");
    }

    @Override
    protected void onBackKey()
    {
        onGoBack();
    }

    @Override
    protected void onViewLoaded()
    {
        runOnUiThread(() -> fetchConvo(m_contactId));
    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    private void fetchConvo(String contactId)
    {
        chatApiClient.loadConvo(contactId).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<ChatConvResponse>> call, Response<CommonResponse<ChatConvResponse>> response)
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
            public void onFailure(Call<CommonResponse<ChatConvResponse>> call, Throwable t)
            {
                Log.e("MINE", "Has error -> " + t.getMessage());
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    private boolean isEmoji(char c) {
        int type = Character.getType(c);
        return type == Character.SURROGATE || type == Character.OTHER_SYMBOL;
    }

    @Override
    public void onGoBack()
    {
        launch(ContactsActivity.class);
    }
}
