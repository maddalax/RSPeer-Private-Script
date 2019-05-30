package org.maddev.helpers.quest;

import org.rspeer.runetek.api.component.Interfaces;

public class QuestHelper {

    public static boolean isNameHidden() {
        return !Interfaces.getComponent(162, 49).isVisible();
    }
}
