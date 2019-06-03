package org.maddev.helpers.zanris;

import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.SceneObjects;

public class ZanarisHelper {

    public static Position PURO_PURO_PEN = new Position(2427, 4445, 0);
    public static Position ZANARIS_START = new Position(2452, 4470, 0);
    public static Position BANK = new Position(2382, 4459, 0);
    public static Position HALF_WAY_BANK = new Position(2398, 4446);
    public static Position PURO_PURO = new Position(2594, 4318);

    public static boolean inZanaris() {
        return PURO_PURO_PEN.isLoaded() || BANK.isLoaded() || ZANARIS_START.isLoaded();
    }

    public static boolean inPuroPuro() {
        return PURO_PURO.isLoaded();
    }

    public static boolean openZanarisBank() {
        int[] choices = new int[]{4458, 4456, 4457, 4459, 4460};
        SceneObject booth = SceneObjects.getFirstAt(new Position(2380,  Random.nextElement(choices), 0));
        if(booth != null && booth.distance() < 10) {
            InteractHelper.interact(booth, "Use");
            return Bank.isOpen();
        }
        if(!BANK.isLoaded()) {
            Movement.setWalkFlagWithConfirm(HALF_WAY_BANK.isLoaded() ? HALF_WAY_BANK : PURO_PURO_PEN);
            Time.sleep(450, 850);
            return false;
        }
        Movement.setWalkFlagWithConfirm(BANK.randomize(1));
        Time.sleep(450, 850);
        return Bank.isOpen();
    }

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
