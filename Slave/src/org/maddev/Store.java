package org.maddev;

public class Store {

    private static State state;
    private static String status;

    public static void setState(State state) {
        Store.state = state;
    }

    public static State getState() {
        return state;
    }

    public static void setStatus(String status) {
        Store.status = status;
    }

    public static String getStatus() {
        return status;
    }
}
