package org.maddev.helpers.walking;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.path.HpaPath;
import org.rspeer.runetek.api.movement.path.Path;
import org.rspeer.runetek.api.movement.path.PredefinedPath;
import org.rspeer.runetek.api.movement.pathfinding.executor.PathExecutor;
import org.rspeer.runetek.api.movement.position.Position;

import java.util.HashSet;

public class CustomWalker {

    private CustomPath currentCustomPath;
    private Path currentPath;
    private static HashSet<CustomPath> customPaths;

    public CustomWalker() {
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
}
