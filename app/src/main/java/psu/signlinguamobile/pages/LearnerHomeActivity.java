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
import psu.signlinguamobile.delegates.LearnerHomeJsBridge;
import psu.signlinguamobile.utilities.LogoutUtility;

public class LearnerHomeActivity extends AppCompatActivity implements LearnerHomeJsBridge.LearnerHomeJsBridgeListener
{
    private WebView m_webView;
    private AuthApiService apiService;
    private final String JS_BRIDGE_NAME = "LearnerHomeJsBridge";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_common_web_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        m_webView.addJavascriptInterface(new LearnerHomeJsBridge(this), JS_BRIDGE_NAME);

        m_webView.loadUrl("file:///android_asset/pages/learner_home.html");
    }

    // This method is called from the bridge (make sure to switch to UI thread if necessary)
    @Override
    public void onLogout()
    {
        apiService = ApiClient.getClient(this).create(AuthApiService.class);
        runOnUiThread(() -> LogoutUtility.logout(LearnerHomeActivity.this, apiService));
    }

    @Override
    public void onNavFindTutors()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(LearnerHomeActivity.this, FindTutorsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onNavMyTutors()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(LearnerHomeActivity.this, MyTutorsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

//    @Override
//    public void onNavClassrooms()
//    {
//        runOnUiThread(() -> {
//            Intent intent = new Intent(LearnerHomeActivity.this, LearnerClassroomsActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        });
//    }

    @Override
    public void onNavChat()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(LearnerHomeActivity.this, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onNavMyProfile()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(LearnerHomeActivity.this, LearnerProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onNavScanner()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(LearnerHomeActivity.this, ScannerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("launchedFrom", "learnerHome");
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