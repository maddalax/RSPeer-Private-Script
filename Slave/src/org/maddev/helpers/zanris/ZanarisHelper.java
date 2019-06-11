package org.maddev.helpers.zanris;

import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.SceneObjects;

public class ZanarisHelper {

    public static Position PURO_PURO_PEN = new Position(2427, 4445, 0);
    public static Position ZANARIS_START = new Position(2452, 4470, 0);
    public static Position BANK = new Position(2382, 4459, 0);
    public static Position HALF_WAY_BANK = new Position(2411, 4447);
    public static Position PURO_PURO = new Position(2594, 4318);

    public static boolean inZanaris() {
        return PURO_PURO_PEN.isLoaded() || BANK.isLoaded() || ZANARIS_START.isLoaded();
    }

    public static boolean hasRequiredItems() {
        return PlayerHelper.getTotalCount("Essence impling jar") >= 3
                && PlayerHelper.getTotalCount("Eclectic impling jar") >= 2
                && PlayerHelper.getTotalCount("Nature impling jar") >= 1;
    }

    public static boolean hasTeleports() {
        return PlayerHelper.hasAny("Varrock teleport") && PlayerHelper.hasAny("Lumbridge teleport");
    }

    public static boolean inPuroPuro() {
        return PURO_PURO.isLoaded();
    }

    public static boolean openZanarisBank() {
        SceneObject booth = SceneObjects.getNearest("Bank chest");
        if(booth != null) {
            InteractHelper.interact(booth, "Use");
            return Bank.isOpen();
        }
        if(!BANK.isLoaded()) {
            Logger.fine("Bank is not loaded, walking to puro puro pen.");
            MovementHelper.setWalkFlag(HALF_WAY_BANK.isLoaded() ? HALF_WAY_BANK : PURO_PURO_PEN);
            TimeHelper.sleep(450, 850);
            return false;
        }
        Logger.fine("Setting walk flag to bank.");
        Movement.setWalkFlag(BANK.randomize(1));
        TimeHelper.sleep(450, 850);
        return Bank.isOpen();
    }

    public static void goToZanaris(boolean useHomeTeleport) {
        Position finish = new Position(3200, 3169, 0);
        if(!finish.isLoaded() || finish.distance() > 10) {
            Store.setAction("Waiting to shed " + finish);
            MovementHelper.walkRandomized(finish, false, useHomeTeleport);
            TimeHelper.sleep(230, 450);
            return;
        }
        Logger.fine("Looking for Door to Zanaris.");
        SceneObject door = SceneObjects.getNearest("Door");
        if(door == null) {
            Store.setAction("Failed to find door to Zanaris.");
            return;
        }
        InteractHelper.interact(door, "Open");
        TimeHelper.sleep(350, 850);
    }

}
