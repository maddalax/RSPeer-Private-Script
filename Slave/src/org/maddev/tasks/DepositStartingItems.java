package org.maddev.tasks;

import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;

public class DepositStartingItems extends Task {

    @Override
    public boolean validate() {
        return Inventory.containsAll("Tinderbox", "Small fishing net", "Bucket");
    }

    @Override
    public int execute() {
        Store.setTask("Depositing Starting Items.");
        if(!Bank.isOpen()) {
            Store.setAction("Opening bank.");
            BankHelper.open(BankLocation.GRAND_EXCHANGE);
            return Random.nextInt(550, 1150);
        }
        Bank.depositInventory();
        return Random.nextInt(550, 1150);
    }
}
