package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileAccountJsBridge
{
    public interface TutorProfileAccountJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfileAccountJsBridge";

        void onGoBack();
        void onCapturePhoto();
        void onSelectGalleryPhoto();
        void onSaveProfile(String username, String email, String bio);
        void onRemoveProfilePic();
    }

    private final TutorProfileAccountJsBridgeListener listener;

    public TutorProfileAccountJsBridge(TutorProfileAccountJsBridgeListener listener)
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
    public void onSaveProfile(String username, String email, String bio)
    {
        if (listener != null) {
            listener.onSaveProfile(username, email, bio);
        }
    }

    @JavascriptInterface
    public void onSelectGalleryPhoto()
    {
        if (listener != null) {
            listener.onSelectGalleryPhoto();
        }
    }

    @JavascriptInterface
    public void onCapturePhoto()
    {
        if (listener != null) {
            listener.onCapturePhoto();
        }
    }

    @JavascriptInterface
    public void onRemoveProfilePic()
    {
        if (listener != null) {
            listener.onRemoveProfilePic();
        }
    }
}
