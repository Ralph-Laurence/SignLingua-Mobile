package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class LearnerProfileJsBridge
{

    public interface LearnerProfileJsBridgeListener
    {
        void onGoBack();
    }

    private final LearnerProfileJsBridgeListener listener;

    public LearnerProfileJsBridge(LearnerProfileJsBridgeListener listener)
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
}
