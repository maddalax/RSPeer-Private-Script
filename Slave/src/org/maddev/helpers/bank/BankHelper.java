package org.maddev.helpers.bank;

import org.maddev.Store;
import org.maddev.helpers.grand_exchange.GrandExchangeHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.walking.MovementUtil;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.Definitions;
import org.rspeer.runetek.api.commons.BankLocation;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.maddev.helpers.log.Logger;
import org.rspeer.runetek.providers.RSItemDefinition;

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
        if(Bank.isOpen()) {
            Logger.fine("Bank is open.");
            return true;
        }
        if(location == null) {
            Logger.fine("Bank location is null, unable to open bank.");
            return false;
        }
        if(ZanarisHelper.inZanaris()) {
            Logger.fine("Bank location is null, unable to open bank.");
            return ZanarisHelper.openZanarisBank();
        }
        if(location == BankLocation.GRAND_EXCHANGE && BankLocation.GRAND_EXCHANGE.getPosition().distance() > 10) {
            Logger.fine("Walking to grand exchange to open bank.");
            GrandExchangeHelper.walkTo();
            return false;
        }
        Logger.fine("Opening bank " + location.getName() + " at " + location.getPosition());
        if(location == BankLocation.LUMBRIDGE_CASTLE) {
            TimeHelper.sleep(350, 650);
            return MovementUtil.applyLumbridgeFix();
        }
        if(location.getPosition().distance() > 10) {
            Logger.fine("Walking closer to bank position. " + location.getPosition());
            MovementHelper.walkRandomized(location.getPosition(), false, useHomeTeleport);
            TimeHelper.sleep(350, 650);
            return false;
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
            Store.setAction("Failed to find bank even after walking closer.");
            Logger.severe("Failed to find bank.");
            return false;
        }
        Logger.fine("Interacting with bank interactable.");
        InteractHelper.interact(bank, location.getAction());
        TimeHelper.sleep(350, 650);
        return Bank.isOpen();
    }

    public static boolean withdraw(ItemPair ... pair) {
        return withdraw(nearest(), false, pair);
    }

    public static boolean withdraw(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        for (ItemPair item : pair) {
            Logger.fine("Attempting to withdraw: " + item.getName());
            boolean isAll = item.getQuantity() == Integer.MAX_VALUE;
            if(!(isAll ? withdrawAll(item.getName(), location, useHomeTeleport) : withdraw(item.getName(), item.getQuantity()))) {
                return false;
            }
        }
        return true;
    }

    public static boolean withdrawNoted(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        for (ItemPair item : pair) {
            Logger.fine("Attempting to withdraw noted: " + item.getName());
            boolean isAll = item.getQuantity() == Integer.MAX_VALUE;
            if(!(isAll ? withdrawAllNoted(item.getName(), location, useHomeTeleport) : withdrawNoted(item.getName(), item.getQuantity(), location, useHomeTeleport))) {
                return false;
            }
        }
        return true;
    }

    public static boolean withdrawOnly(BankLocation location, boolean useHomeTeleport, ItemPair ... pair) {
        List<String> names = Arrays.stream(pair).map(ItemPair::getName).collect(Collectors.toList());
        Logger.fine("Attempting to withdraw only: " + Arrays.toString(names.toArray(new String[0])));
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
            Logger.fine("Already has only specified items in the inventory.");
            return true;
        }
        if(Bank.isOpen()) {
            if(!Bank.depositAllExcept(s -> names.contains(s.getName()) && !s.isNoted())) {
                Logger.fine("Depositing all except items to withdraw only, and noted items.");
                return false;
            }
            TimeHelper.sleep(230, 560);
        }
        for (ItemPair item : pair) {
            Logger.fine("Attempting to withdraw " + item.getName() + " from withdraw only.");
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
        Store.setAction("Withdrawing item: " + item);
        return withdraw(location, useHomeTeleport, compare -> compare.getName().equals(item), quantity);
    }

    public static boolean withdraw(Predicate<Item> predicate, int quantity) {
        return withdraw(nearest(), false, predicate, quantity);
    }

    public static boolean withdraw(BankLocation location, boolean useHomeTeleport, Predicate<Item> predicate, int quantity) {
        if(Inventory.contains(predicate)) {
            Logger.fine("Inventory contains item matched by predicate, returning.");
            return true;
        }
        Store.setAction("Withdrawing item via predicate.");
        if(!Bank.isOpen()) {
            open(location, useHomeTeleport);
            return false;
        }
        if(!Bank.contains(predicate)) {
            Logger.fine("Bank did not contain any items matching the predicate, skipping...");
            return true;
        }
        if(Inventory.isFull()) {
            Logger.fine("Depositing inventory.");
            Bank.depositInventory();
            TimeHelper.sleep(490, 759);
        }
        Logger.fine("Executing withdraw.");
        Bank.withdraw(predicate, quantity);
        TimeHelper.sleep(490, 759);
        BankCache.cache();
        return Inventory.contains(predicate);
    }

    public static boolean withdrawAll(String item) {
       return withdrawAll(item, nearest(), false);
    }

    public static boolean withdrawAll(String item, BankLocation location, boolean useHomeTeleport) {
        if(Inventory.isFull() && Bank.isOpen()) {
            Logger.fine("Depositing inventory.");
            Bank.depositInventory();
            TimeHelper.sleep(490, 759);
            return false;
        }
        if(Inventory.contains(item) && Inventory.isFull() && Definitions.getItem(item, s -> !s.isStackable()) != null) {
            Logger.fine("Inventory already contains " + item + " or the bank count is less than inventory count. Item is not stackable so we cannot withdraw more.");
            return true;
        }
        if(!BankCache.contains(item) && Definitions.getItem(item, RSItemDefinition::isStackable) != null) {
            Logger.fine("Bank does not contain: " + item + " and item is stackable.");
            return true;
        }
        Store.setAction("Withdrawing " + item + ".");
        if(!Bank.isOpen()) {
            Logger.fine("Opening bank.");
            open(location, useHomeTeleport);
            return false;
        }
        if(!Bank.contains(item)) {
            Logger.fine("Bank does not have " + item + ".");
            return false;
        }
        if(!Inventory.isFull()) {
            Logger.fine("Inventory is not full, executing withdraw: " + item + ".");
            Bank.withdrawAll(item);
        }
        TimeHelper.sleep(490, 759);
        BankCache.cache();
        return Inventory.contains(item);
    }

    public static boolean withdrawAllNoted(String item, BankLocation location, boolean useHomeTeleport) {
        Logger.fine("Withdrawing All Noted: " + item);
        if(Bank.isOpen() && Bank.getWithdrawMode() != Bank.WithdrawMode.NOTE) {
            Logger.fine("Setting bank mode to noted.");
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            TimeHelper.sleep(250, 550);
            return false;
        }
        return withdrawAll(item, location, useHomeTeleport);
    }

    public static boolean withdrawNoted(String item, int quantity, BankLocation location, boolean useHomeTeleport) {
        if(Bank.isOpen() && Bank.getWithdrawMode() != Bank.WithdrawMode.NOTE) {
            Logger.fine("Setting withraw mode to note.");
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            TimeHelper.sleep(250, 550);
            return false;
        }
        return withdraw(location, useHomeTeleport, item, quantity);
    }

    public static boolean depositAllExcept(BankLocation location, Predicate<Item> predicate) {
        Store.setAction("Depositing items.");
        if(!Bank.isOpen()) {
            BankHelper.open(location);
            TimeHelper.sleep(450, 850);
            return false;
        }
        Bank.depositAllExcept(predicate);
        TimeHelper.sleep(450, 850);
        BankCache.cache();
        return Inventory.containsOnly(predicate);
    }

}
