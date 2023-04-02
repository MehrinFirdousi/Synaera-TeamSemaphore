package com.example.synaera;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
public class ServerClient {
    public static String TAG = "ServerClientDebug";

    private static String EVENT_AUTHENTICATION = "onAuthentication";
    private static String EVENT_RESPONSE = "onResponse";

    private static String EVENT_TRANSCRIPT = "onTranscriptGenerated";
    private Socket mSocket = null;
    private String mServerIp = "localhost";
    private int mServerPort = 8080;

    private String mUsername = null;
    private String mPassword = null;

    // A single callback to the client aimed to be registered by the current Activity
    private ServerResultCallback mSingleCallback = null;

    private static ServerClient mInstance = null;

    private ServerClient() {
        // Private constructor is part of singleton implementation
    }

    public synchronized static ServerClient getInstance() {
        if (mInstance == null) {
            mInstance = new ServerClient();
        }
        return mInstance;
    }

    public void init(String username, String password, String serverIp, int port) {
        mUsername = username;
        mPassword = password;
        mServerIp = serverIp;
        mServerPort = port;

        if (mSocket == null) {
            try {   // Try to create the socket with the server
                IO.Options options = new IO.Options();
                options.forceNew = true;
                options.multiplex = true;
                options.secure = true;
                options.reconnection = true;
                options.reconnectionDelay = 5000;
                options.reconnectionAttempts = 15;
                options.timeout = 20000;
//                options.timeout = 15000;
                String serverAddress = "http://" + mServerIp + ":" + mServerPort;
                mSocket = IO.socket(serverAddress, options);
                Log.d(TAG, "ServerClient initialized successfully.");
            } catch (URISyntaxException e) {
                // We failed to connect, consider to inform the user
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "ServerClient already initialized. Clearing sockets. Try again...");
            mSocket.disconnect();
            mSocket = null;
        }
    }

    public void registerCallback(ServerResultCallback callback) {
        mSingleCallback = callback;
    }

    public void unregisterCallback() {
        mSingleCallback = null;
    }

    /**
     * The connection to the server is explicitly issued by client activities.
     * <p>
     * Register the socket listeners just before trying to connect, so we can receive feedback
     * from the connection state.
     */
    public void connect() {
        if (mSocket != null && !mSocket.connected() && mUsername != null) {
            unregisterSocketListeners();
            registerSocketListeners();
            mSocket.connect();
        } else {
            Log.d(TAG, "Cannot connect because socket is null or already connected or username isn't defined.");
        }
    }

