package psu.signlinguamobile.pages;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiservice.AuthApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.TutorHomeJsBridge;
import psu.signlinguamobile.utilities.LogoutUtility;

public class TutorHomeActivity extends AppCompatActivity implements TutorHomeJsBridge.TutorHomeJsBridgeListener
{
    private WebView m_webView;
    private AuthApiService apiService;
    private final String JS_BRIDGE_NAME = "TutorHomeJsBridge";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_common_web_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

//        Window window = getWindow();
//        window.setStatusBarColor(ContextCompat.getColor(this, R.color.tutor_status_bar_darker));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        WindowInsetsControllerCompat insetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false); // or false for light icons
        }

        m_webView = findViewById(R.id.webView);
        WebSettings webSettings = m_webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        m_webView.setWebViewClient(new WebViewClient());

        // Add the login bridge defined in a separate file.
        m_webView.addJavascriptInterface(new TutorHomeJsBridge(this), JS_BRIDGE_NAME);

        m_webView.loadUrl("file:///android_asset/pages/tutor_home.html");

    }

    // This method is called from the bridge (make sure to switch to UI thread if necessary)
    @Override
    public void onLogout()
    {
        apiService = ApiClient.getClient(this).create(AuthApiService.class);
        runOnUiThread(() -> LogoutUtility.logout(TutorHomeActivity.this, apiService));
    }

    @Override
    public void onNavFindLearners()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(TutorHomeActivity.this, FindLearnersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onNavMyLearners()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(TutorHomeActivity.this, MyLearnersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onNavScanner()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(TutorHomeActivity.this, ScannerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("launchedFrom", "tutorHome");
            startActivity(intent);
        });
    }

    @Override
    public void onNavClassrooms()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(TutorHomeActivity.this, TutorClassroomsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onNavMyProfile()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(TutorHomeActivity.this, TutorProfileAccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (m_webView != null)
        {
            m_webView.removeJavascriptInterface(JS_BRIDGE_NAME); // Prevents leaks
        }
    }
}