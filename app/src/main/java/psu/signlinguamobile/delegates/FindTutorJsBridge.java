package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class FindTutorJsBridge
{
    public interface FindTutorJsBridgeListener
    {
        void onGoBack();
        void onStalkProfile(String id);
        void onFindTutors(String search, int page, String disability);
    }

    private final FindTutorJsBridge.FindTutorJsBridgeListener listener;

    public FindTutorJsBridge(FindTutorJsBridge.FindTutorJsBridgeListener listener)
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
    public void onStalkProfile(String id)
    {
        if (listener != null)
            listener.onStalkProfile(id);
    }

    @JavascriptInterface
    public void onFindTutors(String search, int page, String disability)
    {
        if (listener != null)
            listener. onFindTutors(search, page, disability);
    }
}
