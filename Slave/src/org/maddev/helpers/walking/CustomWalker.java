package org.maddev.helpers.walking;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.web.dax.DaxWeb;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.path.HpaPath;
import org.rspeer.runetek.api.movement.path.Path;
import org.rspeer.runetek.api.movement.path.PredefinedPath;
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
    private long timeTillTeleport;
    private static final Position LUMBRIDGE_TILE = new Position(3220, 3218, 0);

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
        Log.fine("Walking to " + p.toString() + " Distance: " + p.distance());
        if(useHomeTeleport && timeTillTeleport < System.currentTimeMillis()) {
            if(!useHomeTeleport(p)) {
                return false;
            }
        }
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(true);
        return walk(p, acceptEndBlocked);
    }

    public boolean walk(Position p, boolean acceptEndBlocked) {
        boolean usedDaxWeb = false;
        if(Players.getLocal().getPosition().getFloorLevel() == p.getFloorLevel()) {
            Log.fine("Walking to same floor level, attempting dax web first.");
            Path bPath = DaxWeb.build(p);
            if(bPath != null) {
                usedDaxWeb = true;
                currentPath = bPath;
            }
        }
        if(!usedDaxWeb) {
            Log.fine("Did not use Dax Path.");
            currentPath = Movement.buildPath(p);
        }
        boolean shouldWalk = shouldWalk();
        Log.fine("Should walk: " + shouldWalk);
        if(currentPath == null) {
            Log.severe("Failed to generate path to " + p);
            return false;
        }
        return !shouldWalk || PathExecutor.getPathExecutorSupplier().get().execute(currentPath);
    }

    private boolean shouldWalk() {
        if(!Players.getLocal().isMoving()) {
            return true;
        }
        if(Movement.isDestinationSet() && !Movement.isWalkable(Movement.getDestination())) {
            return true;
        }
       return !Movement.isDestinationSet() || Movement.getDestinationDistance() < 6;
    }

    public boolean isWalkingCustomPath(CustomPath path) {
        if(path != null && !path.didWalkToStart() && path.startPosition() != null) {
            return true;
        }
        return path != null && path.getPath() != null
                && path.getPath().getCurrent() != null;
    }

    private boolean useHomeTeleport(Position dest) {
        if(Players.getLocal().isAnimating()) {
            return false;
        }
        if(dest.distance() < 150) {
            return true;
        }
        if(dest.getFloorLevel() == 0 && LUMBRIDGE_TILE.distance() > dest.distance()) {
            return true;
        }
        if(Magic.canCast(Spell.Modern.HOME_TELEPORT)) {
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            Time.sleepUntil(() -> Players.getLocal().isAnimating(), 3500);
            return false;
        }
        return true;
    }

    @Override
    public void notify(ChatMessageEvent e) {
        try {
            if (Store.getState() == State.SCRIPT_STOPPED) {
                Game.getEventDispatcher().deregister(this);
            }
            if (e.getType() != ChatMessageType.GAME && e.getType() != ChatMessageType.SERVER) {
                return;
            }
            Log.fine(e.getMessage());
            if (e.getMessage().contains("You need to wait another ")) {
                if(e.getMessage().contains("another minute")) {
                    timeTillTeleport = System.currentTimeMillis() + (60000);
                    Log.fine("Can't teleport for at-least " + 1 + " minute.");
                    return;
                }
                int minutes = Integer.parseInt(e.getMessage().replace("You need to wait another", "")
                        .replace("minutes to cast this spell.", "").trim());
                timeTillTeleport = System.currentTimeMillis() + (minutes * 60000);
                Log.fine("Can't teleport for at-least " + minutes + " minutes.");
            }
        } catch (Exception ex) {
            Log.severe(ex.getMessage());
        }
    }
}