    /**
     * This is  main method issued by the client activity to stream pictures to the server.
     */
    public void sendImage(byte[] image) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("receiveImage", image, EVENT_RESPONSE);
        } else {
            Log.d(TAG, "Cannot send message because socket is null or disconnected");
        }
    }

    public void sendVideoFrame(byte[] image, int processedFrameCount) {
        boolean sent = false;
        int attempts = 0;

        while (!sent && attempts < 5) {
            if (mSocket != null && mSocket.connected()) {
                mSocket.emit("receiveVideoStream", image, processedFrameCount);
                sent = true;
            }
            else {
                Log.d(TAG, "Waiting for client to reconnect...");
                attempts++;
            }
        }
//        else {
//            Log.d(TAG, "Cannot send frame because socket is null or disconnected");
//        }
    }

    public void startTranscriptProcessing() {
        Log.d(TAG, "from startTranscriptProcessing: emitting processVideo");
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("processVideo");
        } else {
            Log.d(TAG, "Cannot get transcript because socket is null or disconnected");
        }
    }

    public void checkTranscript() {
        Log.d(TAG, "from checkTranscript: emitting checkTranscript");
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("checkTranscript", EVENT_TRANSCRIPT);
        } else {
            Log.d(TAG, "Cannot check transcript because socket is null or disconnected");
        }
    }
    public void getPrediction() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("stopRecord", EVENT_RESPONSE);
        } else {
            Log.d(TAG, "Cannot get prediction because socket is null or disconnected");
        }
    }


    /**
     * Client activities might issue an explicit disconnect at anytime.
     * <p>
     * Unregister the socket listeners after issuing the disconnect to free resources.
     */
    public void disconnect() {
        if (mSocket != null) {
            Log.d(TAG, "randomly disconnected :(");
            mSocket.disconnect();
            unregisterSocketListeners();
        } else {
            Log.d(TAG, "Cannot disconnect because socket is null.");
        }
    }

    private void registerSocketListeners() {
        if (mSocket != null) {
            mSocket.on(Socket.EVENT_CONNECT, onConnected);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
            mSocket.on(Socket.EVENT_RECONNECT, onReconnecting);
            mSocket.on(Socket.EVENT_RECONNECT_ERROR, onReconnecting);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnected);
            mSocket.on(EVENT_AUTHENTICATION, onAuthentication);
            mSocket.on(EVENT_RESPONSE, onResponse);
            mSocket.on(EVENT_TRANSCRIPT, onTranscriptGenerated);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
            mSocket.on(Socket.EVENT_ERROR, onEventError);
        } else {
            Log.d(TAG, "Cannot register listeners because socket is null.");
        }
    }

    private void unregisterSocketListeners() {
        if (mSocket != null) {
            mSocket.off(Socket.EVENT_CONNECT, onConnected);
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectionError);
            mSocket.off(Socket.EVENT_RECONNECT, onReconnecting);
            mSocket.off(Socket.EVENT_RECONNECT_ERROR, onReconnecting);
            mSocket.off(Socket.EVENT_DISCONNECT, onDisconnected);
            mSocket.off(EVENT_AUTHENTICATION, onAuthentication);
            mSocket.off(EVENT_RESPONSE, onResponse);
            mSocket.off(EVENT_TRANSCRIPT, onTranscriptGenerated);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
            mSocket.off(Socket.EVENT_ERROR, onEventError);
        } else {
            Log.d(TAG, "Cannot unregister listeners because socket is null.");
        }
    }

    /**
     * Callback functions for the socket listeners
     */
    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // We connected to the server successfully
            Log.d(TAG, "Connected to the server! Starting authentication...");
            mSocket.emit("authenticate", mUsername, mPassword, EVENT_AUTHENTICATION);
        }
    };

    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // We got an error while trying to connect
            // The socket will try to reconnect automatically as many times we set on the options
            String reason = "no reason received.";
            if (args.length > 0) {
                reason = args[0].toString();
            }
            Log.d(TAG, "Error while trying to connect: " + reason);
        }
    };

    private Emitter.Listener onReconnecting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // Socket is trying to reconnect automatically
            Log.d(TAG, "Reconnecting to the server...");
        }
    };

    private Emitter.Listener onReconnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // We fail to reconnect
            String reason = "no reason received.";
            if (args.length > 0) {
                reason = args[0].toString();
            }
            Log.d(TAG, "Reconnection failed: " + reason);
        }
    };

    private Emitter.Listener onDisconnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // We were disconnected from the server
            String reason = "no reason received.";
            if (args.length > 0) {
                reason = args[0].toString();
            }
            Log.d(TAG, "Disconnected from the server: " + reason);
        }
    };

    private Emitter.Listener onAuthentication = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            boolean result = (boolean) args[0];
            if(mSingleCallback != null) {
                mSingleCallback.onConnected(result);
            }
            Log.d(TAG, "onAuthentication: " + result);
        }
    };

    private Emitter.Listener onResponse = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "len of args = "+args.length);
            JSONObject data = (JSONObject) args[0];
            String result = "";
            boolean isGloss = false;
            try {
                if (data.has("result")) {
                    result = data.getString("result");
                }
                if (data.has("isGloss")) {
                    isGloss = data.getBoolean("isGloss");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mSingleCallback != null)
                mSingleCallback.displayResponse(result, isGloss);
            Log.d(TAG, "onResponse: " + result);
        }
    };

    private Emitter.Listener onTranscriptGenerated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String result = (String)args[0];
            if (mSingleCallback != null)
                mSingleCallback.addNewTranscript(result);
            Log.d(TAG, "onTranscriptGenerated: " + result);
        }
    };

    private Emitter.Listener onTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String reason = "no reason receiveddd";
            if (args.length > 0) {
                reason = args[0].toString();
            }
            Log.d(TAG, "Connection timed out! Reason: " + reason);
        }
    };

    private Emitter.Listener onEventError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // Something went wrong with an event
            String reason = "no reason received.";
            if (args.length > 0) {
                reason = args[0].toString();
            }
            Log.d(TAG, "Something went wrong with the last event: " + reason);
        }
    };
}
