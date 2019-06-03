package org.maddev.helpers.bank;

import org.maddev.Store;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.walking.MovementUtil;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BankHelper {

    public static BankLocation nearest() {
        return BankLocation.getNearest(s -> s.getType() != BankLocation.Type.DEPOSIT_BOX);
    }

    public static boolean open(BankLocation location) {
       return open(location, false);
    }

    public static boolean open(BankLocation location, boolean useHomeTeleport) {
        if(location == null) {
            return false;
        }
        if(ZanarisHelper.inZanaris()) {
            return ZanarisHelper.openZanarisBank();
        }
        if(location == BankLocation.LUMBRIDGE_CASTLE && Players.getLocal().getPosition().getFloorLevel() == 0) {
            Time.sleep(350, 650);
            return MovementUtil.applyLumbridgeFix();
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
        return withdraw(nearest(), false, pair);
    }

    public static boolean withdraw(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        for (ItemPair item : pair) {
            boolean isAll = item.getQuantity() == Integer.MAX_VALUE;
            if(!(isAll ? withdrawAll(item.getName(), location, useHomeTeleport) : withdraw(item.getName(), item.getQuantity()))) {
                return false;
            }
        }
        return true;
    }

    public static boolean withdrawOnly(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        List<String> names = Arrays.stream(pair).map(ItemPair::getName).collect(Collectors.toList());
        if(Bank.isOpen()) {
            if(!Bank.depositAllExcept(s -> names.contains(s.getName()) && !s.isNoted())) {
                return false;
            }
            Time.sleep(230, 560);
        }
        for (ItemPair item : pair) {
            boolean isAll = item.getQuantity() == Integer.MAX_VALUE;
            if(!(isAll ? withdrawAll(item.getName(), location, useHomeTeleport) : withdraw(item.getName(), item.getQuantity()))) {
                return false;
            }
        }
        return true;
    }

    public static boolean withdraw(String item, int quantity) {
        return withdraw(nearest(), false, item, quantity);
    }


    public static boolean withdraw(BankLocation location, boolean useHomeTeleport, String item, int quantity) {
        return withdraw(location, useHomeTeleport, compare -> compare.getName().equals(item), quantity);
    }

    public static boolean withdraw(Predicate<Item> predicate, int quantity) {
        return withdraw(nearest(), false, predicate, quantity);
    }

    public static boolean withdraw(BankLocation location, boolean useHomeTeleport, Predicate<Item> predicate, int quantity) {
        if(Inventory.contains(predicate)) {
            return true;
        }
        Store.setAction("Withdrawing item via predicate.");
        if(!Bank.isOpen()) {
            open(location, useHomeTeleport);
            return false;
        }
        if(Inventory.isFull()) {
            Bank.depositInventory();
            Time.sleep(490, 759);
        }
        if(!Bank.contains(predicate)) {
            return false;
        }
        Bank.withdraw(predicate, quantity);
        Time.sleep(490, 759);
        return Inventory.contains(predicate);
    }

    public static boolean withdrawAll(String item) {
       return withdrawAll(item, nearest(), false);
    }

    public static boolean withdrawAll(String item, BankLocation location, boolean useHomeTeleport) {
        if(Inventory.isFull()) {
            return true;
        }
        if(Inventory.contains(item) && Bank.getCount(item) < Inventory.getCount(item)) {
            return true;
        }
        Store.setAction("Withdrawing " + item + ".");
        if(!Bank.isOpen()) {
            open(location, useHomeTeleport);
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

    public static boolean depositAllExcept(BankLocation location, Predicate<Item> predicate) {
        if(Inventory.containsOnly(predicate)) {
            return true;
        }
        if(!Bank.isOpen()) {
            BankHelper.open(location);
            Time.sleep(450, 850);
            return false;
        }
        Bank.depositAllExcept(predicate);
        Time.sleep(450, 850);
        return Inventory.containsOnly(predicate);
    }

}
