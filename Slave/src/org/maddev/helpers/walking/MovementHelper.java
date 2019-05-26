package org.maddev.helpers.walking;

import org.rspeer.runetek.api.movement.position.Position;

public class MovementHelper {

    private static CustomWalker instance;

    private static CustomWalker getInstance() {
        if(instance == null) {
            instance = new CustomWalker();
        }
        return instance;
    }

    public static void addCustomPath(CustomPath path) {
        getInstance().addCustomPath(path);
    }

    public static CustomPath getCustomPath() {
        return getInstance().getCurrentCustomPath();
    }

    public static boolean walk(Position p, boolean acceptEndBlocked) {
        return getInstance().walk(p, acceptEndBlocked);
    }

    public static boolean walkRandomized(Position p, boolean acceptEndBlocked, boolean useHomeTeleport) {
        return getInstance().walkRandomized(p, acceptEndBlocked, useHomeTeleport);
    }

    public static boolean walkRandomized(Position p, boolean acceptEndBlocked) {
        return getInstance().walkRandomized(p, acceptEndBlocked, false);
    }

    public static void stopCustomPath() {
        getInstance().stopCustomPath();
    }
}
