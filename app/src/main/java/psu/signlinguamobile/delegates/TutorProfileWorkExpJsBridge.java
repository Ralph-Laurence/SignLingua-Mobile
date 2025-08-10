package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileWorkExpJsBridge
{
    public interface TutorProfileWorkExpJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfileWorkExpJsBridge";

        void onGoBack();
        void onEditWorkExp(String company, String role, int from, int to);
        void onRemoveWorkExp(String docId);
    }

    private final TutorProfileWorkExpJsBridgeListener listener;

    public TutorProfileWorkExpJsBridge(TutorProfileWorkExpJsBridgeListener listener)
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
    public void onEditWorkExp(String company, String role, int from, int to)
    {
        if (listener != null)
            listener.onEditWorkExp(company, role, from, to);
    }

    @JavascriptInterface
    public void onRemoveWorkExp(String docId)
    {
        if (listener != null)
            listener.onRemoveWorkExp(docId);
    }
}
