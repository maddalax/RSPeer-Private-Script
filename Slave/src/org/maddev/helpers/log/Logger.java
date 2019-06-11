package org.maddev.helpers.log;

import org.rspeer.ui.Log;

public class Logger {

    public static void fine(String message) {
        Log.fine(message);
        System.out.println(message);
        //WebSocket.getInstance().dispatch(EVENT, format(message, "fine", null));
    }

    public static void info(String message) {
        Log.info(message);
    }

    public static void info(String action, String message) {
        Log.info(action, message);
        System.out.println(action + " | " + message);
    }

    public static void fine(String action, String message) {
        Log.fine(action, message);
        System.out.println(action + " | " + message);
        //WebSocket.getInstance().dispatch(EVENT, format(message, "fine", action));
    }

    public static void severe(String message) {
        Log.severe(message);
        System.err.println("SEVERE !!! " + " | " + message);
        //WebSocket.getInstance().dispatch(EVENT, format(message, "severe", null));
    }


    public static void severe(Exception message) {
        message.printStackTrace();
        Log.severe(message);
        //WebSocket.getInstance().dispatch(EVENT, format(message.toString(), "severe", null));
    }

}
