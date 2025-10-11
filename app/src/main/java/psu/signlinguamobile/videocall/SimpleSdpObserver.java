package psu.signlinguamobile.videocall;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Simple SDP observer implementation that logs events and allows overriding specific methods.
 */
public class SimpleSdpObserver implements SdpObserver {
    private static final String TAG = "MINE";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "SDP successfully created");
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "SDP successfully set");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "SDP creation failed: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "SDP setting failed: " + s);
    }
}