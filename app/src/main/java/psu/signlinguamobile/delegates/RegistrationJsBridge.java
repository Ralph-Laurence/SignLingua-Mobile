package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class RegistrationJsBridge
{
    public interface RegistrationBridgeListener

{
        String JS_BRIDGE_NAME = "RegistrationBridge";

        void onGoBack();
        void onRegister(String data);
        void onCancelRegistration();
    }

    private final RegistrationJsBridge.RegistrationBridgeListener listener;

    public RegistrationJsBridge(RegistrationJsBridge.RegistrationBridgeListener listener)
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
    public void onRegister(String data)
    {
        if (listener != null) {
            listener.onRegister(data);
        }
    }

    @JavascriptInterface
    public void onCancelRegistration()
    {
        if (listener != null) {
            listener.onCancelRegistration();
        }
    }
}