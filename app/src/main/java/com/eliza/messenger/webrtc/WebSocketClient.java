package com.eliza.messenger.webrtc;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.WebSocketListener;

public class WebSocketClient {
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final String userId;
    private final String serverUrl = "ws://your-backend-url/ws";
    private final Gson gson = new Gson();

    public WebSocketClient(String userId, WebSocket webSocket) {
        this.userId = userId;
        this.client = new OkHttpClient();
        this.webSocket = webSocket;
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(serverUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Handle incoming signaling messages
            }

            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // Handle connection failure
            }
        });
    }

    public void send(String endpoint, Object message) {
        String json = gson.toJson(message);
        webSocket.send(json);
    }
}