package org.maddev;

import org.maddev.helpers.log.Logger;

public class Store {

    private static State state;
    private static String task;
    private static String action;

    public static void setAction(String action) {
        Store.action = action;
    }

    public static String getAction() {
        return action;
    }

    public static void setTask(String task) {
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
