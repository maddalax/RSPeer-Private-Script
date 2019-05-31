package org.maddev.helpers.walking;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Scene;

public class MovementUtil {

    public static Position getBest(Position[] positions, boolean acceptBlockedEnd) {
        int nearIndex = -1;
        int lastDistance = Integer.MAX_VALUE;
        for (int i = 0; i < positions.length; i++) {
            Position position = positions[i];
            double dist = position.distance();
            if (dist < lastDistance) {
                lastDistance = (int) dist;
                nearIndex = i;
            }
        }

        Position furthest = null;
        if (nearIndex != positions.length - 1) {
            furthest = positions[nearIndex + 1];
            for (int i = nearIndex; i < positions.length; i++) {
                Position position = positions[i];
                if (Scene.isLoaded(position) && Movement.isWalkable(position, acceptBlockedEnd)) {
                    if (position.distance() <= 5) {
                        continue;
                    }
                    furthest = position;
                }
            }
        }

        return furthest;
    }
}
