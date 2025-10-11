package psu.signlinguamobile.pages;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;

import psu.signlinguamobile.R;
import psu.signlinguamobile.delegates.EndCallJsBridge;
import psu.signlinguamobile.videocall.VideoCallActivity;

public class CallEndedActivity
        extends BaseWebViewActivity
        implements EndCallJsBridge.EndCallJsBridgeListener
{
    private String lastContactId;
    private String callDuration;

    @Override
    protected void onInitialize()
    {
        lastContactId = getIntent().getStringExtra("CONTACT_ID");

        registerJsBridge(new EndCallJsBridge(this), JS_BRIDGE_NAME);
        renderView("call_ended");

        Log.d("MINE", "CONTACT ID -> " + lastContactId);
    }

    @Override
    protected void onBackKey()
    {
        goBack();
    }

    @Override
    protected void onViewLoaded()
    {

    }

    @Override
    protected void onDispose()
    {
        this.unregisterJsBridge(JS_BRIDGE_NAME);
    }

    @Override
    public void onCallAgain()
    {
        HashMap<String, String> param = new HashMap<>();
        param.put("CONTACT_ID", lastContactId);
        param.put("NEEDS_TOKEN_RENEW", "1");

        launchWith(VideoCallActivity.class, param);
    }

    @Override
    public void onClose()
    {
        goBack();
    }

    private void goBack()
    {
        HashMap<String, String> param = new HashMap<>();
        param.put("contactHashId", lastContactId);

        launchWith(ChatActivity.class, param);
    }
}