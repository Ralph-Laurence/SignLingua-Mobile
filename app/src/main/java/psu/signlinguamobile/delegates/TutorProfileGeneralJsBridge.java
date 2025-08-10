package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileGeneralJsBridge
{
    public interface TutorProfileGeneralJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfileGeneralJsBridge";

        void onGoBack();
        void onEditGeneral(String firstname, String lastname, String contact, String address, String about);
    }

    private final TutorProfileGeneralJsBridgeListener listener;

    public TutorProfileGeneralJsBridge(TutorProfileGeneralJsBridgeListener listener)
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
    public void onEditGeneral(String firstname, String lastname, String contact, String address, String about)
    {
        if (listener != null) {
            listener.onEditGeneral(firstname, lastname, contact, address, about);
        }
    }
}
