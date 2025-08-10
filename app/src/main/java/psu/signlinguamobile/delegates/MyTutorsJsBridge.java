package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;


//////////////////////////////////////////////////
// ============== Unused for now =================
// ...............................................
// My Tutors Activity will share the same function
// with Find Tutors Activity
// ...............................................
// ===============================================
//////////////////////////////////////////////////



public class MyTutorsJsBridge
{
    public interface MyTutorsJsBridgeListener
    {
        void onGoBack();
        void onViewTutor(String id);
        void onFindTutors(String search, int page, String disability);
    }

    private final MyTutorsJsBridge.MyTutorsJsBridgeListener listener;

    public MyTutorsJsBridge(MyTutorsJsBridge.MyTutorsJsBridgeListener listener)
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
    public void onViewTutor(String id)
    {
        if (listener != null)
            listener. onViewTutor(id);
    }

    @JavascriptInterface
    public void onFindTutors(String search, int page, String disability)
    {
        if (listener != null)
            listener. onFindTutors(search, page, disability);
    }
}
