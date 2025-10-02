package psu.signlinguamobile.pages;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.gson.Gson;

import java.lang.Object;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import psu.signlinguamobile.R;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.utilities.VerificationMiddlewareChecker;

public abstract class BaseWebViewActivity extends AppCompatActivity
{
    private WebView m_webView;

    // For tracking the last loaded URL to prevent duplicate calls
    private String lastLoadedUrl = "";

    private boolean m_checkAuth = true;
    private boolean m_useDarkStatusIcons = false;

    private int m_webviewResource = R.id.webView;
    private int m_layoutResource = R.layout.activity_common_web_layout;

    /**
     * Called before rendering the view
     * */
    protected void onAwake() {}
    /**
     * Post initialization after onCreate()
     * */
    protected abstract void onInitialize();

    /**
     * Handle actions when the back key was pressed
     * */
    protected abstract void onBackKey();

    /**
     * Fires after the WebView's main document and its static resources (CSS, images, synchronous <script> tags) have finished loading.
     * This is called from the UI Thread
     * */
    protected abstract void onViewLoaded();

    /**
     * Called when the activity is being destroyed.
     * Best place to unregister webView javascript bridges here
     * */
    protected abstract void onDispose();

    protected void setWebviewResource(int resId)
    {
        m_webviewResource = resId;
    }

