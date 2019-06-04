package org.maddev.ws;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;

public class WebSocket {

    private static WebSocket instance;
    private HashMap<String, HashSet<MessageCallback>> listeners;
    private int messagesRecieved;
    private int messagesSent;

    public static WebSocket getInstance() {
        if(instance == null) {
            instance = new WebSocket();
        }
        return instance;
    }

    private Socket socket;
    private Gson g;

    public WebSocket() {
        g = new Gson();
        listeners = new HashMap<>();
        try {
            socket = IO.socket("http://localhost:4567");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void registerListener(String name, MessageCallback callback) {
        if(listeners.containsKey(name)) {
            HashSet<MessageCallback> current = listeners.get(name);
            current.add(callback);
            return;
        }
        HashSet<MessageCallback> first = new HashSet<>();
        first.add(callback);
        listeners.put(name, first);
        socket.on(name, (Object ... data) -> {
            messagesRecieved++;
            listeners.get(name).forEach(c -> {
                c.onMessage(data.length == 0 ? null : data[0]);
            });
        });
    }

    public void dispose() {
        socket.disconnect();
        socket = null;
    }

    public void connect() {
        socket.connect();
    }

    public boolean isConnected() {
        if(socket == null) {
            return false;
        }
        return socket.connected();
    }

    public int getMessagesRecieved() {
        return messagesRecieved;
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public void dispatch(String event, Object data) {
        if(socket == null) {
            return;
        }
        socket.emit(event, g.toJson(data));
        messagesSent++;
    }

    public static void main(String[] args) throws URISyntaxException {
        Socket socket = IO.socket("http://localhost:3000");
        socket.on(Socket.EVENT_CONNECT, objects -> System.out.println("Connected."));
        socket.connect();
    }

}
