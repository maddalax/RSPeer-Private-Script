package org.maddev.helpers.bank;

import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;

public class BankHelper {

    public static boolean open() {
        BankLocation nearest = BankLocation.getNearest();
        if(nearest == null) {
            return false;
        }
        if(nearest.getPosition().distance() > 5) {
            MovementHelper.walkRandomized(nearest.getPosition(), false);
            Time.sleep(350, 650);
            return false;
        }
        Bank.open(nearest);
        Time.sleep(350, 650);
        return Bank.isOpen();
    }

    public static boolean withdraw(String item, int quantity) {
        if(!Bank.isOpen()) {
            open();
            return false;
        }
        Bank.withdraw(item, quantity);
        Time.sleep(490, 759);
        return Inventory.contains(item);
    }

    public static boolean withdrawAll(String item) {
        if(!Bank.isOpen()) {
            open();
            return false;
        }
        Bank.withdrawAll(item);
        Time.sleep(490, 759);
        return Inventory.contains(item);
    }

}
