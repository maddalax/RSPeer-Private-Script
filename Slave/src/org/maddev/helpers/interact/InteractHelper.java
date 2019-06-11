package org.maddev.helpers.interact;

import org.maddev.helpers.log.Logger;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;

public class InteractHelper {

    public static boolean interact(Interactable interactable, String action) {
        Logger.fine("Attempting to interact with interactable for action: " + action);
        if(interactable == null) {
            Logger.severe("Interactable was null, unable to interact for action: " + action);
            return false;
        }
        if(Players.getLocal().isAnimating()) {
            Logger.fine("Currently animating, waiting until finish before interacting.");
            return false;
        }
        if(Players.getLocal().isMoving() && Movement.isDestinationSet() && Movement.getDestinationDistance() > 2) {
            Logger.fine("We are moving and our destination distance > 2. Skipping interact for action: " + action);
            TimeHelper.sleep(450, 850);
            return false;
        }
        boolean result = action == null ? interactable.click() : interactable.interact(action);
        if(!result) {
            Logger.info("Interaction failed for action " + action + ".");
        }
        TimeHelper.sleep(450, 950);
        if(Movement.isDestinationSet() && Movement.getDestinationDistance() <= 2) {
            Logger.fine("Sleeping until player is animating.");
            TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), Random.nextInt(1550, 2350));
        }
        return result;
    }

    public static boolean interact(Interactable interactable) {
       return interact(interactable, null);
    }

}
