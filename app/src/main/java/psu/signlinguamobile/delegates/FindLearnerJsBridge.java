package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class FindLearnerJsBridge
{
    public interface FindLearnerJsBridgeListener
    {
        void onGoBack();
        void onFindLearners(String search, int page, String disability);
        void onStalkProfile(String id);
    }

    private final FindLearnerJsBridge.FindLearnerJsBridgeListener listener;

    public FindLearnerJsBridge(FindLearnerJsBridge.FindLearnerJsBridgeListener listener)
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
    public void onFindLearners(String search, int page, String disability)
    {
        if (listener != null)
            listener. onFindLearners(search, page, disability);
    }

    @JavascriptInterface
    public void onStalkProfile(String id)
    {
        if (listener != null)
            listener.onStalkProfile(id);
    }
}
