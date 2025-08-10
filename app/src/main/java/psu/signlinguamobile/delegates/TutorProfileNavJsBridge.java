package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class TutorProfileNavJsBridge
{
    public interface TutorProfileNavJsBridgeListener
    {
        void onNavUserAccount();
        void onNavCertifications();
        void onNavEducationalBackground();
        void onNavGeneralInformation();
        void onNavPasswordSecurity();
        void onNavSkillsAccessibility();
        void onNavWorkExperience();
    }

    private final TutorProfileNavJsBridge.TutorProfileNavJsBridgeListener listener;

    public TutorProfileNavJsBridge(TutorProfileNavJsBridge.TutorProfileNavJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onNavUserAccount()
    {
        if (listener != null) {
            listener.onNavUserAccount();
        }
    }

    @JavascriptInterface
    public void onNavCertifications()
    {
        if (listener != null) {
            listener.onNavCertifications();
        }
    }

    @JavascriptInterface
    public void onNavEducationalBackground()
    {
        if (listener != null) {
            listener.onNavEducationalBackground();
        }
    }

    @JavascriptInterface
    public void onNavGeneralInformation()
    {
        if (listener != null) {
            listener.onNavGeneralInformation();
        }
    }

    @JavascriptInterface
    public void onNavPasswordSecurity()
    {
        if (listener != null) {
            listener.onNavPasswordSecurity();
        }
    }

    @JavascriptInterface
    public void onNavSkillsAccessibility()
    {
        if (listener != null) {
            listener.onNavSkillsAccessibility();
        }
    }

    @JavascriptInterface
    public void onNavWorkExperience()
    {
        if (listener != null) {
            listener.onNavWorkExperience();
        }
    }
}
