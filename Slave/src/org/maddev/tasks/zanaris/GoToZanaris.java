package org.maddev.tasks.zanaris;

import org.maddev.Store;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.maddev.tasks.LostCity;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.script.task.Task;

public class GoToZanaris extends Task {

    @Override
    public boolean validate() {
        return LostCity.isComplete();
    }

    @Override
    public int execute() {
        Store.setTask("Going to Zanaris.");
        ZanarisHelper.goToZanaris(true);
        return Random.nextInt(100, 250);
    }
}
