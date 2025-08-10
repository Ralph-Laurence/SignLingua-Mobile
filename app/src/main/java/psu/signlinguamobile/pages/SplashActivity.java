package psu.signlinguamobile.pages;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.gson.Gson;

import psu.signlinguamobile.R;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.models.User;

public class SplashActivity extends AppCompatActivity
{
    // Duration for how long to display the splash screen (in milliseconds)
    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 2 seconds
    // private final String JS_BRIDGE_NAME =

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        WindowInsetsControllerCompat insetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false); // or false for light icons
            insetsController.setAppearanceLightNavigationBars(false); // false = light icons
        }

        setContentView(R.layout.activity_splash);

        // Load session from persistent storage into SessionManager (if available)
        loadSessionFromPrefs();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                redirectBasedOnSession();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void loadSessionFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(Constants.SharedPrefKeys.AUTH, Context.MODE_PRIVATE);
        String token = prefs.getString(Constants.SharedPrefKeys.TOKEN, null);
        String userJson = prefs.getString("user_details", null);

        if (token != null && userJson != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            SessionManager.getInstance().saveSession(user, token);
        }
    }

    private void redirectBasedOnSession()
    {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn())
        {
            User user = session.getCurrentUser();
            int role = user.getRole();

            if (role == User.Role.LEARNER.getValue()) {
                startActivity(new Intent(SplashActivity.this, LearnerHomeActivity.class));
            } else if (role == User.Role.TUTOR.getValue()) {
                startActivity(new Intent(SplashActivity.this, TutorHomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }

//    @Override
//    protected void onDestroy()
//    {
//        super.onDestroy();
//        if (m_webView != null)
//        {
//            m_webView.removeJavascriptInterface(JS_BRIDGE_NAME); // Prevents leaks
//        }
//    }
}