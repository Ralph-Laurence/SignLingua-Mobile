package psu.signlinguamobile.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;

import psu.signlinguamobile.delegates.TutorProfileNavJsBridge;
import psu.signlinguamobile.pages.TutorProfileAccountActivity;
import psu.signlinguamobile.pages.TutorProfileCertificationsActivity;
import psu.signlinguamobile.pages.TutorProfileEducationActivity;
import psu.signlinguamobile.pages.TutorProfileGeneralActivity;
import psu.signlinguamobile.pages.TutorProfilePasswordsActivity;
import psu.signlinguamobile.pages.TutorProfileSkillsActivity;
import psu.signlinguamobile.pages.TutorProfileWorkExpActivity;

public class TutorProfileNavController implements TutorProfileNavJsBridge.TutorProfileNavJsBridgeListener
{
    private final WebView m_webView;
    private Context m_context;
    private final String JS_BRIDGE_NAME = "TutorProfileNavJsBridge";

    public TutorProfileNavController(Context context, WebView webView)
    {
        this.m_webView = webView;
        this.m_context = context;
        bindJsBridge();
    }

    private void bindJsBridge()
    {
        m_webView.addJavascriptInterface(new TutorProfileNavJsBridge(this), JS_BRIDGE_NAME);
    }

    @Override
    public void onNavUserAccount()
    {
        launchActivity(m_context, TutorProfileAccountActivity.class);
    }

    @Override
    public void onNavCertifications()
    {
        launchActivity(m_context, TutorProfileCertificationsActivity.class);
    }

    @Override
    public void onNavEducationalBackground()
    {
        launchActivity(m_context, TutorProfileEducationActivity.class);
    }

    @Override
    public void onNavGeneralInformation()
    {
        launchActivity(m_context, TutorProfileGeneralActivity.class);
    }

    @Override
    public void onNavPasswordSecurity()
    {
        launchActivity(m_context, TutorProfilePasswordsActivity.class);
    }

    @Override
    public void onNavSkillsAccessibility()
    {
        launchActivity(m_context, TutorProfileSkillsActivity.class);
    }

    @Override
    public void onNavWorkExperience()
    {
        launchActivity(m_context, TutorProfileWorkExpActivity.class);
    }

    public void removeBridge()
    {
        if (m_webView != null)
            m_webView.removeJavascriptInterface(JS_BRIDGE_NAME); // Prevents leaks
    }

    private <T> void launchActivity(Context context, Class<T> cls)
    {
        Intent intent = new Intent(m_context, cls);

        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
        m_context.startActivity(intent);
    }
}
