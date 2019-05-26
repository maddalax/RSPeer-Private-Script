package org.maddev.helpers.bank;

import org.maddev.Store;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;

public class BankHelper {

    public static boolean open() {
        return open(BankLocation.getNearest());
    }

    public static boolean open(BankLocation location) {
       return open(location, false);
    }

    public static boolean open(BankLocation location, boolean useHomeTeleport) {
        if(location == null) {
            return false;
        }
        if(location.getPosition().distance() > 5) {
            MovementHelper.walkRandomized(location.getPosition(), false, useHomeTeleport);
            Time.sleep(350, 650);
            return false;
        }
        Bank.open(location);
        Time.sleep(350, 650);
        return Bank.isOpen();
    }

    public static boolean withdraw(ItemPair ... pair) {
        for (ItemPair item : pair) {
            boolean isAll = item.getQuantity() == Integer.MAX_VALUE;
            if(!(isAll ? withdrawAll(item.getName()) : withdraw(item.getName(), item.getQuantity()))) {
                return false;
            }
        }
        return true;
    }

    public static boolean withdraw(String item, int quantity) {
        if(Inventory.contains(item)) {
            return true;
        }
        Store.setStatus("Withdrawing " + item + ".");
        if(!Bank.isOpen()) {
            open();
            return false;
        }
        if(Inventory.isFull()) {
            Bank.depositInventory();
            Time.sleep(490, 759);
        }
        if(!Bank.contains(item)) {
            return false;
        }
        Bank.withdraw(item, quantity);
        Time.sleep(490, 759);
        return Inventory.contains(item);
    }

    public static boolean withdrawAll(String item) {
       return withdrawAll(item, BankLocation.getNearest(), false);
    }

    public static boolean withdrawAll(String item, BankLocation location, boolean useHomeTeleport) {
        if(Inventory.contains(item)) {
            return true;
        }
        Store.setStatus("Withdrawing " + item + ".");
        if(!Bank.isOpen()) {
            open(location, true);
            return false;
        }
        if(!Bank.contains(item)) {
            return false;
        }
        if(Inventory.isFull()) {
            Bank.depositInventory();
            Time.sleep(490, 759);
        }
        Bank.withdrawAll(item);
        Time.sleep(490, 759);
        return Inventory.contains(item);
    }

}
