package org.maddev.helpers.interact;

import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;

public class InteractHelper {

    public static boolean interact(Interactable interactable, String action) {
        if(interactable == null) {
            return false;
        }
        if(Movement.isDestinationSet() && Movement.getDestinationDistance() > 2) {
            Time.sleep(450, 850);
            return false;
        }
        boolean result = action == null ? interactable.click() : interactable.interact(action);
        Time.sleep(450, 950);
        return result;
    }

    public static boolean interact(Interactable interactable) {
       return interact(interactable, null);
    }

}