    protected void setLayoutResource(int resId)
    {
        m_layoutResource = resId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        onAwake();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (m_checkAuth)
            checkAuth();

        // Use the common web view layout
        setContentView(m_layoutResource);

        // Remove the top padding, to achieve that "Overlay" status bar effect.
        // This will render the content underneath it.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Works along with zero top-padding to render contents underneath it by removing opacity
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        WindowInsetsControllerCompat insetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        // Should the status bar use light or dark icons
        if (insetsController != null)
        {
            // false for light icons
            // true for dark icons
            insetsController.setAppearanceLightStatusBars(m_useDarkStatusIcons); //(false);
        }

        // Modernized approach to handle back key press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                onBackKey();
            }
        });

        initWebView();
        onInitialize();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        onDispose();
    }

    //=================================================
    // <editor-fold desc="Business Logic">
    //=================================================

    /**
     * Should we check for auth before rendering the view?
     * If the current user isn't authenticated, he will be
     * redirected to the login screen. Set this to false
     * if the activity doesn't need auth
     * */
    protected void shouldCheckAuth(boolean shouldCheck)
    {
        m_checkAuth = shouldCheck;
    }

    /**
     * Should the status bar use light or dark icons
     * @param useDark -> FALSE for LIGHT icons
     *                -> TRUE for DARK icons
     */
    protected void useDarkStatusIcons(boolean useDark)
    {
        m_useDarkStatusIcons = useDark;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView()
    {
        m_webView = findViewById(m_webviewResource);
        WebSettings webSettings = m_webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        m_webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.w("WEBVIEW", consoleMessage.message());
                return true;
            }
        });

        m_webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                // Prevents extra API calls by tracking the last loaded URL.
                // Ensures onPageFinished() only runs once per unique page load.
                // Works smoothly with redirects or dynamic content updates in WebView.
                if (!url.equals(lastLoadedUrl))
                {
                    lastLoadedUrl = url;
                    Log.d("WEBVIEW", "Page fully loaded: " + url);

                    runOnUiThread(() -> onViewLoaded());
                }
            }
        });
    }

    /**
     * Loads an HTML page from the assets/pages directory into the WebView.
     *
     * @param viewFileName Name of the HTML file (with or without ".html")
     */
    protected void renderView(String viewFileName)
    {
        if (!checkWebview()) return;

        if (TextUtils.isEmpty(viewFileName)) {
            Log.d("MINE", "No view filename provided.");
            return;
        }

        // Ensure file ends with ".html" (case-insensitive)
        if (!viewFileName.toLowerCase(Locale.ROOT).endsWith(".html")) {
            viewFileName += ".html";
        }

        // Load the asset page
        String filePath = String.format("file:///android_asset/pages/%s", viewFileName);
        m_webView.loadUrl(filePath);
    }

    /**
     * Add a Javascript Bridge to the webview
     */
    @SuppressLint("JavascriptInterface")
    protected void registerJsBridge(@NonNull Object object, @NonNull String name)
    {
        if (!checkWebview())
            return;

        m_webView.addJavascriptInterface(object, name);
    }

    /**
     * Remove a Javascript Bridge to the webview
     */
    protected void unregisterJsBridge(@NonNull String name)
    {

        if (!checkWebview())
            return;

        m_webView.removeJavascriptInterface(name);
    }

    /**
     * Opens a given activity
     * */
    protected <T> void launch(Class<T> activity)
    {
        Intent intent = new Intent(BaseWebViewActivity.this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }
    /**
     * Opens a given activity with String key-value pairs
     * */
    protected <T> void launchWith(Class<T> activity, HashMap<String, String> extras)
    {
        Intent intent = new Intent(BaseWebViewActivity.this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        for (Map.Entry<String, String> kvp : extras.entrySet())
        {
            intent.putExtra(kvp.getKey(), kvp.getValue());
        }

        finish();
        startActivity(intent);
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Javascript Bridge Methods">
    //=================================================
    protected void bridgeCall_showWebViewLoadingOverlay()
    {
        if (!checkWebview())
            return;

        m_webView.evaluateJavascript("showLoading()", null);
    }

    protected void bridgeCall_hideWebViewLoadingOverlay()
    {
        if (!checkWebview())
            return;

        m_webView.evaluateJavascript("hideLoading()", null);
    }

    protected void bridgeCall_alertWarn(String message)
    {
        if (!checkWebview())
            return;

        m_webView.evaluateJavascript("AlertWarn(\"" + message + "\")", null);
    }

    protected void bridgeCall_showSnackbar(String message)
    {
        if (!checkWebview())
            return;

        String js = String.format("ShowSnackbar(\"%s\")", message);
        m_webView.evaluateJavascript(js, null);
    }

    protected void bridgeCall_execJavascriptFunction(String function) {
        bridgeCall_execJavascriptFunction(function, null);
    }

    /**
     * Executes a javascript function with json string as its payload.
     * You don't have to manually escape the json as it is automatically
     * escaped before being sent to the web view bridge.
     * @param function The Javascript function to call
     * @param jsonPayload The JSON data to send
     */
    protected void bridgeCall_execJavascriptFunction(String function, @Nullable String jsonPayload)
    {
        if (!checkWebview() || function == null || function.trim().isEmpty()) return;

        final boolean hasPayload = jsonPayload != null && !jsonPayload.isEmpty();

        String escapedPayload = hasPayload ? escapeJson(jsonPayload) : "";
        String jsBody = hasPayload
                ? String.format("%s(`%s`);", function, escapedPayload)
                : String.format("%s();", function);

        Log.d("MINE", jsBody);
        m_webView.evaluateJavascript(jsBody, null);
    }

    /**
     * Call a function with a simple parameter
     * @param function The Javascript function to call
     * @param param The parameter data to send
     */
    protected void bridgeCall_function(String function, @Nullable String param)
    {
        if (!checkWebview() || function == null || function.trim().isEmpty()) return;

        final boolean hasPayload = param != null && !param.isEmpty();

        String jsBody = hasPayload
                ? String.format("%s(`%s`);", function, param)
                : String.format("%s();", function);

        Log.d("MINE", jsBody);
        m_webView.evaluateJavascript(jsBody, null);
    }

    /**
     * Call a function with a simple parameter
     * @param function The Javascript function to call
     * @param param The parameter data to send
     */
    protected void bridgeCall_function(String function, int param)
    {
        if (!checkWebview() || function == null || function.trim().isEmpty()) return;

        String jsBody = String.format("%s(%d);", function, param);

        Log.d("MINE", jsBody);
        m_webView.evaluateJavascript(jsBody, null);
    }
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Utility Methods">
    //=================================================

    /**
     * Encodes an object into a JSON string without escaping characters.
     *
     * @param obj The object to encode.
     * @return A raw (unescaped) JSON string.
     */
    protected String encodeJson(Object obj)
    {
        return encodeJson(obj, false);
    }

    /**
     * Encodes an object into a JSON string with character escaping,
     * making it safe for injection into WebView via JS bridge.
     *
     * @param obj The object to encode.
     * @return An escaped JSON string.
     */
    protected String encodeJsonEscaped(Object obj)
    {
        return encodeJson(obj, true);
    }

    /**
     * Core method to encode an object into a JSON string.
     * Optionally escapes special characters based on the flag.
     *
     * @param obj    The object to encode.
     * @param escape True to escape the JSON string; false to keep it raw.
     * @return The resulting JSON string.
     */
    protected String encodeJson(Object obj, boolean escape)
    {
        Gson gson = new Gson();
        String encoded = gson.toJson(obj);

        return escape ? escapeJson(encoded) : encoded;
    }

    /**
     * Escapes special characters in a JSON string so it can be
     * safely injected into a WebView JavaScript context.
     *
     * @param json The raw JSON string.
     * @return A sanitized JSON string with escaped characters.
     */
    protected String escapeJson(String json)
    {
        return json
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("`", "\\`");
    }

    protected WebView getWebView()
    {
        return m_webView;
    }
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Private Methods">
    //=================================================

    private boolean checkWebview()
    {
        if (m_webView == null)
        {
            Log.d("MINE", "The webView isn't initialized.");
            return false;
        }

        return true;
    }

    private void checkAuth()
    {
        // Also cache in your session manager:
        boolean isLoggedIn = SessionManager.getInstance().isLoggedIn();

        if (!isLoggedIn)
        {
            launch(LoginActivity.class);
        }
    }
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Verification Middleware">
    //=================================================
    private VerificationMiddlewareChecker verificationMw;

    protected VerificationMiddlewareChecker getVerificationMw()
    {
        if (verificationMw == null)
            verificationMw = new VerificationMiddlewareChecker(this);

        return verificationMw;
    }
    //=================================================
    // </editor-fold>
    //=================================================
}
