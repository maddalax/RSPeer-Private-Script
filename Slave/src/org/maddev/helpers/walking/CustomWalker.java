package org.maddev.helpers.walking;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.time.TimeHelper;
import org.maddev.web.dax.DaxWeb;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.path.Path;
import org.rspeer.runetek.api.movement.pathfinding.executor.PathExecutor;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.ui.Log;

import java.util.HashSet;

public class CustomWalker implements ChatMessageListener  {

    private CustomPath currentCustomPath;
    private Path currentPath;
    private static HashSet<CustomPath> customPaths;
    private static long timeTillTeleport;
    private static final Position LUMBRIDGE_TILE = new Position(3220, 3218, 0);
    private static boolean shouldWalk;

    public boolean canUseHomeTeleport() {
     return timeTillTeleport < System.currentTimeMillis();
    }

    public boolean isShouldWalk() {
        return shouldWalk;
    }

    public CustomWalker() {
        Game.getEventDispatcher().register(this);
        customPaths = new HashSet<>();
    }

    public CustomWalker addCustomPath(CustomPath path) {
        customPaths.add(path);
        return this;
    }

    public CustomWalker removeCustomPath(CustomPath path) {
        customPaths.remove(path);
        return this;
    }

    public void stopCustomPath() {
        currentCustomPath = null;
    }

    public CustomPath getCurrentCustomPath() {
        return currentCustomPath;
    }

    public boolean walkRandomized(Position p, boolean acceptEndBlocked) {
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(true);
        return walk(p, acceptEndBlocked);
    }

    public boolean walkRandomized(Position p, boolean acceptEndBlocked, boolean useHomeTeleport) {
        if(useHomeTeleport && canUseHomeTeleport()) {
            useHomeTeleport(p);
        }
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(true);
        return walk(p, acceptEndBlocked);
    }

    public boolean walk(Position p, boolean acceptEndBlocked) {
        if(!shouldWalk()) {
            return false;
        }
        boolean usedDaxWeb = false;
        if(Players.getLocal().getPosition().getFloorLevel() == p.getFloorLevel()) {
            Path bPath = DaxWeb.build(p);
            if(bPath != null) {
                usedDaxWeb = true;
                currentPath = bPath;
            }
        }
        if(!usedDaxWeb) {
            log("Dax path failed, using regular web.");
            currentPath = Movement.buildPath(p);
        }
        CustomWalker.shouldWalk = shouldWalk();
        if(currentPath == null) {
            Logger.severe("Failed to generate path to " + p);
            return false;
        }
        return !shouldWalk || PathExecutor.getPathExecutorSupplier().get().execute(currentPath);
    }

    private boolean shouldWalk() {
        if(Players.getLocal().isAnimating()) {
            log("We are animating, should not move.");
            return false;
        }
        if(!Players.getLocal().isMoving()) {
            log("Not moving, we can walk.");
            return true;
        }
        if(Movement.isDestinationSet() && !Movement.isWalkable(Movement.getDestination(), false)) {
            log("Current destination is not walkable, we can walk.");
            return true;
        }
        boolean set = Movement.isDestinationSet();
        if(!set) {
            log("Destination is not set. We can walk.");
            return true;
        }
        int threshold = Random.nextInt(2, 7);
        boolean distance = Movement.getDestinationDistance() < threshold;
        if(distance) {
            log("Distance is less than " + threshold + " . We can walk.");
            return true;
        }
        log("We cannot walk right now. " + " Is Moving: " + Players.getLocal().isMoving());
        return false;
    }

    private void useHomeTeleport(Position dest) {
        log("Checking home teleport.");
        if(Players.getLocal().isAnimating()) {
            log("Currently animating, skipping home teleport.");
            return;
        }
        if(dest.distance() < 150) {
            log("Distance is less than 150, no need for home teleport.");
            return;
        }
        if(LUMBRIDGE_TILE.distance() < dest.distance()) {
            log("Destination is farther than lumby tile, teleporting.");
            return;
        }
        if(Magic.canCast(Spell.Modern.HOME_TELEPORT)) {
            log("Can not cast home teleport.");
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), 3500);
            return;
        }
        return;
    }

    private void log(String message) {
        Logger.fine("Movement", message);
    }

    @Override
    public void notify(ChatMessageEvent e) {
        try {
            Logger.fine("ChatMessageEvent", e.getMessage());
            if (Store.getState() == State.SCRIPT_STOPPED) {
                Game.getEventDispatcher().deregister(this);
            }
            if (e.getType() == ChatMessageType.PUBLIC) {
                return;
            }
            if (e.getMessage().contains("You need to wait another ")) {
                if(e.getMessage().contains("another minute")) {
                    timeTillTeleport = System.currentTimeMillis() + (60000);
                    Logger.fine("Can't teleport for at-least " + 1 + " minute.");
                    return;
                }
                int minutes = Integer.parseInt(e.getMessage().replace("You need to wait another", "")
                        .replace("minutes to cast this spell.", "").trim());
                timeTillTeleport = System.currentTimeMillis() + (minutes * 60000);
                Logger.fine("Can't teleport for at-least " + minutes + " minutes.");
            }
        } catch (Exception ex) {
            Logger.severe(ex.getMessage());
        }
    }
}
