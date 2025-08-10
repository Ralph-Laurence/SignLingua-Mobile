package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class LearnerClassroomsJsBridge
{
    public interface LearnerClassroomsJsBridgeListener
    {
        void onGoBack();
    }

    private final LearnerClassroomsJsBridge.LearnerClassroomsJsBridgeListener listener;

    public LearnerClassroomsJsBridge(LearnerClassroomsJsBridge.LearnerClassroomsJsBridgeListener listener)
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
