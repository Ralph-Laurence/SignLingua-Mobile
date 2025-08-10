package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorDetailsJsBridge
{
    public interface TutorDetailsJsBridgeListener
    {
        void onGoBack();
    }

    private final TutorDetailsJsBridge.TutorDetailsJsBridgeListener listener;

    public TutorDetailsJsBridge(TutorDetailsJsBridge.TutorDetailsJsBridgeListener listener)
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
