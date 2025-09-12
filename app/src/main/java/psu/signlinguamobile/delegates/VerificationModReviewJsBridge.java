package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class VerificationModReviewJsBridge
{
    public interface VerificationModReviewBridgeListener
    {
        String JS_BRIDGE_NAME = "VerificationModReviewBridge";

        void onGoHome();
        void onTerminate();
    }

    private final VerificationModReviewJsBridge.VerificationModReviewBridgeListener listener;

    public VerificationModReviewJsBridge(VerificationModReviewJsBridge.VerificationModReviewBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onGoHome()
    {
        if (listener != null) {
            listener.onGoHome();
        }
    }

    @JavascriptInterface
    public void onTerminate()
    {
        if (listener != null) {
            listener.onTerminate();
        }
    }
}
