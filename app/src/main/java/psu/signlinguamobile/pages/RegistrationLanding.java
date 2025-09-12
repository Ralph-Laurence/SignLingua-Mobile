package psu.signlinguamobile.pages;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//
//import psu.signlinguamobile.R;

import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

import psu.signlinguamobile.delegates.RegistrationLandingJsBridge;
import psu.signlinguamobile.models.RegistrationDetails;

public class RegistrationLanding
        extends BaseWebViewActivity
        implements RegistrationLandingJsBridge.RegistrationLandingJsBridgeListener
{
    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    @Override
    protected void onAwake()
    {
        shouldCheckAuth(false);
    }

    @Override
    protected void onInitialize()
    {
        registerJsBridge(new RegistrationLandingJsBridge(this), JS_BRIDGE_NAME);

        renderView("registration_landing");
    }

    @Override
    protected void onBackKey()
    {
        finish();
    }

    @Override
    protected void onViewLoaded()
    {

    }

    @Override
    protected void onDispose()
    {
        unregisterJsBridge(JS_BRIDGE_NAME); // Prevents leaks
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: JS BRIDGE METHODS">
    //=================================================

    @Override
    public void onGoBack()
    {
        finish();
    }

    @Override
    public void onLoginPage()
    {
        launch(LoginActivity.class);
    }

    @Override
    public void onRegisterTutorPage()
    {
        HashMap<String, String> extras = new HashMap<>();
        // extras.put("launchedFrom", "tutorRegistration");
        extras.put("registrationMode", String.valueOf(RegistrationDetails.TUTOR_REGISTRATION));

        launchWith(RegistrationActivity.class, extras);
    }

    @Override
    public void onRegisterLearnerPage()
    {
        HashMap<String, String> extras = new HashMap<>();
        // extras.put("launchedFrom", "learnerRegistration");
        extras.put("registrationMode", String.valueOf(RegistrationDetails.LEARNER_REGISTRATION));

        launchWith(RegistrationActivity.class, extras);
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: BUSINESS METHODS">
    //=================================================

//    private void alert(String message, String title, DialogInterface.OnClickListener onOk, DialogInterface.OnClickListener onCancel)
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(false);
//        builder.setTitle(title.isEmpty() ? getString(R.string.app_name) : title);
//        builder.setMessage(message);
//        builder.setPositiveButton("OK", onOk);
//        builder.setNegativeButton("Cancel", onCancel);
//        builder.setIcon(R.drawable.app_logo_xl);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
    //=================================================
    // </editor-fold>
    //=================================================
}