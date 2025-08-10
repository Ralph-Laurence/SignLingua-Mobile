package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class LearnerDetailsJsBridge
{
    public interface LearnerDetailsJsBridgeListener
    {
        void onGoBack();
    }

    private final LearnerDetailsJsBridge.LearnerDetailsJsBridgeListener listener;

    public LearnerDetailsJsBridge(LearnerDetailsJsBridge.LearnerDetailsJsBridgeListener listener)
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
