package org.maddev.tasks;

import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.script.task.Task;

public class WalkTest extends Task {
    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public int execute() {
        MovementHelper.walkRandomized(BankLocation.DRAYNOR.getPosition(), false);
        return Random.nextInt(450, 650);
    }
}
