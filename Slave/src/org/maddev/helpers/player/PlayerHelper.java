package org.maddev.helpers.player;

import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.grand_exchange.GrandExchangeHelper;
import org.maddev.helpers.log.Logger;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;

public class PlayerHelper {

    public static int getTotalCount(String name) {
        Logger.fine("PlayerHelper", "Getting total count of: " + name);
        int count = BankCache.getCount(name);
        count += Inventory.getCount(true, name);
        count += Equipment.getCount(true, name);
        Logger.fine("PlayerHelper", "Count of " + name + ": " + count);
        return count;
    }

    public static boolean hasAll(String ... names) {
        Logger.fine("PlayerHelper", "Checking if we have all items: " + String.join(",", names));
        for (String name : names) {
            if(!BankCache.contains(name) && !Inventory.contains(name) && !Equipment.contains(name)) {
                Logger.fine("PlayerHelper", name + " was not found in inventory, equipment, or bank cache. hasAll is false.");
                return false;
            }
        }
        Logger.fine("PlayerHelper", "Found all items in inventory, equipment, or bank cache.");
        return true;
    }

    public static boolean hasAny(String ... names) {
        for (String name : names) {
            Logger.fine("PlayerHelper", "Checking if we have " + name + " in inventory, equipment, or bank cache.");
            if(BankCache.contains(name) || Inventory.contains(name) || Equipment.contains(name)) {
                Logger.fine("PlayerHelper", "Found " + name + " in inventory, equipment, or bank cache.");
                return true;
            }
        }
        Logger.fine("PlayerHelper", "Did not find any items in inventory, equipment, or bank cache. " + String.join(",", names));
        return false;
    }

    public static boolean hasAllIncludeGrandExchange(boolean onlyPending, String ... names) {
        Logger.fine("PlayerHelper", "hasAllIncludeGrandExchange for" + String.join(",", names) + " with onlyPending as: " + onlyPending);
        for (String name : names) {
            RSGrandExchangeOffer offer = GrandExchangeHelper.getBuyOffer(name);
            if(onlyPending && offer != null && offer.getProgress() != RSGrandExchangeOffer.Progress.IN_PROGRESS)  {
                Logger.fine("PlayerHelper", "Found offer by name: " + name + " that was not pending. Setting offer to null.");
                offer = null;
            }
            if(!BankCache.contains(name) && !Inventory.contains(name) && !Equipment.contains(name) && (offer == null)) {
                Logger.fine("Player did not have " + name + " and ge offer for " + name + " was null.");
                return false;
            }
        }
        Logger.fine("Found all of the items on either the player or pending grand exchange offers.");
        return true;
    }

    public static String getFirst(String ... names) {
        Logger.fine("PlayerHelper", "Attempting to get first: " + String.join(",", names));
        for (String name : names) {
            if(BankCache.contains(name) || Inventory.contains(name) || Equipment.contains(name)) {
                Logger.fine("PlayerHelper", "Found " + name + " in inventory, equipment, or bank cache.");
                return name;
            }
        }
        Logger.fine("PlayerHelper", "Did not find any items in inventory, equipment, or bank cache.");
        return null;
    }

}

