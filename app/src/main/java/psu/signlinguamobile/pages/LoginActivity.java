package psu.signlinguamobile.pages;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.gson.Gson;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apirequest.LoginRequest;
import psu.signlinguamobile.api.apiresponse.LoginResponse;
import psu.signlinguamobile.api.apiservice.AuthApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.delegates.LoginJsBridge;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements LoginJsBridge.LoginBridgeListener
{

//    private GeckoView m_geckoView;
//    private GeckoSession m_session;
//    private GeckoRuntime m_runtime;
//    private GeckoCustomNavigationDelegate m_navDelegate;
    private WebView m_webView;
    private static final String PREFS_NAME_AUTH = Constants.SharedPrefKeys.AUTH;
    private static final String PREFS_KEY_TOKEN = Constants.SharedPrefKeys.TOKEN;
    private static final String PREFS_KEY_USER_ID = Constants.SharedPrefKeys.USER_ID;
    private static final String PREFS_KEY_USER_DETAILS = Constants.SharedPrefKeys.USER_DETAILS;
    private AuthApiService apiService;
    private final String JS_BRIDGE_NAME = "LoginBridge";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_common_web_layout);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        WindowInsetsControllerCompat insetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(true); // or false for light icons
        }

//        m_geckoView = findViewById(R.id.geckoView);
//        m_session   = new GeckoSession();
//        m_runtime   = GeckoRuntime.create(this);
//        m_navDelegate = new GeckoCustomNavigationDelegate();
//
//        m_session.open(m_runtime);
//
//        m_session.getSettings().setAllowJavascript(true);
//        m_session.setNavigationDelegate(m_navDelegate);
//
//        m_geckoView.setSession(m_session);
//        m_session.loadUri("resource://android/assets/pages/login.html"); // Or your local HTML

        m_webView = findViewById(R.id.webView);
        WebSettings webSettings = m_webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        m_webView.setWebViewClient(new WebViewClient());
        m_webView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);

        // Add the login bridge defined in a separate file.
        m_webView.addJavascriptInterface(new LoginJsBridge(this), JS_BRIDGE_NAME);

        m_webView.loadUrl("file:///android_asset/pages/login.html");

        // Initialize Retrofit API Service
        apiService = ApiClient.getClient(this).create(AuthApiService.class);
    }

    // This method is called from the bridge (make sure to switch to UI thread if necessary)
    @Override
    public void onSignIn(final String username, final String password) {
        runOnUiThread(() -> performLogin(username, password));
    }

    private void performLogin(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        apiService.login(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getToken();
                    User user = loginResponse.getUser();

                    // Log the token for debugging
                    Log.d("Login", "Token: " + token);

                    // Log various user details
                    Log.d("Login", "User ID: " + user.getId());
                    Log.d("Login", "First Name: " + user.getFirstname());
                    Log.d("Login", "Last Name: " + user.getLastname());
                    Log.d("Login", "Username: " + user.getUsername());
                    Log.d("Login", "Email: " + user.getEmail());
                    Log.d("Login", "Role: " + user.getRole());     // raw role integer
                    Log.d("Login", "Role Display: " + User.Role.fromInt(user.getRole()).getDisplayName());
                    Log.d("Login", "Contact: " + user.getContact());
                    Log.d("Login", "Address: " + user.getAddress());
                    Log.d("Login", "Photo: " + user.getPhoto()); // Constructed photo URL

                    cacheUser(user, token);
//                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME_AUTH, Context.MODE_PRIVATE);
//
//                    prefs.edit()
//                         .putString(PREFS_KEY_TOKEN, token)
//                         .putString(PREFS_KEY_USER_ID, user.getId())
//                         .apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    if (user.getRole() == User.Role.LEARNER.getValue())
                        startActivity(new Intent(LoginActivity.this, LearnerHomeActivity.class));

                    else if (user.getRole() == User.Role.TUTOR.getValue())
                        startActivity(new Intent(LoginActivity.this, TutorHomeActivity.class));

                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    m_webView.evaluateJavascript("javascript:alertInvalidCredentials()", null);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t)
            {
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("Login", "Error: " + t.getMessage());

                m_webView.evaluateJavascript("javascript:alertNetworkError()", null);
            }
        });
    }

    private void cacheUser(User user, String token)
    {
        // Assume you have a Gson instance;
        Gson gson = new Gson();

        // Inside performLogin() after a successful login:
        // User user = loginResponse.getUser(); // Already contains name, photo, etc.
        String userJson = gson.toJson(user);

        // Save token and user JSON together:
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME_AUTH, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PREFS_KEY_TOKEN, token)
                .putString(PREFS_KEY_USER_ID, user.getId()) // or part of the userJson, but saving separately if desired
                .putString(PREFS_KEY_USER_DETAILS, userJson)
                .commit();

        // SharedPreferences prefsx = getSharedPreferences(PREFS_NAME_AUTH, Context.MODE_PRIVATE);
        // String savedToken = prefsx.getString(PREFS_KEY_TOKEN, null);
        // String savedUserId = prefsx.getString(PREFS_KEY_USER_ID, null);
        // Log.d("Login", String.format("Stored Token: %s, Stored ID: %s", savedToken, savedUserId));

        // Also cache in your session manager:
        SessionManager.getInstance().saveSession(user, token);

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