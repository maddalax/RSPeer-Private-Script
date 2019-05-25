package org.maddev;

public class Store {

    private static State state;

    public static void setState(State state) {
        Store.state = state;
    }

    public static State getState() {
        return state;
    }
}
