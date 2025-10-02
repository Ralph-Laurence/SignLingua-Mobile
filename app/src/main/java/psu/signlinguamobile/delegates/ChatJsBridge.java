package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class ChatJsBridge
{
    public interface ChatJsBridgeListener
    {
        String JS_BRIDGE_NAME = "ChatJsBridge";
        void onGoBack();
//        void onProfileLink();
//        void onContactItemSelected(String contactId);
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
//
//    @JavascriptInterface
//    public void onContactItemSelected(String contactId)
//    {
//        if (listener != null) {
//            listener.onContactItemSelected(contactId);
//        }
//    }
}
