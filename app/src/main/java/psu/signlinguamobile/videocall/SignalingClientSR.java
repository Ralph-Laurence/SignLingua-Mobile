package psu.signlinguamobile.videocall;

import android.annotation.SuppressLint;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import io.reactivex.rxjava3.core.Single;
import psu.signlinguamobile.utilities.HubUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class SignalingClientSR
{
    private final HubConnection m_callHubConn;
    private HubConnection m_notifHubConn;

    //private final String roomId;
    private String roomId = null;
    private final String peerUserId;
    private final SignalRListener listener;
    protected boolean isInitiator = false;
    public interface SignalRListener {
        void onConnected();
        void onOfferReceived(JSONObject offer);
        void onAnswerReceived(JSONObject answer);
        void onIceCandidateReceived(JSONObject candidate);
        void onDisconnected();
        void onError(String message);
//        void onRoomJoined(String roomId);
        void onRoomJoined(String roomId, boolean isInitiator);
        void onPeerReady();
    }

    public SignalingClientSR(String serverUrl, String jwtToken, /*String roomId,*/ String peerUserId, SignalRListener listener) {
        //this.roomId = roomId;
        this.peerUserId = peerUserId;
        this.listener = listener;

        m_callHubConn = HubConnectionBuilder.create(serverUrl)
                .withAccessTokenProvider(Single.defer(() -> Single.just(jwtToken)))
                .build();

        m_notifHubConn = HubConnectionBuilder.create(HubUtils.getNotifHubUrl())
                .withAccessTokenProvider(Single.defer(() -> Single.just(jwtToken)))
                .build();

        registerHandlers();
        connect();
    }

    private void registerHandlers()
    {
        m_callHubConn.on("ReceiveOffer", (senderId, sdp) -> {
            JSONObject offer = new JSONObject();
            try
            {
                offer.put("type", "offer");
                offer.put("sdp", sdp);

                Log.d("MINE", "ReceiveOffer");
            }
            catch (JSONException e)
            {
                Log.d("MINE", "Error in Handler 'ReceiveOffer' -> " + e.getMessage());
                throw new RuntimeException(e);
            }

            listener.onOfferReceived(offer);
        }, String.class, String.class);

        m_callHubConn.on("ReceiveAnswer", (senderId, sdp) -> {
            JSONObject answer = new JSONObject();
            try
            {
                answer.put("sdp", sdp);
                answer.put("type", "answer");

                Log.d("MINE", "ReceiveAnswer");
            }
            catch (JSONException e)
            {
                Log.d("MINE", "Error in Handler 'ReceiveAnswer' -> " + e.getMessage());
                throw new RuntimeException(e);
            }

            listener.onAnswerReceived(answer);
        }, String.class, String.class);

        m_callHubConn.on("ReceiveIceCandidate", (senderId, candidate, sdpMid, sdpMLineIndex) -> {
            JSONObject ice = new JSONObject();
            try
            {
                ice.put("type", "ice-candidate");
                ice.put("candidate", candidate);
                ice.put("sdpMid", sdpMid);
                ice.put("sdpMLineIndex", sdpMLineIndex);

                Log.d("MINE", "ReceiveIceCandidate");
            }
            catch (JSONException e)
            {
                Log.d("MINE", "Error in Handler 'ReceiveIceCandidate' -> " + e.getMessage());
                throw new RuntimeException(e);
            }

            listener.onIceCandidateReceived(ice);
        }, String.class, String.class, String.class, Integer.class);

//        hubConnection.on("CallRoomJoined", (roomId) -> {
//            this.roomId = roomId;
//            Log.d("SignalR", "Joined room: " + roomId);
//            listener.onRoomJoined(roomId);
//        }, String.class);

        m_callHubConn.on("CallRoomJoined", (roomId, initiatorFlag) -> {
            this.roomId = roomId;
            this.isInitiator = Boolean.parseBoolean(initiatorFlag);
            Log.d("SignalR", "Joined room: " + roomId + ", isInitiator: " + isInitiator);
            listener.onRoomJoined(roomId, isInitiator);
        }, String.class, String.class);

        m_callHubConn.on("PeerReady", () -> {
            Log.d("MINE", "Peer is ready, creating offer...");
            listener.onPeerReady(); // add this to your interface
        });
    }

    private void connect()
    {
        m_callHubConn.start().doOnComplete(() -> {
            m_callHubConn.send("JoinCallRoom", peerUserId);
            Log.d("MINE", "INITIATING JOIN ROOM");
            listener.onConnected();
        }).doOnError(error -> listener.onError(error.getMessage())).subscribe();

        m_notifHubConn.start().subscribe();
    }

    // Used for late joins (eg caller already joined but recipient joined late, but after accepting the ringing)
    public void sendReadyForCall(String roomId) {
        m_callHubConn.send("ReadyForCall", roomId);
        Log.d("MINE", "Sent ReadyForCall for room: " + roomId);
    }

    // Used for watching incoming call (eg ringing)

    @SuppressLint("CheckResult")
    public void ringContact(String recipientUserId, String roomId)
    {
        m_notifHubConn.invoke("SendCallNotification", recipientUserId, roomId)
                .subscribe(() -> Log.d("MINE", "Call notification sent to " + recipientUserId),
                        error -> Log.e("MINE", "Failed to send call notification: " + error.getMessage()));
    }

    public void sendOffer(JSONObject offer)
    {
        if (roomId != null) {
            m_callHubConn.send("SendOffer", roomId, offer.optString("sdp"));
        } else {
            Log.e("MINE", "Cannot send offer: roomId is null");
        }
    }


    public void sendAnswer(JSONObject answer) {
        if (roomId != null) {
            m_callHubConn.send("SendAnswer", roomId, answer.optString("sdp"));
        } else {
            Log.e("MINE", "Cannot send answer: roomId is null");
        }
    }

    public void sendIceCandidate(JSONObject candidate) {
        if (roomId != null) {
            m_callHubConn.send("SendIceCandidate",
                    roomId,
                    candidate.optString("candidate"),
                    candidate.optString("sdpMid"),
                    candidate.optInt("sdpMLineIndex"));
        } else {
            Log.e("MINE", "Cannot send ICE candidate: roomId is null");
        }
    }

    public void disconnect() {
        m_callHubConn.stop();
        m_notifHubConn.stop();
        listener.onDisconnected();
    }
}
