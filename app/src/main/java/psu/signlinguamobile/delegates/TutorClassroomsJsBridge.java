package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorClassroomsJsBridge
{
    public interface TutorClassroomsJsBridgeListener
    {
        void onGoBack();
    }

    private final TutorClassroomsJsBridge.TutorClassroomsJsBridgeListener listener;

    public TutorClassroomsJsBridge(TutorClassroomsJsBridge.TutorClassroomsJsBridgeListener listener)
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
