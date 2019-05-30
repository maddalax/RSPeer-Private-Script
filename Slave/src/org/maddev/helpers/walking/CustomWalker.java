package org.maddev.helpers.walking;

import org.maddev.State;
import org.maddev.Store;
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
        Log.fine("Walking to " + p.toString());
        Store.setStatus("Destination distance: " + p.distance());
        if(useHomeTeleport && timeTillTeleport < System.currentTimeMillis()) {
            if(!useHomeTeleport(p)) {
                return false;
            }
        }
        PathExecutor.getPathExecutorSupplier().get().setRandomizeAll(true);
        return walk(p, acceptEndBlocked);
    }

    public boolean walk(Position p, boolean acceptEndBlocked) {

        if(isWalkingCustomPath(currentCustomPath)) {
            return shouldWalk() || currentCustomPath.getPath().walk(acceptEndBlocked);
        }

        CustomPath temp = null;
        for (CustomPath customPath : customPaths) {
            if(!customPath.validate(p)) {
                continue;
            }
            temp = customPath;
            temp.setPath(PredefinedPath.build(customPath.getPositions()));
            temp.getPath().walk(acceptEndBlocked);
            break;
        }

        /*
          Once finished with a custom path, we must clear the HPA cache for our position
          so it doesn't try to pickup where we previously were before the custom path.
         */
        if(!isWalkingCustomPath(temp)) {
            if(currentCustomPath != null) {
                currentCustomPath = null;
                if(currentPath != null && currentPath instanceof HpaPath) {
                    ((HpaPath) currentPath).decache();
                }
            }
        }

        else if(isWalkingCustomPath(temp)) {
            currentCustomPath = temp;
            return true;
        }

        currentPath = Movement.buildPath(p);
        return shouldWalk() || PathExecutor.getPathExecutorSupplier().get().execute(currentPath);
    }

    private boolean shouldWalk() {
       return Movement.isDestinationSet() && Movement.getDestinationDistance() > 3;
    }

    public boolean isWalkingCustomPath(CustomPath path) {
        return path != null && path.getPath() != null
                && path.getPath().getCurrent() != null;
    }

    private boolean useHomeTeleport(Position dest) {
        if(Players.getLocal().isAnimating()) {
            return false;
        }
        if(dest.distance() < 50) {
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
        if(Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
        }
        if(e.getType() != ChatMessageType.GAME && e.getType() != ChatMessageType.SERVER) {
            return;
        }
        Log.fine(e.getMessage());
        if(e.getMessage().contains("You need to wait another ")) {
            int minutes = Integer.parseInt(e.getMessage().replace("You need to wait another", "")
                    .replace("minutes to cast this spell.", "").trim());
            timeTillTeleport = System.currentTimeMillis() + (minutes * 60000);
            Log.fine("Can't teleport for at-least " + minutes + " minutes.");
        }
    }
}
