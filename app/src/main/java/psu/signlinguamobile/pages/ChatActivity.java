package psu.signlinguamobile.pages;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;

import com.google.gson.Gson;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.HashMap;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.ChatApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.ChatJsBridge;
import psu.signlinguamobile.models.ChatConvResponse;
import psu.signlinguamobile.models.ChatMessagePayload;
import psu.signlinguamobile.models.SendChatMessageData;
import psu.signlinguamobile.utilities.HubUtils;
import psu.signlinguamobile.utilities.UXMessages;
import psu.signlinguamobile.utilities.VerificationMiddlewareChecker;
import psu.signlinguamobile.videocall.VideoCallActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity
        extends BaseWebViewActivity
        implements ChatJsBridge.ChatJsBridgeListener
{
    private String m_authToken;
    private ChatApiService chatApiClient;
    private VerificationMiddlewareChecker verificationMw;

    private String m_contactId;
    private String m_currentUserId;
    private String m_roomId;
    private Runnable onChatsLoaded;

    private HubConnection m_chatHubConn;
    private HubConnection m_notifHubConn;
    private Disposable m_disposableChatHubStart;
    private Disposable m_disposableNotifHubConn;

    //------------------------------------//
    //  S I G N A L R  R E C O N N E C T  //
    //------------------------------------//
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private int reconnectAttempts = 0;
    private boolean wasConnected = false;
    private boolean isReconnecting = false;
    //------------------------------------//
    //------------------------------------//

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
        // (May not fully work). Use Rasul's solution instead.
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
        m_contactId    = getIntent().getStringExtra("contactHashId");

        registerJsBridge(new ChatJsBridge(this), JS_BRIDGE_NAME);
        renderView("chat");

        // Runnable
        onChatsLoaded = () -> {

            if (m_authToken == null)
            {
                runOnUiThread(() -> bridgeCall_execJavascriptFunction("notifyCriticalError"));
                return;
            }

            runOnUiThread(() -> initializeHubs(m_authToken));
        };
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
        if (m_disposableChatHubStart != null && !m_disposableChatHubStart.isDisposed()) {
            m_disposableChatHubStart.dispose();
        }

        if (m_disposableNotifHubConn != null && !m_disposableNotifHubConn.isDisposed()){
            m_disposableNotifHubConn.dispose();
        }
        unregisterJsBridge(JS_BRIDGE_NAME);
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
                    if (!response.isSuccessful() || response.body() == null)
                    {
                        bridgeCall_execJavascriptFunction("notifyCriticalError");
                        return;
                    }

                    // Convert the parsed object back to JSON safely
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    ChatConvResponse obj = response.body().getContent();
                    m_authToken = obj.getAuthToken();
                    m_currentUserId = obj.getSenderId();
                    /*
                    * if (Looper.myLooper() == Looper.getMainLooper()) {
                        onChatsLoaded.run();
                    } else {
                        runOnUiThread(onChatsLoaded);
                    }
                    */
                    onChatsLoaded.run(); // Safe: already on main thread

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

    @Override public void onCaptureMessage(String message)
    {
        // Log.d("MINE", "Capture message -> " + message);
        SendChatMessageData payload = new SendChatMessageData();

        payload.setMessage(message);
        payload.setReceiver(m_contactId);

        sendMessage(payload);
    }

    @SuppressLint("CheckResult")
    private void initializeHubs(String jwtToken)
    {
        m_chatHubConn = HubConnectionBuilder.create(HubUtils.getChatHubUrl())
                .withAccessTokenProvider(Single.defer(() -> Single.just(jwtToken)))
                .build();

        // Listen for room join confirmation
        m_chatHubConn.on("RoomJoined", (roomId) -> {
            m_roomId = roomId;
            Log.d("MINE", "Joined room: " + roomId);

            m_notifHubConn.invoke("ReadyToReceiveCall", m_currentUserId)
                    .subscribe(() -> Log.d("MINE", "Ready to receive call"),
                            error -> Log.e("MINE", "Error signaling readiness: " + error.getMessage()));

            runOnUiThread(() -> bridgeCall_execJavascriptFunction("enableControls"));
//            m_hubConn.invoke("SendMessageToRoom", roomId, "IT WORKS!").subscribe();
        }, String.class);

        m_chatHubConn.on("ReceiveMessage", (String senderId, String message) -> {

            runOnUiThread(() -> {
                ChatMessagePayload payload = new ChatMessagePayload();

                if (senderId.equals(m_contactId))
                    payload.setFromSender("contact");
                else
                    payload.setFromSender("me");

                payload.setMessage(message);

                Gson gson = new Gson();
                String jsonResponse = gson.toJson(payload);

                bridgeCall_execJavascriptFunction("enqueueMessage", jsonResponse);
            });

        }, String.class, String.class);

        // Handle full disconnect
        m_chatHubConn.onClosed(error -> {
            Log.e("MINE", "Connection closed: " + (error != null ? error.getMessage() : "No error"));
            //attemptReconnectWithBackoff();

            if (wasConnected)
            {
                Log.d("MINE", "Disconnected — attempting reconnect...");
                attemptReconnectWithBackoff();
            }
            else
            {
                Log.d("MINE", "Initial connection failed.");
            }
        });

        m_notifHubConn = HubConnectionBuilder.create(HubUtils.getNotifHubUrl())
                .withAccessTokenProvider(Single.defer(() -> Single.just(jwtToken)))
                .build();

        m_notifHubConn.on("IncomingCall",  (String senderId, String roomId) -> {
            runOnUiThread(() -> {
                Log.d("MINE", "Ringing");
                bridgeCall_execJavascriptFunction("showIncomingCallRinger");
            });
        }, String.class, String.class);

        // Initial connection
        startHubConnection();
    }

    private void startHubConnection()
    {
        if (m_chatHubConn.getConnectionState() == HubConnectionState.DISCONNECTED)
        {
            m_disposableChatHubStart = m_chatHubConn.start()
                    .doOnComplete(() -> joinChatRoom(m_chatHubConn))
                    .subscribe(() -> {
                        Log.d("MINE", "Connected!");
                        wasConnected = true;
                        reconnectAttempts = 0; // Reset on success
                    }, error -> {
                        wasConnected = false;
                        Log.e("MINE", "Initial connection failed: " + error.getMessage());
                        runOnUiThread(() -> {
                            bridgeCall_function("notifyCriticalError", "Sorry, we can't reach the messaging service right now.");
                        });
                    });
        }

        if (m_notifHubConn.getConnectionState() == HubConnectionState.DISCONNECTED)
        {
            m_disposableNotifHubConn = m_notifHubConn.start()
                    .subscribe(() -> {
                        Log.d("MINE", "Connected to notification hub!");
                        wasConnected = true;
                    }, error -> {
                        wasConnected = false;
                        Log.e("MINE", "Notif hub connection failed: " + error.getMessage());
                        runOnUiThread(() -> {
                            bridgeCall_function("notifyCriticalError", "Sorry, we can't reach the messaging service right now.");
                        });
                    });
        }
    }

    @SuppressLint("CheckResult")
    private void joinChatRoom(HubConnection hubConnection)
    {
        hubConnection.invoke("CreateOrJoinRoom", m_contactId)
                .subscribe(() -> Log.d("MINE", "Room join invoked"),
                        error -> {
                            Log.e("MINE", "Room join failed: " + error.getMessage());
                            bridgeCall_function("notifyCriticalError", "Sorry, the messaging service isn't available right now.");
                        });
    }

    private void attemptReconnectWithBackoff()
    {
        if (isReconnecting || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS)
        {
            Log.e("MINE", "Reconnect blocked or max attempts reached.");
            bridgeCall_function("notifyCriticalError", "Messaging service is temporarily unavailable.");
            return;
        }

        isReconnecting = true;
        reconnectAttempts++;
        long delaySeconds = reconnectAttempts * 2L;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d("MINE", "Attempting reconnect #" + reconnectAttempts);
            startHubConnection();
            isReconnecting = false;
        }, delaySeconds * 1000);
    }

    private void sendMessage(SendChatMessageData payload)
    {
        chatApiClient.sendMessage(payload).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<Void>> call, Response<CommonResponse<Void>> response)
            {
                if (!verificationMw.IsAllowed(response))
                    return;

                try
                {
                    if (!response.isSuccessful() || response.body() == null)
                    {
                        bridgeCall_execJavascriptFunction("notifyCriticalError");
                        return;
                    }

                    // Convert the parsed object back to JSON safely
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    Log.d("MINE", jsonResponse);
                    m_chatHubConn.invoke("SendMessageToRoom", m_roomId, response.body().getMessage()).subscribe();
                    // bridgeCall_execJavascriptFunction("renderDetails", jsonResponse);

                } catch (Exception e) {
                    Log.e("RETROFIT_ERROR", "Error reading response body: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                Log.e("MINE", "Has error -> " + t.getMessage());
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    @Override
    public void onInitiateCall()
    {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("ROOM_ID", m_roomId);
        payload.put("JWT", m_authToken);
        payload.put("CONTACT_ID", m_contactId);

        Log.d("MINE", "WITH ROOM ID -> " + m_roomId);
        launchWith(VideoCallActivity.class, payload);
    }
}
