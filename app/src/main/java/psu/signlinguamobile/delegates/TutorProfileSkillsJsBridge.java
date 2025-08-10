package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileSkillsJsBridge
{
    public interface TutorProfileSkillsJsBridgeListener
    {
        String JS_BRIDGE_NAME = "TutorProfileSkillsJsBridge";

        void onGoBack();
        void onEditSkills(int disability, String[] skills);
    }

    private final TutorProfileSkillsJsBridgeListener listener;

    public TutorProfileSkillsJsBridge(TutorProfileSkillsJsBridgeListener listener)
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
    public void onEditSkills(int disability, String[] skills)
    {
        if (listener != null) {
            listener.onEditSkills(disability, skills);
        }
    }
}