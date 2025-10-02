package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class LearnerDetailsJsBridge
{
    public interface LearnerDetailsJsBridgeListener
    {
        String JS_BRIDGE_NAME = "LearnerDetailsJsBridge";
        void onGoBack();
        void onConfirmRequest();
        void onDeclineRequest();
        void onDropLearner();
        void onAddLearner();
        void onCancelRequest();
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

    @JavascriptInterface
    public void onConfirmRequest()
    {
        if (listener != null)
            listener.onConfirmRequest();
    }

    @JavascriptInterface
    public void onDeclineRequest()
    {
        if (listener != null)
            listener.onDeclineRequest();
    }

    @JavascriptInterface
    public void onDropLearner()
    {
        if (listener != null)
            listener.onDropLearner();
    }

    @JavascriptInterface
    public void onAddLearner()
    {
        if (listener != null)
            listener.onAddLearner();
    }

    @JavascriptInterface
    public void onCancelRequest()
    {
        if (listener != null)
            listener.onCancelRequest();
    }
}
