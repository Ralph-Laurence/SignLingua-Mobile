package psu.signlinguamobile.pages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
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
import psu.signlinguamobile.api.apiresponse.TutorDetailsResponse;
import psu.signlinguamobile.api.apiservice.TutorManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.TutorDetailsJsBridge;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorDetailsActivity extends AppCompatActivity implements TutorDetailsJsBridge.TutorDetailsJsBridgeListener
{
    private WebView m_webView;
    private TutorManagementApiService tutorMgtApiService;
    private String lastLoadedUrl = "";
    private final String JS_BRIDGE_NAME = "TutorDetailsJsBridge";

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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        m_webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebView", consoleMessage.message());
                return true;
            }
        });

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

                    // Load initial tutors once
                    runOnUiThread(() -> fetchTutorDetails());
                }
            }
        });

        // Add the js bridge defined in a separate file.
        m_webView.addJavascriptInterface(new TutorDetailsJsBridge(this), JS_BRIDGE_NAME);

        m_webView.loadUrl("file:///android_asset/pages/tutor_details.html");

        // Initialize Retrofit API Service
        tutorMgtApiService = ApiClient.getClient(this, true).create(TutorManagementApiService.class);
    }

    private void fetchTutorDetails()
    {
        Intent intent = getIntent();
        String tutorId = intent.getStringExtra("tutorId");

        tutorMgtApiService.showTutor(tutorId).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<TutorDetailsResponse> call, Response<TutorDetailsResponse> response)
            {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("RETROFIT_ERROR", "API failed with code: " + response.code());
                    return;
                }

                try {
                    // Convert the parsed object back to JSON safely
                    Gson gson = new Gson();
                    String jsonResponse = escapeJsonForWebView(gson.toJson(response.body()));

                    Log.d("MINE", jsonResponse);
                    // Pass JSON string safely to WebView
                    m_webView.evaluateJavascript("javascript:renderDetails(`" + jsonResponse + "`);", null);

                } catch (Exception e) {
                    Log.e("RETROFIT_ERROR", "Error reading response body: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<TutorDetailsResponse> call, Throwable t)
            {
                Log.e("API_ERROR", "Failed to fetch tutors: " + t.getMessage());
            }
        });
    }

    private String escapeJsonForWebView(String json) {
        return json.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void onGoBack()
    {
        runOnUiThread(() -> {
            Intent intent = null;

            String launchedFrom = getIntent().getStringExtra("launchedFrom");

            if (launchedFrom.equals("myTutors"))
                intent = new Intent(TutorDetailsActivity.this, MyTutorsActivity.class);
            else if (launchedFrom.equals("findTutors"))
                intent = new Intent(TutorDetailsActivity.this, FindTutorsActivity.class);

            if (intent != null)
            {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
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