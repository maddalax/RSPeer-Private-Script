package org.maddev;

import org.rspeer.ui.Log;

public class Store {

    private static State state;
    private static String task;
    private static String action;

    public static void setAction(String action) {
        if(action != null && !action.equals(Store.action)) {
            Log.fine("Setting action to: " + action + ".");
        }
        Store.action = action;
    }

    public static String getAction() {
        return action;
    }

    public static void setTask(String task) {
        if(task != null && !task.equals(Store.task)) {
            Log.fine("Setting task to: " + task + ".");
        }
        Store.task = task;
    }

    public static void setState(State state) {
        Store.state = state;
    }

    public static State getState() {
        return state;
    }

    public static String getTask() {
        return task;
    }
}
