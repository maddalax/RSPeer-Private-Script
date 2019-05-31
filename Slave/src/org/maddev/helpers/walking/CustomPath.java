package org.maddev.helpers.walking;

import org.rspeer.runetek.api.movement.path.PredefinedPath;
import org.rspeer.runetek.api.movement.position.Position;

public abstract class CustomPath {

    private PredefinedPath path;
    private boolean walkedtoStart;

    public void setWalkedtoStart(boolean walkedtoStart) {
        this.walkedtoStart = walkedtoStart;
    }

    public boolean didWalkToStart() {
        return walkedtoStart;
    }

    void setPath(PredefinedPath path) {
        this.path = path;
    }

    PredefinedPath getPath() {
        return path;
    }

    public abstract boolean validate(Position end);

    public abstract Position[] getPositions();

    public abstract String getName();

    public abstract Position startPosition();

}

