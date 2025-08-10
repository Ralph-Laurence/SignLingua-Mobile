package psu.signlinguamobile.pages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import psu.signlinguamobile.api.apiresponse.PaginatedResponse;
import psu.signlinguamobile.api.apiservice.TutorManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.delegates.FindTutorJsBridge;
import psu.signlinguamobile.models.TutorItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTutorsActivity extends AppCompatActivity implements FindTutorJsBridge.FindTutorJsBridgeListener
{
    private WebView m_webView;
    private TutorManagementApiService tutorMgtApiService;
    private String lastLoadedUrl = "";
    private final String JS_BRIDGE_NAME = "FindTutorJsBridge";

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
                    runOnUiThread(() -> fetchTutors(null, 1, null));
                }
            }
        });

        // Add the js bridge defined in a separate file.
        m_webView.addJavascriptInterface(new FindTutorJsBridge(this), JS_BRIDGE_NAME);

        m_webView.loadUrl("file:///android_asset/pages/my_tutors.html");

        // Initialize Retrofit API Service
        tutorMgtApiService = ApiClient.getClient(this, true).create(TutorManagementApiService.class);
    }

    private String escapeJsonForWebView(String json) {
        return json.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void onGoBack()
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(MyTutorsActivity.this, LearnerHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onFindTutors(String search, int page, String disability)
    {
        runOnUiThread(() -> fetchTutors(search, page, disability));
    }

    @Override
    public void onStalkProfile(String id)
    {
        runOnUiThread(() -> {
            Intent intent = new Intent(MyTutorsActivity.this, TutorDetailsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("tutorId", id);
            intent.putExtra("launchedFrom", "myTutors");
            startActivity(intent);
            finish();
        });
    }

    private void fetchTutors(String search, int page, String disability)
    {
        tutorMgtApiService.findTutors(page, search, disability, "friend").enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<PaginatedResponse<TutorItem>> call, Response<PaginatedResponse<TutorItem>> response)
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

                    Log.d("MINE", jsonResponse);
                    // Pass JSON string safely to WebView
                    m_webView.evaluateJavascript("javascript:displayTutors(`" + jsonResponse + "`);", null);

                } catch (Exception e) {
                    Log.e("RETROFIT_ERROR", "Error reading response body: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<TutorItem>> call, Throwable t)
            {
                Log.e("API_ERROR", "Failed to fetch tutors: " + t.getMessage());
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

