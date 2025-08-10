package psu.signlinguamobile.pages;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Locale;

import psu.signlinguamobile.R;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.delegates.ScannerJsBridge;
import psu.signlinguamobile.managers.LocalHttpServer;

public class ScannerActivity extends AppCompatActivity implements ScannerJsBridge.ScannerJsBridgeListener
{
    private WebView m_webView;
    private String lastLoadedUrl = "";
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 211;
    private TextToSpeech tts;
    private Bundle bundleParams;
    private final String[] requiredPerms = new String[]
    {
        Manifest.permission.CAMERA,
        //Manifest.permission.RECORD_AUDIO
    };

    private LocalHttpServer server;
    private final String JS_BRIDGE_NAME = "ScannerJSBridge";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_common_web_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false); // or false for light icons
            insetsController.setAppearanceLightNavigationBars(false);
        }

        bundleParams = new Bundle();
        bundleParams.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f); // Example parameter

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
        requestPerms();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                onGoBack();
            }
        });
    }

    private void requestPerms()
    {
        if (hasAllPermissions())
        {
            startWebView();
            return;
        }

        // Ask directly. Do not check rationale now.
        // ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);
        Log.d("MINE", String.valueOf(denialCount));

        if (denialCount == 2)
        {
            // Third+ denial: assume “Don’t ask again”
            showGoToSettingsDialog();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPerms[0])) {
            // Second denial: show rationale dialog
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Camera Access Required")
                    .setMessage("Dear User,\n\nTo recognize hand gestures, we need access to your camera.\n\nWe deeply respect your privacy and assure you that the camera will only be used for gesture recognition—nothing else.\n\nPlease grant camera access to continue using this feature.")
                    .setPositiveButton("I Understand", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
                    })
                    .setNegativeButton("No, Thanks", (dialog, which) -> onGoBack())
                    .show();
        }

        else
        {
            ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_CAMERA_PERMISSION || !permissions[0].equals(Manifest.permission.CAMERA))
            return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            startWebView();
        }
        else
        {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);

            // Check only CAMERA permission
            denialCount++;
            prefs.edit().putInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, denialCount).commit();

            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Camera Permission Denied")
                    .setMessage("Dear User,\n\nYou have denied camera access, which is required for gesture recognition.\n\nWithout this permission, the scanner cannot function and will now exit.")
                    .setPositiveButton("OK", (dialog, which) -> onGoBack())
                    .show();
        }
    }

    private void showGoToSettingsDialog()
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Camera Access Required")
                .setMessage("Dear User,\n\nYou have chosen to deny camera access permanently. Since this permission is essential for gesture recognition, the scanner cannot function without it.\n\nTo enable camera access, please go to your app settings and grant the permission manually.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> onGoBack())
                .show();
    }

    private boolean hasAllPermissions()
    {
        for (String perm : requiredPerms)
        {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    private void startWebView()
    {
        m_webView = findViewById(R.id.webView);
        WebSettings webSettings = m_webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        m_webView.addJavascriptInterface(new ScannerJsBridge(this), JS_BRIDGE_NAME);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);

        m_webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message());
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // For simplicity, grant all requested resources
                runOnUiThread(() -> request.grant(request.getResources()));
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
                    //runOnUiThread(() -> fetchDetails());
                }
            }
        });

        try
        {
            server = new LocalHttpServer(this, 8080);
            server.start();
            Log.d("LocalHttpServer", "Server started at http://localhost:8080");
            m_webView.loadUrl("http://localhost:8080/index.html");
        }
        catch (Exception e)
        {
            Log.e("LocalHttpServer", "Failed to start server", e);
        }
    }


    private String escapeJsonForWebView(String json) {
        return json.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void onReadLetter(String letter)
    {
        if (letter != null)
        {
            Log.d("MINE", "This one is called");
            tts.speak(letter, TextToSpeech.QUEUE_FLUSH, bundleParams, "common_utterance");
        }
    }

    @Override
    public void onStopRead()
    {
        if (tts != null)
            tts.stop();
    }

    @Override
    public void onGoBack()
    {
        runOnUiThread(() -> {
            Intent intent = null;

            String launchedFrom = getIntent().getStringExtra("launchedFrom");

            if (launchedFrom.equals("tutorHome"))
                intent = new Intent(ScannerActivity.this, TutorHomeActivity.class);
            else if (launchedFrom.equals("learnerHome"))
                intent = new Intent(ScannerActivity.this, LearnerHomeActivity.class);

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
        server.stop();

        if (m_webView != null)
        {
            m_webView.removeJavascriptInterface(JS_BRIDGE_NAME); // Prevents leaks
        }
    }
}