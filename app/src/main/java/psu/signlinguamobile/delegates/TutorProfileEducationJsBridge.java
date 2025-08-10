package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileEducationJsBridge
{
    public interface TutorProfileEducationJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfileEducationJsBridge";

        void onGoBack();
        void onEditEducation(String institution, String degree, int from, int to);
        void onRemoveEducation(String docId);
    }

    private final TutorProfileEducationJsBridgeListener listener;

    public TutorProfileEducationJsBridge(TutorProfileEducationJsBridgeListener listener)
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
    public void onEditEducation(String institution, String degree, int from, int to)
    {
        if (listener != null)
            listener.onEditEducation(institution, degree, from, to);
    }

    @JavascriptInterface
    public void onRemoveEducation(String docId)
    {
        if (listener != null)
            listener.onRemoveEducation(docId);
    }
}