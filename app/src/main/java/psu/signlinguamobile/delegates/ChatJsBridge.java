package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class ChatJsBridge
{
    public interface ChatJsBridgeListener
    {
        String JS_BRIDGE_NAME = "ChatJsBridge";
        void onGoBack();
        void onCaptureMessage(String message);
        void onInitiateCall();
    }

    private final ChatJsBridge.ChatJsBridgeListener listener;

    public ChatJsBridge(ChatJsBridge.ChatJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onGoBack()
    {
        if (listener != null) {
            listener.onGoBack();
        }
    }

    @JavascriptInterface
    public void onCaptureMessage(String message)
    {
        if (listener != null) {
            listener.onCaptureMessage(message);
        }
    }

    @JavascriptInterface
    public void onInitiateCall()
    {
        if (listener != null)
            listener.onInitiateCall();
    }
}
