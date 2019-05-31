package org.maddev.helpers.walking.paths;

import org.maddev.helpers.walking.CustomPath;
import org.rspeer.runetek.api.movement.position.Position;

public class HuntingHillGiants extends CustomPath {

    private Position location = new Position(2386, 3394);

    private Position[] path = {
                new Position(2386, 3405, 0),
                new Position(2376, 3409, 0),
                new Position(2373, 3416, 0),
                new Position(2366, 3423, 0),
                new Position(2367, 3430, 0),
                new Position(2368, 3438, 0)
    };

    @Override
    public boolean validate(Position end) {
        return location.isLoaded();
    }

    @Override
    public Position[] getPositions() {
        return path;
    }

    @Override
    public String getName() {
        return "Bypass Hill Giants";
    }

    @Override
    public Position startPosition() {
        return new Position(2386, 3405, 0);
    }
}
