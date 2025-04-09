package com.eliza.messenger.webrtc;

import org.webrtc.*;
import java.util.List;
import android.os.Looper;
import android.os.Handler;
import java.util.ArrayList;
import android.content.Context;
import org.webrtc.PeerConnection;
import com.eliza.messenger.model.Call;
import java.util.Arrays;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PeerConnection;
import org.webrtc.MediaConstraints;
import org.webrtc.SessionDescription;
import org.webrtc.SdpObserver;
import org.webrtc.PeerConnection.Observer;

public class WebRTCClient implements PeerConnection.Observer {
    private final Context context;
    private final String roomId;
    private final String userId;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private WebSocketClient webSocketClient;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebRTCInitializationCallback initializationCallback;

    public interface WebRTCInitializationCallback {
        void onInitializationSuccess();
        void onInitializationFailed(Exception error);
    }

    public WebRTCClient(Context context, String roomId, String userId) {
        this.context = context;
        this.roomId = roomId;
        this.userId = userId;
        initializePeerConnectionFactory();
    }

    public void setInitializationCallback(WebRTCInitializationCallback callback) {
        this.initializationCallback = callback;
    }

    private void initializePeerConnectionFactory() {
        new Thread(() -> {
            try {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions());

                factory = PeerConnectionFactory.builder()
                    .setOptions(new PeerConnectionFactory.Options())
                    .createPeerConnectionFactory();
                
                // Notify on main thread when initialization is complete
                handler.post(() -> {
                    if (initializationCallback != null) {
                        initializationCallback.onInitializationSuccess();
                    }
                });
            } catch (Exception e) {
                // Handle initialization errors
                e.printStackTrace();
                handler.post(() -> {
                    if (initializationCallback != null) {
                        initializationCallback.onInitializationFailed(e);
                    }
                });
            }
        }).start();
    }

    public void createOffer() {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(createIceServers());
        peerConnection = factory.createPeerConnection(rtcConfig, new MediaConstraints(), this);

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sdp);
                sendSignalingMessage(new Call(
                        userId,           // id
                        userId,           // callerId
                        userId,          // calleeId
                        roomId,          // roomId
                        sdp.description, // sdp
                        "offer",         // type
                        Call.Status.ONGOING, // status
                        true             // isVideoCall
                ));
            }
            @Override
            public void onSetSuccess() {
                // Handle success
            }

            @Override
            public void onCreateFailure(String error) {
                // Handle failure
            }

            @Override
            public void onSetFailure(String error) {
                // Handle failure
            }
        }, new MediaConstraints());
    }

    private List<PeerConnection.IceServer> createIceServers() {
        return Arrays.asList(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        );
    }

    private static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {}
        @Override
        public void onSetSuccess() {}
        @Override
        public void onCreateFailure(String s) {}
        @Override
        public void onSetFailure(String s) {}
    }

    private void sendSignalingMessage(String userId, String roomId, SessionDescription sdp) {
        Call call = new Call(
                userId,           // id
                userId,           // callerId
                roomId,          // calleeId
                roomId,          // roomId
                sdp.description, // sdp
                "offer",         // type
                Call.Status.ONGOING, // status
                true             // isVideoCall
        );
        sendSignalingMessage(call);
    }

    private void sendSignalingMessage(Call call) {
        webSocketClient.send("/app/call/init", call);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {}

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState state) {}

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState state) {}
    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {}
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState state) {}
    @Override
    public void onAddStream(MediaStream stream) {}
    @Override
    public void onRemoveStream(MediaStream stream) {}
    @Override
    public void onDataChannel(DataChannel channel) {}
    @Override
    public void onRenegotiationNeeded() {}
    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {}
}