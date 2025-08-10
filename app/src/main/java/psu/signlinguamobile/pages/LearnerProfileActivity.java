package psu.signlinguamobile.pages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.gson.Gson;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiresponse.LearnerProfileDetailsResponse;
import psu.signlinguamobile.api.apiservice.AuthApiService;
import psu.signlinguamobile.api.apiservice.LearnerManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.LearnerProfileJsBridge;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnerProfileActivity extends AppCompatActivity implements LearnerProfileJsBridge.LearnerProfileJsBridgeListener
{
    private WebView m_webView;
    private AuthApiService apiService;
    private LearnerManagementApiService learnerMgtApiService;
    private String lastLoadedUrl = "";
    private final String JS_BRIDGE_NAME = "LearnerProfileJsBridge";

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                onGoBack();
            }
        });

        m_webView = findViewById(R.id.webView);
        WebSettings webSettings = m_webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        m_webView.setWebViewClient(new WebViewClient());

        m_webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                // Prevent duplicate calls:
                // Prevents extra API calls by tracking the last loaded URL.
                // Ensures onPageFinished() only runs once per unique page load.
                // Works smoothly with redirects or dynamic content updates in WebView.
                if (!url.equals(lastLoadedUrl))
                {
                    lastLoadedUrl = url;
                    Log.d("WEBVIEW", "Page fully loaded: " + url);

                    // Load initial learners once
                    runOnUiThread(() -> fetchProfileDetails());
                }
            }
        });

        // Add the login bridge defined in a separate file.
        m_webView.addJavascriptInterface(new LearnerProfileJsBridge(this), JS_BRIDGE_NAME);

        m_webView.loadUrl("file:///android_asset/pages/learner_myprofile.html");

        // Initialize Retrofit API Service
        learnerMgtApiService = ApiClient.getClient(this, true).create(LearnerManagementApiService.class);
    }

    @Override
    public void onGoBack()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(LearnerProfileActivity.this, LearnerHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void fetchProfileDetails()
    {
        learnerMgtApiService.fetchLearnerProfileDetails().enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<LearnerProfileDetailsResponse> call, Response<LearnerProfileDetailsResponse> response)
            {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("RETROFIT_ERROR", "API failed with code: " + response.code());
                    return;
                }

                try
                {
                    // Convert the parsed object back to JSON safely
                    Gson gson = new Gson();
                    String jsonResponse = escapeJsonForWebView(gson.toJson(response.body()));

                    Log.d("MINE", "Profile -> " + jsonResponse);
                    // Pass JSON string safely to WebView
                    m_webView.evaluateJavascript("javascript:renderDetails(`" + jsonResponse + "`);", null);

                } catch (Exception e) {
                    Log.e("RETROFIT_ERROR", "Error reading response body: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<LearnerProfileDetailsResponse> call, Throwable t)
            {
                Log.e("API_ERROR", "Failed to fetch learners: " + t.getMessage());
            }
        });
    }

    private String escapeJsonForWebView(String json) {
        return json.replace("\\", "\\\\").replace("\"", "\\\"");
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