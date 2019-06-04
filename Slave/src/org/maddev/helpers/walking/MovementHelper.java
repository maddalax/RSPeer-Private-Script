package org.maddev.helpers.walking;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.pathfinding.executor.PathExecutor;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;

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
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(false);
        return getInstance().walk(p, acceptEndBlocked);
    }

    public static boolean setWalkFlag(Position p) {
        if(Movement.isDestinationSet() && Movement.getDestination().equals(p)) {
            return false;
        }
        Movement.setWalkFlag(p);
        Time.sleep(650, 1120);
        return Players.getLocal().getPosition().equals(p);
    }

    public static boolean walkRandomized(Position p, boolean acceptEndBlocked, boolean useHomeTeleport) {
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(true);
        return getInstance().walkRandomized(p, acceptEndBlocked, useHomeTeleport);
    }

    public static boolean walkRandomized(Position p, boolean acceptEndBlocked) {
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(true);
        return getInstance().walkRandomized(p, acceptEndBlocked, false);
    }

    public static void stopCustomPath() {
        getInstance().stopCustomPath();
    }
}
