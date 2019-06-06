package org.maddev.helpers.walking;

import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public class MovementUtil {

    public static boolean applyLumbridgeFix() {
        Log.fine("Applying lumbridge fix.");
        if(Players.getLocal().getPosition().getFloorLevel() == 0) {
            SceneObject stairs = SceneObjects.getFirstAt(new Position(3204, 3229));
            if (stairs == null || stairs.distance() > 10) {
                return MovementHelper.walkRandomized(new Position(3207, 3228, 0), false, true);
            }
            InteractHelper.interact(stairs, "Climb-up");
            return false;
        }
        if(Players.getLocal().getPosition().getFloorLevel() == 1) {
            SceneObject stairs = SceneObjects.getFirstAt(new Position(3204, 3207, 1));
            SceneObject door = SceneObjects.getFirstAt(new Position(3207, 3214, 1));
            if (door != null && stairs != null && !Movement.isInteractable(stairs, false)) {
                Store.setAction("Opening door to wheel.");
                InteractHelper.interact(door);
                return false;
            }
            Store.setAction("Going up stairs.");
            if(stairs == null) {
                Store.setAction("Failed to find stairs.");
                return false;
            }
            InteractHelper.interact(stairs, "Climb-up");
            return false;
        }
        if(Players.getLocal().getPosition().getFloorLevel() == 2) {
            SceneObject booth = SceneObjects.getNearest("Bank booth");
            if(booth == null) {
                Store.setAction("Failed to find booth.");
                return false;
            }
            InteractHelper.interact(booth, "Bank");
            Time.sleep(100, 250);
        }
        return Bank.isOpen();
    }
}
