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

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apirequest.LoginRequest;
import psu.signlinguamobile.api.apiresponse.LoginResponse;
import psu.signlinguamobile.api.apiservice.AuthApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.delegates.LoginJsBridge;
import psu.signlinguamobile.managers.LoginManager;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.HttpCodes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements LoginJsBridge.LoginBridgeListener
{
    private WebView m_webView;

    private LoginManager loginManager;
    private AuthApiService apiService;
    private final String JS_BRIDGE_NAME = "LoginBridge";

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
            insetsController.setAppearanceLightStatusBars(true); // or false for light icons
        }

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

        loginManager = new LoginManager(this.getApplicationContext());
    }

    // This method is called from the bridge (make sure to switch to UI thread if necessary)
    @Override
    public void onSignIn(final String username, final String password) {
        runOnUiThread(() -> performLogin(username, password));
    }

    @Override
    public void onRegister()
    {
        launch(RegistrationLanding.class);
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

                      // If the registration was for tutor, ask for their valid ID
//                    if (user.getRole() == User.Role.TUTOR.getValue() && loginResponse.getRequireValidId())
//                    {
//                        # HANDLE WITH 403 INSTEAD
//                        launch(CaptureIDBoardingActivity.class);
//                        return;
//                    }

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

                    loginManager.cacheUser(user, token);

//                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

//                    if (user.getRole() == User.Role.LEARNER.getValue())
//                        launch(LearnerHomeActivity.class);
//
//                    else if (user.getRole() == User.Role.TUTOR.getValue())
//                        launch(TutorHomeActivity.class);

                    // If the registration was for tutor, ask for their valid ID
                    if (user.getRole() == User.Role.TUTOR.getValue())
                    {
                        if (loginResponse.getRequireValidId())
                        {
                            launch(CaptureIDBoardingActivity.class);
                            return;
                        }

                        launch(TutorHomeActivity.class);
                    }
                    else if (user.getRole() == User.Role.LEARNER.getValue())
                        launch(LearnerHomeActivity.class);

                }
                else
                {
                    // Pending registrations throw 403
                    if (response.code() == HttpCodes.FORBIDDEN)
                    {
                        try
                        {
                            String errorJson = response.errorBody().string();
                            Gson gson = new Gson();
                            LoginResponse resp = gson.fromJson(errorJson, LoginResponse.class);
                            Log.e("MINE", errorJson); // Confirm payload is present

                            HashMap<String, String> extra = new HashMap<>();
                            extra.put("userId", resp.getUser().getId());

                            launchWith(CaptureIDBoardingActivity.class, extra);
                            return;
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (m_webView != null)
        {
            m_webView.removeJavascriptInterface(JS_BRIDGE_NAME); // Prevents leaks
        }
    }

    protected <T> void launch(Class<T> activity)
    {
        Intent intent = new Intent(LoginActivity.this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

    protected <T> void launchWith(Class<T> activity, HashMap<String, String> extras)
    {
        Intent intent = new Intent(LoginActivity.this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        for (Map.Entry<String, String> kvp : extras.entrySet())
        {
            intent.putExtra(kvp.getKey(), kvp.getValue());
        }

        finish();
        startActivity(intent);
    }
}