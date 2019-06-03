package org.maddev.helpers.zanris;

import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.SceneObjects;

public class ZanarisHelper {

    public static void goToZanaris(boolean useHomeTeleport) {
        Position finish = new Position(3200, 3169, 0);
        if(!finish.isLoaded() || finish.distance() > 10) {
            MovementHelper.walkRandomized(finish, false, useHomeTeleport);
            Time.sleep(230, 450);
            return;
        }
        SceneObject door = SceneObjects.getNearest("Door");
        if(door == null) {
            Store.setAction("Failed to find door to Zanaris.");
            return;
        }
        InteractHelper.interact(door, "Open");
        Time.sleep(350, 850);
    }

}
