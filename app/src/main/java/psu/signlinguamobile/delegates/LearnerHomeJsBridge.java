package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class LearnerHomeJsBridge
{

    public interface LearnerHomeJsBridgeListener
    {
        void onLogout();
        void onNavFindTutors();
        //void onNavClassrooms();
        void onNavChat();
        void onNavMyTutors();
        void onNavMyProfile();
        void onNavScanner();
    }

    private final LearnerHomeJsBridgeListener listener;

    public LearnerHomeJsBridge(LearnerHomeJsBridgeListener listener)
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
    public void onNavFindTutors()
    {
        if (listener != null) {
            listener.onNavFindTutors();
        }
    }

//    @JavascriptInterface
//    public void onNavClassrooms()
//    {
//        if (listener != null)
//        {
//            listener.onNavClassrooms();
//        }
//    }

    @JavascriptInterface
    public void onNavChat()
    {
        if (listener != null) {
            listener.onNavChat();
        }
    }

    @JavascriptInterface
    public void onNavMyTutors()
    {
        if (listener != null)
        {
            listener.onNavMyTutors();
        }
    }

    @JavascriptInterface
    public void onNavMyProfile()
    {
        if (listener != null)
        {
            listener.onNavMyProfile();
        }
    }

    @JavascriptInterface
    public void onNavScanner()
    {
        if (listener != null) {
            listener.onNavScanner();
        }
    }
}
