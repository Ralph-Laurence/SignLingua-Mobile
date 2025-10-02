package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorHomeJsBridge
{

    public interface TutorHomeJsBridgeListener
    {
       void onLogout();
       void onNavFindLearners();
       void onNavScanner();
       void onNavMyLearners();
       // void onNavClassrooms();
       void onNavChat();
       void onNavMyProfile();
       void onNavHireRequests();
    }

    private final TutorHomeJsBridgeListener listener;

    public TutorHomeJsBridge(TutorHomeJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onLogout()
    {
        if (listener != null) {
            listener.onLogout();
        }
    }

    @JavascriptInterface
    public void onNavFindLearners()
    {
        if (listener != null) {
            listener.onNavFindLearners();
        }
    }

    @JavascriptInterface
    public void onNavMyLearners()
    {
        if (listener != null) {
            listener.onNavMyLearners();
        }
    }

    @JavascriptInterface
    public void onNavScanner()
    {
        if (listener != null) {
            listener.onNavScanner();
        }
    }

    /*@JavascriptInterface
    public void onNavClassrooms()
    {
        if (listener != null) {
            listener.onNavClassrooms();
        }
    }*/

    @JavascriptInterface
    public void onNavChat()
    {
        if (listener != null) {
            listener.onNavChat();
        }
    }

    @JavascriptInterface
    public void onNavMyProfile()
    {
        if (listener != null) {
            listener.onNavMyProfile();
        }
    }

    @JavascriptInterface
    public void onNavHireRequests()
    {
        if (listener != null) {
            listener.onNavHireRequests();
        }
    }
}
