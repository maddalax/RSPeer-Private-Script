package org.maddev.helpers.walking;

import org.maddev.helpers.interact.InteractHelper;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public class MovementUtil {

    public static boolean applyLumbridgeFix() {
        SceneObject stairs = SceneObjects.getFirstAt(new Position(3204, 3229));
        if(stairs == null || stairs.distance() > 10) {
            return MovementHelper.walkRandomized(new Position(3207, 3228, 0), false, true);
        }
        InteractHelper.interact(stairs, "Climb-up");
        return Players.getLocal().getPosition().getFloorLevel() != 0;
    }
}
