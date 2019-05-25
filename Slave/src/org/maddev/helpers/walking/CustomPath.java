package org.maddev.helpers.walking;

import org.rspeer.runetek.api.movement.path.PredefinedPath;
import org.rspeer.runetek.api.movement.position.Position;

public abstract class CustomPath {

    private PredefinedPath path;

    void setPath(PredefinedPath path) {
        this.path = path;
    }

    PredefinedPath getPath() {
        return path;
    }

    public abstract boolean validate(Position end);

    public abstract Position[] getPositions();

    public abstract String getName();
}

