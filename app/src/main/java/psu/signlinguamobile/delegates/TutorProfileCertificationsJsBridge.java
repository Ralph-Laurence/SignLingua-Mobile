package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileCertificationsJsBridge
{
    public interface TutorProfileCertificationsJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfileCertsJsBridge";

        void onGoBack();
        void onEditCertification(String from, String certification, String description);
        void onRemoveCertification(String docId);
    }

    private final TutorProfileCertificationsJsBridgeListener listener;

    public TutorProfileCertificationsJsBridge(TutorProfileCertificationsJsBridgeListener listener)
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
    public void onEditCertification(String from, String certification, String description)
    {
        if (listener != null)
            listener.onEditCertification(from, certification, description);
    }

    @JavascriptInterface
    public void onRemoveCertification(String docId)
    {
        if (listener != null)
            listener.onRemoveCertification(docId);
    }
}
