package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfilePasswordsJsBridge
{
    public interface TutorProfilePasswordsJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfilePasswordsJsBridge";

        void onGoBack();
        void onSavePassword(String oldPassword, String newPassword, String confirmPassword);
    }

    private final TutorProfilePasswordsJsBridgeListener listener;

    public TutorProfilePasswordsJsBridge(TutorProfilePasswordsJsBridgeListener listener)
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
    public void onSavePassword(String oldPassword, String newPassword, String confirmPassword)
    {
        if (listener != null) {
            listener.onSavePassword(oldPassword, newPassword, confirmPassword);
        }
    }
}
