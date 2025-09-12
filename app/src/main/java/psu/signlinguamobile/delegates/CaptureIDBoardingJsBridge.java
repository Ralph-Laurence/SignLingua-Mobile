package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class CaptureIDBoardingJsBridge
{
    public interface CaptureIDBoardingJsBridgeListener
    {
        String JS_BRIDGE_NAME = "CaptureIDBoardingBridge";

        void onCancelRegistration();
        void onProceedRegistration();
        void onTerminateAppOnCriticalError();
    }

    private final CaptureIDBoardingJsBridge.CaptureIDBoardingJsBridgeListener listener;

    public CaptureIDBoardingJsBridge(CaptureIDBoardingJsBridge.CaptureIDBoardingJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onCancelRegistration()
    {
        if (listener != null) {
            listener.onCancelRegistration();
        }
    }

    @JavascriptInterface
    public void onProceedRegistration()
    {
        if (listener != null) {
            listener.onProceedRegistration();
        }
    }

    @JavascriptInterface
    public void onTerminateAppOnCriticalError()
    {
        if (listener != null)
            listener.onTerminateAppOnCriticalError();
    }
}
