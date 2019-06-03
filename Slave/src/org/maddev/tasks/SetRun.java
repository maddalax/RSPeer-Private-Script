package org.maddev.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;

public class SetRun extends Task {

    private int threshold = Random.nextInt(45, 65);

    @Override
    public boolean validate() {
        if(Players.getLocal().isHealthBarVisible() && Players.getLocal().isMoving() && !Movement.isRunEnabled()) {
            return true;
        }
        return Movement.getRunEnergy() > threshold && !Movement.isRunEnabled();
    }

    @Override
    public int execute() {
        if(Movement.toggleRun(true)) {
            threshold = Random.nextInt(45, 65);
        }
        Time.sleepUntil(Movement::isRunEnabled, 1000);
        return Random.nextInt(250, 450);
    }
}
