package psu.signlinguamobile.videocall;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiservice.ChatApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.models.RenewAuthTokenResponse;
import psu.signlinguamobile.pages.CallEndedActivity;
import psu.signlinguamobile.pages.ChatActivity;
import psu.signlinguamobile.pages.ContactsActivity;
import psu.signlinguamobile.utilities.HubUtils;
import psu.signlinguamobile.utilities.VerificationMiddlewareChecker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoCallActivity extends AppCompatActivity
{
    //=============================================
    // <editor-fold desc="PERMISSIONS MANAGEMENT">
    //=============================================
    //
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 211;
    private final String[] requiredPerms = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };
    //
    // </editor-fold>
    //=============================================

    private static final String TAG = "MINE";
    // WebRTC related variables
    private EglBase eglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private AudioManager m_audioManager;
    private VideoCapturer videoCapturer;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer remoteVideoView;
    private boolean m_isInitiator = false;
    private TextView statusText;
    private ImageButton microphoneButton;
    private ImageButton cameraButton;
    private ImageButton endCallButton;
    private boolean isMicrophoneEnabled = true;
    private boolean isCameraEnabled = true;

    // Signaling client
    private String m_authToken;
    private String m_contactPeerId;

    private SignalingClientSR signalingClient;
    private ChatApiService chatApiService;
    private VerificationMiddlewareChecker verificationMw;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_call), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        verificationMw  = new VerificationMiddlewareChecker(this);
        m_contactPeerId = getIntent().getStringExtra("CONTACT_ID");

        String needsTokenRenew = getIntent().getStringExtra("NEEDS_TOKEN_RENEW");

        if (needsTokenRenew != null && needsTokenRenew.equals("1"))
        {
            chatApiService = ApiClient.getClient(this, true).create(ChatApiService.class);
            refreshAuthToken();
        }
        else
        {
            m_authToken = getIntent().getStringExtra("JWT");
            requestPerms();
        }
    }

    private void begin()
    {
        // Initialize UI elements
        localVideoView = findViewById(R.id.local_video_view);
        remoteVideoView = findViewById(R.id.remote_video_view);
        statusText = findViewById(R.id.status_text);
        microphoneButton = findViewById(R.id.microphone_button);
        cameraButton = findViewById(R.id.camera_button);
        endCallButton = findViewById(R.id.end_call_button);

        // Set up click listeners for controls
        microphoneButton.setOnClickListener(v -> toggleMicrophone());
        cameraButton.setOnClickListener(v -> toggleCamera());
        endCallButton.setOnClickListener(v -> endCall());

        // Configure AudioManager for correct audio routing
        m_audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        m_audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);  // Ensures proper mic gain/routing
        m_audioManager.setSpeakerphoneOn(true); // Optional: controls speakerphone output
        // m_audioManager.setParameters("agc_enabled=true");

        // Initialize WebRTC components
        initializeWebRTC();

        // Setup the signaling server
        if (/*m_roomId == null ||*/ m_authToken == null)
        {
            Log.d("MINE", "Room ID is missing or auth token is null");
            Toast.makeText(this, "Unable to join the call.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        signalingClient = initializeSignalR(m_authToken, m_contactPeerId);
    }

    private void initializeWebRTC()
    {
        // Create EglBase for rendering
        eglBase = EglBase.create();

        // Initialize video views
        localVideoView.init(eglBase.getEglBaseContext(), null);
        localVideoView.setZOrderMediaOverlay(true); // Draw local view on top
        remoteVideoView.init(eglBase.getEglBaseContext(), null);

        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initOptions);

        // Create PeerConnectionFactory
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory videoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true);
        DefaultVideoDecoderFactory videoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(videoEncoderFactory)
                .setVideoDecoderFactory(videoDecoderFactory)
                .createPeerConnectionFactory();

        // Create media streams
        setupMediaStreams();

        // Create peer connection
        createPeerConnection();

        // Add tracks to peer connection
        addTracksToLocalPeerConnection();
    }

    private void setupMediaStreams()
    {
        // Create video capturer
        videoCapturer = createVideoCapturer();
        if (videoCapturer == null) {
            Log.e(TAG, "Failed to create video capturer");
            Toast.makeText(this, "Failed to access camera", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Create video source
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());

        // Create video track
        localVideoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        localVideoTrack.addSink(localVideoView);

        // Create audio source and track
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));

        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);

        // Start video capture
        videoCapturer.startCapture(640, 480, 30);
    }

    private SignalingClientSR initializeSignalR(String authToken, String peerId)
    {
        return  new SignalingClientSR(
                HubUtils.getVideoCallHubUrl(),            // your backend URL
                authToken,        // your JWT token
                peerId,    // the other participant's ID
                new SignalingClientSR.SignalRListener() {
                    @Override
                    public void onConnected() {
                        runOnUiThread(() -> statusText.setText("Waiting for another user to join..."));
                    }

                    @Override
                    public void onOfferReceived(JSONObject offer) {

                        Log.d("MINE", "[WEBRTC] -> onOfferReceived");

                        runOnUiThread(() -> {
                            statusText.setText("Received offer, creating answer...");
                            SessionDescription remoteSdp = new SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    offer.optString("sdp")
                            );
                            createAnswer(remoteSdp);
                        });
                    }

                    @Override
                    public void onAnswerReceived(JSONObject answer) {

                        Log.d("MINE", "[WEBRTC] -> onAnswerReceived");

                        runOnUiThread(() -> {
                            statusText.setText("Received answer, connecting...");
                            SessionDescription remoteSdp = new SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    answer.optString("sdp")
                            );
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), remoteSdp);
                        });
                    }

                    @Override
                    public void onIceCandidateReceived(JSONObject candidate) {

                        Log.d("MINE", "[WEBRTC] -> onIceCandidateReceived");

                        runOnUiThread(() -> {
                            IceCandidate ice = new IceCandidate(
                                    candidate.optString("sdpMid"),
                                    candidate.optInt("sdpMLineIndex"),
                                    candidate.optString("candidate")
                            );
                            peerConnection.addIceCandidate(ice);
                        });
                    }

                    @Override
                    public void onDisconnected() {
                        runOnUiThread(() -> {
                            statusText.setText("Disconnected");
                            Toast.makeText(VideoCallActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            statusText.setText("Error: " + message);
                            Toast.makeText(VideoCallActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onRoomJoined(String roomId, boolean isInitiator) {
                        Log.d("MINE", "JOINED TO -> " + roomId);
                        m_isInitiator = isInitiator;

                        runOnUiThread(() -> {
                            // statusText.setText("Joined room: " + roomId);
                            statusText.setText("Ringing...");
                            if (isInitiator) {
                                Log.d("MINE", "Waiting for peer to be ready...");
                                signalingClient.ringContact(m_contactPeerId, roomId);
                                // Do nothing yet — wait for PeerReady
                            } else {
                                signalingClient.sendReadyForCall(roomId);
                            }
                        });
                    }

                    @Override
                    public void onPeerReady() {
                        runOnUiThread(() -> {
                            Log.d("MINE", "PeerReady received, creating offer...");
                            createOffer();
                        });
                    }

                }
        );
    }

    private VideoCapturer createVideoCapturer()
    {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIds;

        try {
            cameraIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to access camera IDs");
            return null;
        }

        String usbCameraId = null;

        for (String id : cameraIds) {
            CameraCharacteristics characteristics;
            try {
                characteristics = manager.getCameraCharacteristics(id);
            } catch (CameraAccessException e) {
                continue;
            }

            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

            if (facing == null || facing == CameraCharacteristics.LENS_FACING_EXTERNAL) {
                usbCameraId = id; // Found USB camera
                Log.d(TAG, "USB Camera found: " + usbCameraId);
                break; // Stop after finding the first USB camera
            }
        }

        if (usbCameraId != null) {
            CameraEnumerator enumerator = new Camera2Enumerator(this);
            VideoCapturer videoCapturer = enumerator.createCapturer(usbCameraId, null);
            if (videoCapturer != null) {
                Log.d(TAG, "Using USB Camera: " + usbCameraId);
                return videoCapturer;
            }
        }

        // If no USB camera, fall back to default logic
        if (Camera2Enumerator.isSupported(this)) {
            Log.d(TAG, "Using Camera2 API");
            return createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Log.d(TAG, "Using Camera1 API");
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }


    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator)
    {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // If front facing camera not available, try back camera
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private void createPeerConnection()
    {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        // Add Google's public STUN servers for NAT traversal
        iceServers.add(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                        .createIceServer()
        );

        // You may want to add TURN servers for more reliability
        // iceServers.add(
        //     PeerConnection.IceServer.builder("turn:turn.example.org")
        //         .setUsername("username")
        //         .setPassword("password")
        //         .createIceServer()
        // );

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        // Create the peer connection
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer()
        {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState)
            {
                Log.d(TAG, "onSignalingChange: " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState)
            {
                Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);
                runOnUiThread(() -> {
                    switch (iceConnectionState) {
                        case CONNECTED:
                        case COMPLETED:
                            statusText.setText("Connected");
                            statusText.setBackgroundColor(ContextCompat.getColor(VideoCallActivity.this, R.color.bg_call_status_text_connected));
                            break;
                        case DISCONNECTED:
                            statusText.setText("Disconnected");
                            break;
//                        case FAILED:
//                            statusText.setText("Connection failed");
                        case FAILED:
                        case CLOSED:
                            HashMap<String, String> param = new HashMap<>();
                            param.put("CONTACT_ID", m_contactPeerId);
                            // param.put("CALL_DURATION", "xx");

                            launchWith(CallEndedActivity.class, param);
                            break;
                        default:
                            statusText.setText("Connecting...");
                            statusText.setBackgroundColor(ContextCompat.getColor(VideoCallActivity.this, R.color.bg_call_status_text));
                    }
                });
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b)
            {
                Log.d(TAG, "onIceConnectionReceivingChange: " + b);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState)
            {
                Log.d(TAG, "onIceGatheringChange: " + iceGatheringState);
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate)
            {
                Log.d(TAG, "onIceCandidate: " + iceCandidate);
                try
                {
                    JSONObject iceCandidateJson = new JSONObject();
                    iceCandidateJson.put("type", "ice-candidate");
                    iceCandidateJson.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                    iceCandidateJson.put("sdpMid", iceCandidate.sdpMid);
                    iceCandidateJson.put("candidate", iceCandidate.sdp);

                    signalingClient.sendIceCandidate(iceCandidateJson);
                }
                catch (JSONException e) {
                    Log.e(TAG, "Failed to send ice candidate: " + e.getMessage());
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates)
            {
                Log.d(TAG, "onIceCandidatesRemoved");
            }

            @Override
            public void onAddStream(MediaStream mediaStream)
            {
                // This method is deprecated but still required by the interface
                // Log.d(TAG, "onAddStream");
                if (!mediaStream.audioTracks.isEmpty()) {
                    AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                    remoteAudioTrack.setEnabled(true); // Only this should play
                }
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream)
            {
                // This method is deprecated but still required by the interface
                Log.d(TAG, "onRemoveStream");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel)
            {
                Log.d(TAG, "onDataChannel");
            }

            @Override
            public void onRenegotiationNeeded()
            {
                Log.d(TAG, "onRenegotiationNeeded");
                if (m_isInitiator) {
                    createOffer();
                }
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams)
            {
                Log.d(TAG, "onAddTrack");
                // This will be called when remote track is added
                if (rtpReceiver.track() instanceof VideoTrack) {
                    VideoTrack remoteVideoTrack = (VideoTrack) rtpReceiver.track();
                    runOnUiThread(() -> {
                        remoteVideoTrack.addSink(remoteVideoView);
                        statusText.setText("Connected");
                    });
                }
            }
        });
    }

    private void addTracksToLocalPeerConnection()
    {
        if (peerConnection != null)
        {
            // Add local audio and video tracks to the connection
            peerConnection.addTrack(localVideoTrack, List.of("ARDAMS"));
            peerConnection.addTrack(localAudioTrack, List.of("ARDAMS"));
        }
    }

    private void createOffer()
    {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createOffer(new SimpleSdpObserver()
        {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription)
            {
                Log.d(TAG, "Create offer success");
                peerConnection.setLocalDescription(new SimpleSdpObserver()
                {
                    @Override
                    public void onSetSuccess()
                    {
                        Log.d(TAG, "Set local description success");
                        try {
                            JSONObject offerJson = new JSONObject();
                            offerJson.put("type", "offer");
                            offerJson.put("sdp", sessionDescription.description);

                            signalingClient.sendOffer(offerJson);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to send offer: " + e.getMessage());
                        }
                    }
                }, sessionDescription);
            }
        }, constraints);
    }

    private void createAnswer(SessionDescription remoteSdp)
    {
        peerConnection.setRemoteDescription(new SimpleSdpObserver()
        {
            @Override
            public void onSetSuccess()
            {
                Log.d(TAG, "Set remote description success");

                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

                peerConnection.createAnswer(new SimpleSdpObserver()
                {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription)
                    {
                        Log.d(TAG, "Create answer success");
                        peerConnection.setLocalDescription(new SimpleSdpObserver()
                        {
                            @Override
                            public void onSetSuccess()
                            {
                                Log.d(TAG, "Set local description success for answer");
                                try {
                                    JSONObject answerJson = new JSONObject();
                                    answerJson.put("type", "answer");
                                    answerJson.put("sdp", sessionDescription.description);

                                    signalingClient.sendAnswer(answerJson);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Failed to send answer: " + e.getMessage());
                                }
                            }
                        }, sessionDescription);
                    }
                }, constraints);
            }
        }, remoteSdp);
    }

    private void toggleMicrophone()
    {
        isMicrophoneEnabled = !isMicrophoneEnabled;
        localAudioTrack.setEnabled(isMicrophoneEnabled);
        // Update UI to reflect state
        microphoneButton.setAlpha(isMicrophoneEnabled ? 1.0f : 0.5f);
    }

    private void toggleCamera()
    {
        isCameraEnabled = !isCameraEnabled;
        localVideoTrack.setEnabled(isCameraEnabled);
        // Update UI to reflect state
        cameraButton.setAlpha(isCameraEnabled ? 1.0f : 0.5f);
    }

    private void endCall()
    {
        // Clean up and end the call
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (signalingClient != null) {
            signalingClient.disconnect();
        }
        finish();
    }

    @Override
    protected void onDestroy()
    {
        // Clean up resources
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to stop video capture: " + e.getMessage());
            }
            videoCapturer.dispose();
        }

        if (localVideoView != null) {
            localVideoView.release();
        }

        if (remoteVideoView != null) {
            remoteVideoView.release();
        }

        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(false);
            localAudioTrack.dispose();
        }

        if (m_audioManager != null)
        {
            m_audioManager.setParameters("agc_enabled=false");
            m_audioManager.setMode(AudioManager.MODE_NORMAL); // Restore normal audio routing
            m_audioManager.abandonAudioFocus(null); // Release any lingering audio focus
        }

        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }

        if (signalingClient != null) {
            signalingClient.disconnect();
        }

        if (eglBase != null) {
            eglBase.release();
            eglBase = null;
        }

        super.onDestroy();
    }

    private void onGoBack()
    {
        // If no contact id, default to contacts page
        if (m_contactPeerId == null)
        {
            launchWith(ContactsActivity.class, null);
            return;
        }

        HashMap<String, String> param = new HashMap<>();
        param.put("contactHashId", m_contactPeerId);

        launchWith(ChatActivity.class, param);
    }

    protected <T> void launchWith(Class<T> activity, HashMap<String, String> extras)
    {
        Intent intent = new Intent(VideoCallActivity.this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (extras != null && !extras.isEmpty())
        {
            for (Map.Entry<String, String> kvp : extras.entrySet())
            {
                intent.putExtra(kvp.getKey(), kvp.getValue());
            }
        }

        finish();
        startActivity(intent);
    }

    //=============================================
    // <editor-fold desc="PERMISSIONS MANAGEMENT">
    //=============================================
    //
    private void requestPerms()
    {
        if (hasAllPermissions())
        {
            begin();
            return;
        }

        // Ask directly. Do not check rationale now.
        // ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);
        Log.d("MINE", String.valueOf(denialCount));

        if (denialCount == 2)
        {
            // Third+ denial: assume "Don't ask again"
            showGoToSettingsDialog(
                    "Camera and Microphone Access Required",
                    "Dear User,\n\nYou've denied camera or microphone access multiple times. These permissions are essential for video calling.\n\nTo enable full functionality, please go to your app settings and grant both camera and microphone access manually."
            );

            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPerms[0])) {
            // Second denial: show rationale dialog
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Camera Access Required")
                    .setMessage("Dear User,\n\nFor the video call to work, we need access to your camera.\n\nWe deeply respect your privacy and assure you that the camera will only be used for video calls and nothing else.\n\nPlease grant camera access to continue using this feature.")
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

        if (requestCode != REQUEST_CODE_CAMERA_PERMISSION)
            return;

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean allGranted = true;

        for (int i = 0; i < permissions.length; i++)
        {
            String perm = permissions[i];
            int result = grantResults[i];

            if (result != PackageManager.PERMISSION_GRANTED)
            {
                allGranted = false;

                if (perm.equals(Manifest.permission.CAMERA)) {
                    int cameraDenialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);
                    prefs.edit().putInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, cameraDenialCount + 1).apply();
                }

                if (perm.equals(Manifest.permission.RECORD_AUDIO)) {
                    int micDenialCount = prefs.getInt(Constants.SharedPrefKeys.MIC_PERMISSION_DENIALS, 0);
                    prefs.edit().putInt(Constants.SharedPrefKeys.MIC_PERMISSION_DENIALS, micDenialCount + 1).apply();
                }
            }
        }

        if (allGranted) {
            begin();
        } else {
            int cameraDenialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);
            int micDenialCount = prefs.getInt(Constants.SharedPrefKeys.MIC_PERMISSION_DENIALS, 0);

            if (cameraDenialCount >= 2 || micDenialCount >= 2) {
                showGoToSettingsDialog("Permissions Required",
                        "You've denied camera or microphone access multiple times. To enable video calling, please grant these permissions in your app settings.");
            } else {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("Permissions Denied")
                        .setMessage("Camera and microphone access are required for video calling. Please grant these permissions to continue.")
                        .setPositiveButton("OK", (dialog, which) -> onGoBack())
                        .show();
            }
        }
    }

    private void showGoToSettingsDialog(String title, String message)
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
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
    //
    // </editor-fold>
    //=============================================

    private void refreshAuthToken()
    {
        chatApiService.renewAuthToken().enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<RenewAuthTokenResponse> call, Response<RenewAuthTokenResponse> response)
            {
                if (!verificationMw.IsAllowed(response))
                    return;

//                Gson gson = new Gson();
//                String resp = gson.toJson(response.body());

                RenewAuthTokenResponse auth = response.body();
                m_authToken = auth.getToken();

                runOnUiThread(() -> requestPerms());
            }

            @Override
            public void onFailure(Call<RenewAuthTokenResponse> call, Throwable t)
            {
                Log.d("MINE", "AND IT FAILS -> " + t.getMessage());
                // Simply go back to the contacts page on failure
                launchWith(ContactsActivity.class, null);
            }
        });
    }
}





























/*
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
*/