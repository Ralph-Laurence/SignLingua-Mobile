package psu.signlinguamobile.delegates;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class LoginJsBridge
{

    public interface LoginBridgeListener
    {
        void onSignIn(String username, String password);
        void onRegister();
    }

    private final LoginBridgeListener listener;
    private static final String TAG = "LoginJsBridge";

    public LoginJsBridge(LoginBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onSignIn(String username, String password)
    {
        // Log.d(TAG, "Received credentials -> Username: " + username + ", Password: " + password);
        // Delegate the login action to the listener (which is typically your Activity)
        if (listener != null) {
            listener.onSignIn(username, password);
        }
    }

    @JavascriptInterface
    public void onRegister()
    {
        if (listener != null) {
            listener.onRegister();
        }
    }
}
