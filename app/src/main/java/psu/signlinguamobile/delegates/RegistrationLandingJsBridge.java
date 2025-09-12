package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class RegistrationLandingJsBridge
{
    public interface RegistrationLandingJsBridgeListener
    {
        String JS_BRIDGE_NAME = "RegistrationLandingBridge";

        void onGoBack();
        void onLoginPage();
        void onRegisterTutorPage();
        void onRegisterLearnerPage();
    }

    private final RegistrationLandingJsBridge.RegistrationLandingJsBridgeListener listener;

    public RegistrationLandingJsBridge(RegistrationLandingJsBridge.RegistrationLandingJsBridgeListener listener)
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
    public void onLoginPage()
    {
        if (listener != null) {
            listener.onLoginPage();
        }
    }

    @JavascriptInterface
    public void onRegisterTutorPage()
    {
        if (listener != null) {
            listener.onRegisterTutorPage();
        }
    }

    @JavascriptInterface
    public void onRegisterLearnerPage()
    {
        if (listener != null) {
            listener.onRegisterLearnerPage();
        }
    }
}
