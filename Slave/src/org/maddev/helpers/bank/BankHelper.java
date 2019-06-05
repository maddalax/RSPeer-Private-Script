package org.maddev.helpers.bank;

import org.maddev.Store;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.walking.MovementUtil;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

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
        Interactable bank = null;
        switch (location.getType()) {
            case NPC: {
                bank = Npcs.getNearest(location.getName());
                break;
            }
            case DEPOSIT_BOX:
            case BANK_CHEST:
            case BANK_BOOTH: {
                bank = SceneObjects.getNearest(i -> i.containsAction(location.getAction()) && i.getName().equals(location.getName()));
            }
        }
        if(bank == null) {
            if(location.getPosition().distance() > 10) {
                MovementHelper.walkRandomized(location.getPosition(), false, useHomeTeleport);
                Time.sleep(350, 650);
                return false;
            }
            Store.setAction("Failed to find bank.");
            Logger.severe("Failed to find bank.");
            return false;
        }
        InteractHelper.interact(bank, location.getAction());
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

    public static boolean withdrawNoted(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        for (ItemPair item : pair) {
            boolean isAll = item.getQuantity() == Integer.MAX_VALUE;
            if(!Bank.contains(item.getName())) {
                continue;
            }
            if(!(isAll ? withdrawAllNoted(item.getName(), location, useHomeTeleport) : withdrawNoted(item.getName(), item.getQuantity(), location, useHomeTeleport))) {
                return false;
            }
        }
        return true;
    }

    public static boolean withdrawOnly(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        List<String> names = Arrays.stream(pair).map(ItemPair::getName).collect(Collectors.toList());
        int size = 0;
        for (ItemPair p : pair) {
            size += p.getQuantity();
        }
        int count = 0;
        for (Item item : Inventory.getItems()) {
            boolean has = names.contains(item.getName());
            if(has) count++;
        }
        if(count == size && 28 - Inventory.getFreeSlots() == count) {
            return true;
        }
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
        BankCache.cache();
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
        BankCache.cache();
        return Inventory.contains(item);
    }

    public static boolean withdrawAllNoted(String item, BankLocation location, boolean useHomeTeleport) {
        if(Bank.isOpen() && Bank.getWithdrawMode() != Bank.WithdrawMode.NOTE) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleep(250, 550);
            return false;
        }
        return withdrawAll(item, location, useHomeTeleport);
    }

    public static boolean withdrawNoted(String item, int quantity, BankLocation location, boolean useHomeTeleport) {
        if(Bank.isOpen() && Bank.getWithdrawMode() != Bank.WithdrawMode.NOTE) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleep(250, 550);
            return false;
        }
        return withdraw(location, useHomeTeleport, item, quantity);
    }

    public static boolean depositAllExcept(BankLocation location, Predicate<Item> predicate) {
        Store.setAction("Depositing items.");
        if(!Bank.isOpen()) {
            BankHelper.open(location);
            Time.sleep(450, 850);
            return false;
        }
        Bank.depositAllExcept(predicate);
        Time.sleep(450, 850);
        BankCache.cache();
        return Inventory.containsOnly(predicate);
    }

}
