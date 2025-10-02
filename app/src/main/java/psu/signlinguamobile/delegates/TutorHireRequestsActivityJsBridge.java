package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorHireRequestsActivityJsBridge
{
    public interface TutorHireRequestsActivityJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorHireRequestsActivityJsBridge";
        void onFindLearners();
        void onGoBack();
        void onShowLearnerInfo(String learnerId);
        void onConfirmRequest(String learnerId);
        void onDeclineRequest(String learnerId);
    }

    private final TutorHireRequestsActivityJsBridge.TutorHireRequestsActivityJsBridgeListener listener;

    public TutorHireRequestsActivityJsBridge(TutorHireRequestsActivityJsBridge.TutorHireRequestsActivityJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onFindLearners()
    {
        if (listener != null) {
            listener.onFindLearners();
        }
    }

    @JavascriptInterface
    public void onGoBack()
    {
        if (listener != null) {
            listener.onGoBack();
        }
    }

    @JavascriptInterface
    public void onShowLearnerInfo(String learnerId)
    {
        if (listener != null) {
            listener.onShowLearnerInfo(learnerId);
        }
    }

    @JavascriptInterface
    public void onConfirmRequest(String learnerId)
    {
        if (listener != null) {
            listener.onConfirmRequest(learnerId);
        }
    }

    @JavascriptInterface
    public void onDeclineRequest(String learnerId)
    {
        if (listener != null) {
            listener.onDeclineRequest(learnerId);
        }
    }
}
