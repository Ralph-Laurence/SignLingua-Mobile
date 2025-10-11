package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class EndCallJsBridge
{
    public interface EndCallJsBridgeListener
    {
        String JS_BRIDGE_NAME = "EndCallJsBridge";
        void onCallAgain();
        void onClose();
    }

    private final EndCallJsBridge.EndCallJsBridgeListener listener;

    public EndCallJsBridge(EndCallJsBridge.EndCallJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onCallAgain()
    {
        if (listener != null)
            listener.onCallAgain();
    }

    @JavascriptInterface
    public void onClose()
    {
        if (listener != null)
            listener.onClose();
    }
}
