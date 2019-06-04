package org.maddev.helpers.log;

import com.google.gson.JsonObject;
import org.maddev.ws.WebSocket;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

public class Logger {

    private static final String EVENT = "client_log";

    public static void fine(String message) {
        Log.fine(message);
        WebSocket.getInstance().dispatch(EVENT, format(message, "fine", null));
    }

    public static void fine(String action, String message) {
        Log.fine(action, message);
        WebSocket.getInstance().dispatch(EVENT, format(message, "fine", action));
    }

    public static void severe(String message) {
        Log.severe(message);
        WebSocket.getInstance().dispatch(EVENT, format(message, "severe", null));
    }


    public static void severe(Exception message) {
        Log.severe(message);
        WebSocket.getInstance().dispatch(EVENT, format(message.toString(), "severe", null));
    }

    private static JsonObject format(String message, String type, String action) {
        JsonObject o = new JsonObject();
        if (Game.getClient() != null) {
            String name = Game.getClient().getUsername();
            o.addProperty("email", name);
            if (name != null) {
                Player player = Players.getLocal();
                if (player != null) {
                    o.addProperty("rsn", player.getName());
                }
            }
        }
        o.addProperty("message", message);
        o.addProperty("type", type);
        o.addProperty("action", action == null ? "regular" : action);
        o.addProperty("date", System.currentTimeMillis());
        return o;
    }
}
