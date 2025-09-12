package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class GlobalCrashHandlerJsBridge
{
    public interface GlobalCrashHandlerJsBridgeListener
    {
        String JS_BRIDGE_NAME = "GlobalCrashHandlerBridge";
        void onTerminate();
    }

    private final GlobalCrashHandlerJsBridge.GlobalCrashHandlerJsBridgeListener listener;

    public GlobalCrashHandlerJsBridge(GlobalCrashHandlerJsBridge.GlobalCrashHandlerJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onTerminate()
    {
        if (listener != null) {
            listener.onTerminate();
        }
    }
}
